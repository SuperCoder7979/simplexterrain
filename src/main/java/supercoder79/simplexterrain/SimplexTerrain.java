package supercoder79.simplexterrain;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeKeys;
import supercoder79.simplexterrain.api.biomes.SimplexBiomes;
import supercoder79.simplexterrain.api.biomes.SimplexClimate;
import supercoder79.simplexterrain.command.MapHeightmapCommand;
import supercoder79.simplexterrain.command.MapNoiseTypeCommand;
import supercoder79.simplexterrain.command.ReloadConfigCommand;
import supercoder79.simplexterrain.compat.Compat;
import supercoder79.simplexterrain.compat.VanillaBiomes;
import supercoder79.simplexterrain.configs.ConfigHelper;
import supercoder79.simplexterrain.configs.MainConfigData;
import supercoder79.simplexterrain.world.SimplexWorldType;
import supercoder79.simplexterrain.world.gen.SimplexBiomeSource;
import supercoder79.simplexterrain.world.gen.SimplexChunkGenerator;

import java.util.concurrent.*;

public class SimplexTerrain implements ModInitializer {
	public static final String VERSION = "0.7.2";

	//if the current world is a Simplex Terrain world. Has no meaning when outside of a world.
	public static boolean isSimplexEnabled = false;

	public static MainConfigData CONFIG;
	public static ForkJoinPool globalThreadPool;

	public static SimplexWorldType levelGeneratorType;

	private static Identifier SWAMP;
	private static Identifier PLAINS;
	private static Identifier FOREST;
	private static Identifier TAIGA;
	private static Identifier BIRCH_FOREST;
	private static Identifier JUNGLE;
	private static Identifier MOUNTAINS;
	private static Identifier MOUNTAIN_EDGE;
	private static Identifier WOODED_MOUNTAINS;
	private static Identifier GRAVELLY_MOUNTAINS;
	private static Identifier DESERT;
	private static Identifier SAVANNA;
	private static Identifier BAMBOO_JUNGLE;
	private static Identifier BADLANDS_PLATEAU;

	@Override
	public void onInitialize() {
		SWAMP = BiomeKeys.SWAMP.getValue();
		PLAINS = BiomeKeys.PLAINS.getValue();
		FOREST = BiomeKeys.FOREST.getValue();
		TAIGA = BiomeKeys.TAIGA.getValue();
		BIRCH_FOREST = BiomeKeys.BIRCH_FOREST.getValue();
		JUNGLE = BiomeKeys.JUNGLE.getValue();
		MOUNTAINS = BiomeKeys.MOUNTAINS.getValue();
		MOUNTAIN_EDGE = BiomeKeys.MOUNTAIN_EDGE.getValue();
		WOODED_MOUNTAINS = BiomeKeys.WOODED_MOUNTAINS.getValue();
		GRAVELLY_MOUNTAINS = BiomeKeys.GRAVELLY_MOUNTAINS.getValue();
		DESERT = BiomeKeys.DESERT.getValue();
		SAVANNA = BiomeKeys.SAVANNA.getValue();
		BAMBOO_JUNGLE = BiomeKeys.BAMBOO_JUNGLE.getValue();
		BADLANDS_PLATEAU = BiomeKeys.BADLANDS_PLATEAU.getValue();

		ConfigHelper.init();

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			levelGeneratorType = new SimplexWorldType();
		}

		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("simplexterrain:simplex"), SimplexChunkGenerator.CODEC);
		Registry.register(Registry.BIOME_SOURCE, new Identifier("simplexterrain:simplex"), SimplexBiomeSource.CODEC);

		globalThreadPool = new ForkJoinPool(CONFIG.noiseGenerationThreads,
						ForkJoinPool.defaultForkJoinWorkerThreadFactory,
						null, true);

		addDefaultBiomes();

		VanillaBiomes.register();

//		SimplexBiomes.addReplacementBiome(FOREST, biomeId(Biomes.FLOWER_FOREST), 15);
//		SimplexBiomes.addReplacementBiome(BIRCH_FOREST, biomeId(Biomes.TALL_BIRCH_FOREST), 6);
//		SimplexBiomes.addReplacementBiome(PLAINS, biomeId(Biomes.SUNFLOWER_PLAINS), 50);
//		SimplexBiomes.addReplacementBiome(JUNGLE, BAMBOO_JUNGLE, 8);
//		SimplexBiomes.addReplacementBiome(biomeId(Biomes.SNOWY_TUNDRA), biomeId(Biomes.ICE_SPIKES), 20);
//		SimplexBiomes.addReplacementBiome(biomeId(Biomes.PLAINS), biomeId(Biomes.MUSHROOM_FIELDS), 100);

		if (CONFIG.reloadConfigCommand) {
			ReloadConfigCommand.init();
		}

		// Dev only commands
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			MapHeightmapCommand.init();
			MapNoiseTypeCommand.init();
		}

		//Addition to the chunk generator
		SimplexTerrain.CONFIG.postProcessors.forEach(postProcessors -> SimplexChunkGenerator.addTerrainPostProcessor(postProcessors.postProcessor));
		SimplexTerrain.CONFIG.noiseModifiers.forEach(noiseModifiers -> SimplexChunkGenerator.addNoiseModifier(noiseModifiers.noiseModifier));
	}

	private static void addDefaultBiomes() {
		//holy shit this code is still cursed

		if (CONFIG.doModCompat) {
			// Mod Compat
			if (FabricLoader.getInstance().isModLoaded("winterbiomemod")) {
				addWinterBiomes();
				System.out.println("Winter biomes registered!");
			}

			if (FabricLoader.getInstance().isModLoaded("traverse")) {
				Compat.addTraverseBiomes();
				System.out.println("Traverse biomes registered!");
			}

			if (FabricLoader.getInstance().isModLoaded("terrestria")) {
				Compat.addTerrestriaBiomes();
				System.out.println("Terrestria biomes registered!");
			}

			if (FabricLoader.getInstance().isModLoaded("byg")) {
				Compat.addBYGBiomes();
				System.out.println("Biomes You'll Go biomes registered!");
			}
		}

		addVanillaLowlands();
		addVanillaMidlands();
		addVanillaHighlands();
		addVanillaMountainPeaks();

//		CommandRegistry.INSTANCE.register(false, dispatcher -> {
//			LiteralArgumentBuilder<ServerCommandSource> lab = CommandManager.literal("sdebug").requires(executor -> executor.hasPermissionLevel(2)).executes(cmd -> {
//				ServerCommandSource source = cmd.getSource();
//				source.sendFeedback(new LiteralText(Formatting.DARK_GREEN.toString() + Formatting.BOLD.toString() +
//						"Derivative (quad): " + NoiseMath.derivative(SimplexChunkGenerator.THIS.baseNoise, source.getPlayer().getX(), source.getPlayer().getZ())), true);
////				source.sendFeedback(new LiteralText(Formatting.GREEN.toString() + Formatting.BOLD.toString() +
////						"Derivative (tri): " + NoiseMath.derivative2(SimplexChunkGenerator.THIS.newNoise, source.getPlayer().getX(), source.getPlayer().getZ())), true);
//				return 1;
//			});
//			dispatcher.register(lab);
//		});

	}

	public static void addWinterBiomes() {
		double whiteOaksWeight = 0.75;
		double alpineWeight = 1.0;
		double subalpineWeight = 1.0;
		double alpinePeaksWeight = 1.0;
		double alpineGlacierWeight = 0.5;

		//lowlands biomes
		SimplexBiomes.addLowlandsBiome(new Identifier("winterbiomemod", "white_oaks"), SimplexClimate.SNOWY, whiteOaksWeight);

		SimplexBiomes.addLowlandsBiome(new Identifier("winterbiomemod", "white_oaks_thicket"), SimplexClimate.SNOWY, whiteOaksWeight*0.75);

		//Midlands biomes
		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks"), SimplexClimate.BOREAL, whiteOaksWeight*0.8);
		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks"), SimplexClimate.DRY_BOREAL, whiteOaksWeight*0.8);
		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks"), SimplexClimate.LUSH_BOREAL, whiteOaksWeight*0.8);

		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks_thicket"), SimplexClimate.BOREAL, whiteOaksWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks_thicket"), SimplexClimate.DRY_BOREAL, whiteOaksWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("winterbiomemod", "white_oaks_thicket"), SimplexClimate.LUSH_BOREAL, whiteOaksWeight*0.6);

		//highlands biomes
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "alpine"), SimplexClimate.BOREAL, alpineWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "alpine"), SimplexClimate.LUSH_BOREAL, alpineWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "alpine"), SimplexClimate.TEMPERATE, alpineWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "alpine"), SimplexClimate.LUSH_TEMPERATE, alpineWeight);

		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "subalpine"), SimplexClimate.DRY_BOREAL, subalpineWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "subalpine"), SimplexClimate.DRY_TEMPERATE, subalpineWeight*0.9);

		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "subalpine_crag"), SimplexClimate.DRY_BOREAL, subalpineWeight*0.8);
		SimplexBiomes.addHighlandsBiome(new Identifier("winterbiomemod", "subalpine_crag"), SimplexClimate.DRY_TEMPERATE, subalpineWeight*0.7);

		//peaks biomes
		SimplexBiomes.addMountainPeaksBiome(new Identifier("winterbiomemod", "alpine_peaks"), SimplexClimate.BOREAL, alpinePeaksWeight);
		SimplexBiomes.addMountainPeaksBiome(new Identifier("winterbiomemod", "alpine_peaks"), SimplexClimate.DRY_BOREAL, alpinePeaksWeight);
		SimplexBiomes.addMountainPeaksBiome(new Identifier("winterbiomemod", "alpine_peaks"), SimplexClimate.LUSH_BOREAL, alpinePeaksWeight);

		SimplexBiomes.addMountainPeaksBiome(new Identifier("winterbiomemod", "alpine_glacier"), SimplexClimate.SNOWY, alpineGlacierWeight);
	}

	private static void addVanillaLowlands() {
		final double swampWeight = 0.8;
		final double desertWeight = 1.6;
		final double jungleWeight = 1.7;
		final double savannahWeight = 1.2;
		final double plainsWeight = 1.0;
		final double forestWeight = 1.0;
		final double taigaWeight = 1.2;
		final double birchForestWeight = 0.3;
		final double snowyTaigaWeight = 1.4;
		final double snowyTundraWeight = 0.6;
		final double mesaWeight = 0.6;

		SimplexBiomes.addLowlandsBiome(DESERT, SimplexClimate.DRY_TROPICAL, desertWeight);

		SimplexBiomes.addLowlandsBiome(SAVANNA, SimplexClimate.TROPICAL, savannahWeight * 0.8);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight);

		SimplexBiomes.addLowlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addLowlandsBiome(JUNGLE, SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75);
		SimplexBiomes.addLowlandsBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addLowlandsBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75 / 2);
		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.LUSH_TROPICAL, swampWeight / 2);

		SimplexBiomes.addLowlandsBiome(DESERT, SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addLowlandsBiome(DESERT, SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addLowlandsBiome(BADLANDS_PLATEAU, SimplexClimate.DRY_TEMPERATE, desertWeight);
		SimplexBiomes.addLowlandsBiome(BADLANDS_PLATEAU, SimplexClimate.DRY_TROPICAL, desertWeight*0.75);

		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.DRY_TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(SAVANNA, SimplexClimate.DRY_TROPICAL, savannahWeight);
		SimplexBiomes.addLowlandsBiome(SAVANNA, SimplexClimate.DRY_TEMPERATE, savannahWeight*0.8);

		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.TEMPERATE, swampWeight);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight / 2);
		SimplexBiomes.addLowlandsBiome(FOREST, SimplexClimate.TEMPERATE, forestWeight * 0.8);

		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.LUSH_TEMPERATE, swampWeight);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.LUSH_TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(FOREST, SimplexClimate.LUSH_TEMPERATE, forestWeight * 1.5);

		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.DRY_BOREAL, plainsWeight);
		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.DRY_BOREAL, taigaWeight);

		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.BOREAL, taigaWeight);
		SimplexBiomes.addLowlandsBiome(BIRCH_FOREST, SimplexClimate.BOREAL, birchForestWeight);

		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.LUSH_BOREAL, taigaWeight);
		SimplexBiomes.addLowlandsBiome(BiomeKeys.GIANT_TREE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addLowlandsBiome(BiomeKeys.GIANT_SPRUCE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addLowlandsBiome(BiomeKeys.SNOWY_TAIGA.getValue(), SimplexClimate.SNOWY, snowyTaigaWeight);
		SimplexBiomes.addLowlandsBiome(BiomeKeys.SNOWY_TUNDRA.getValue(), SimplexClimate.SNOWY, snowyTundraWeight);
	}

	private static void addVanillaMidlands() {
		final double jungleWeight = 1.0;
		final double savannaPlateauWeight = 1.0;
		final double birchForestWeight = 0.8;
		final double darkForestWeight = 1.0;
		final double forestWeight = 1.0;
		final double plainsWeight = 0.5;
		final double swampWeight = 0.4;
		final double taigaWeight = 0.9;
		final double snowyTaigaWeight = 1.0;
		final double snowyTundraWeight = 1.0;
		final double mesaWeight = 0.6;

		SimplexBiomes.addMidlandsBiome(BiomeKeys.SAVANNA_PLATEAU.getValue(), SimplexClimate.DRY_TROPICAL, savannaPlateauWeight);

		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight * 1.6);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.SAVANNA_PLATEAU.getValue(), SimplexClimate.TROPICAL, savannaPlateauWeight);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.SAVANNA_PLATEAU.getValue(), SimplexClimate.DRY_TROPICAL, savannaPlateauWeight*0.75);

		SimplexBiomes.addMidlandsBiome(BADLANDS_PLATEAU, SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addMidlandsBiome(BADLANDS_PLATEAU, SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.TROPICAL, jungleWeight*0.8);
		SimplexBiomes.addMidlandsBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addMidlandsBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75 / 2);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.DARK_FOREST.getValue(), SimplexClimate.LUSH_TROPICAL, darkForestWeight);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.DARK_FOREST.getValue(), SimplexClimate.TROPICAL, darkForestWeight*0.8);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.TEMPERATE, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight * 0.5);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.TEMPERATE, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.TEMPERATE, forestWeight);
		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight);

		SimplexBiomes.addMidlandsBiome(BiomeKeys.DARK_FOREST.getValue(), SimplexClimate.LUSH_TEMPERATE, darkForestWeight);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.LUSH_TEMPERATE, forestWeight);
		SimplexBiomes.addMidlandsBiome(SWAMP, SimplexClimate.LUSH_TEMPERATE, swampWeight);

		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.DRY_BOREAL, birchForestWeight);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.BOREAL, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.BOREAL, taigaWeight);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.LUSH_BOREAL, birchForestWeight * 1.5);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.LUSH_BOREAL, forestWeight * 0.5);
		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.LUSH_BOREAL, taigaWeight);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.GIANT_TREE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.GIANT_SPRUCE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addMidlandsBiome(BiomeKeys.SNOWY_TAIGA.getValue(), SimplexClimate.SNOWY, snowyTaigaWeight);
		SimplexBiomes.addMidlandsBiome(BiomeKeys.SNOWY_TUNDRA.getValue(), SimplexClimate.SNOWY, snowyTundraWeight);
	}

	private static void addVanillaHighlands() {
		final double plainsWeight = 1.0;
		final double jungleWeight = 0.6;
		final double savannaPlateauMWeight = 0.5;
		final double mountainEdgeWeight = 1.0;
		final double woodedMountainsWeight = 1.0;
		final double taigaWeight = 0.8;
		final double snowyTaigaWeight = 0.6;
		final double mesaWeight = 0.5;
		final double desertWeight = 0.4;

		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.DRY_TROPICAL, plainsWeight);
		SimplexBiomes.addHighlandsBiome(BiomeKeys.SHATTERED_SAVANNA_PLATEAU.getValue(), SimplexClimate.DRY_TROPICAL, savannaPlateauMWeight);
		SimplexBiomes.addHighlandsBiome(DESERT, SimplexClimate.DRY_TROPICAL, desertWeight);

		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight);

		SimplexBiomes.addHighlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addHighlandsBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, plainsWeight);

		SimplexBiomes.addHighlandsBiome(BiomeKeys.WOODED_BADLANDS_PLATEAU.getValue(), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addHighlandsBiome(WOODED_MOUNTAINS, SimplexClimate.DRY_TEMPERATE, woodedMountainsWeight);
		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.DRY_TEMPERATE, mountainEdgeWeight);

		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.TEMPERATE, mountainEdgeWeight);

		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.LUSH_TEMPERATE, mountainEdgeWeight);

		SimplexBiomes.addHighlandsBiome(WOODED_MOUNTAINS, SimplexClimate.DRY_BOREAL, woodedMountainsWeight);
		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.DRY_BOREAL, mountainEdgeWeight * 1.5);
		SimplexBiomes.addHighlandsBiome(TAIGA, SimplexClimate.DRY_BOREAL, taigaWeight);

		SimplexBiomes.addHighlandsBiome(WOODED_MOUNTAINS, SimplexClimate.BOREAL, woodedMountainsWeight);
		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.BOREAL, mountainEdgeWeight);
		SimplexBiomes.addHighlandsBiome(TAIGA, SimplexClimate.BOREAL, taigaWeight);

		SimplexBiomes.addHighlandsBiome(WOODED_MOUNTAINS, SimplexClimate.LUSH_BOREAL, woodedMountainsWeight);
		SimplexBiomes.addHighlandsBiome(TAIGA, SimplexClimate.LUSH_BOREAL, taigaWeight);
		SimplexBiomes.addHighlandsBiome(BiomeKeys.GIANT_TREE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addHighlandsBiome(BiomeKeys.GIANT_SPRUCE_TAIGA.getValue(), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.SNOWY, mountainEdgeWeight);
		SimplexBiomes.addHighlandsBiome(BiomeKeys.SNOWY_TAIGA.getValue(), SimplexClimate.SNOWY, snowyTaigaWeight);
	}

	private static void addVanillaMountainPeaks() {
		final double gravellyMountainsWeight = 0.6;
		final double mountainsWeight = 1.0;
		final double plainsWeight = 1.0;
		final double forestWeight = 1.0;
		final double forestedMountainWeight = 1.0;
		final double mesaWeight = 0.4;
		final double jungleWeight = 0.45;
		final double tundraWeight = 1;

		SimplexBiomes.addMountainPeaksBiome(PLAINS, SimplexClimate.DRY_TROPICAL, plainsWeight);

		SimplexBiomes.addMountainPeaksBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight);
		SimplexBiomes.addMountainPeaksBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addMountainPeaksBiome(BAMBOO_JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TROPICAL, gravellyMountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, forestWeight);
		SimplexBiomes.addMountainPeaksBiome(BiomeKeys.WOODED_BADLANDS_PLATEAU.getValue(), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);
		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TROPICAL, gravellyMountainsWeight * 0.4);

		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.DRY_TEMPERATE, gravellyMountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TEMPERATE, gravellyMountainsWeight);
		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.TEMPERATE, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(WOODED_MOUNTAINS, SimplexClimate.LUSH_TEMPERATE, forestedMountainWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.DRY_BOREAL, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.BOREAL, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(WOODED_MOUNTAINS, SimplexClimate.LUSH_BOREAL, forestedMountainWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.SNOWY, mountainsWeight);
		SimplexBiomes.addMountainPeaksBiome(BiomeKeys.SNOWY_TUNDRA.getValue(), SimplexClimate.SNOWY, tundraWeight);
	}
}

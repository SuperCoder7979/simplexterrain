package supercoder79.simplexterrain;

import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import supercoder79.simplexterrain.api.biomes.SimplexBiomes;
import supercoder79.simplexterrain.api.biomes.SimplexClimate;
import supercoder79.simplexterrain.api.biomes.SimplexNether;
import supercoder79.simplexterrain.command.ReloadConfigCommand;
import supercoder79.simplexterrain.compat.Compat;
import supercoder79.simplexterrain.configs.Config;
import supercoder79.simplexterrain.configs.ConfigUtil;
import supercoder79.simplexterrain.configs.MainConfigData;
import supercoder79.simplexterrain.configs.noisemodifiers.DetailsConfigData;
import supercoder79.simplexterrain.configs.noisemodifiers.MountainConfigData;
import supercoder79.simplexterrain.configs.noisemodifiers.RidgesConfigData;
import supercoder79.simplexterrain.scripting.SimplexScripting;
import supercoder79.simplexterrain.world.SimplexGenType;
import supercoder79.simplexterrain.world.gen.SimplexBiomeSource;
import supercoder79.simplexterrain.world.gen.SimplexChunkGenerator;

public class SimplexTerrain implements ModInitializer {
	public static final String VERSION = "0.7.0";

	//if the current world is a Simplex Terrain world. Has no meaning when outside of a world.
	public static boolean isSimplexEnabled = false;

	public static MainConfigData CONFIG;
	public static MountainConfigData MOUNTAIN_CONFIG;
	public static RidgesConfigData RIDGES_CONFIG;
	public static DetailsConfigData DETAIL_CONFIG;

	public static ForkJoinPool globalThreadPool;

	public static SimplexGenType levelGeneratorType;

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

	@Override
	public void onInitialize() {
		SWAMP = biomeId(Biomes.SWAMP);
		PLAINS = biomeId(Biomes.PLAINS);
		FOREST = biomeId(Biomes.FOREST);
		TAIGA = biomeId(Biomes.TAIGA);
		BIRCH_FOREST = biomeId(Biomes.BIRCH_FOREST);
		JUNGLE = biomeId(Biomes.JUNGLE);
		MOUNTAINS = biomeId(Biomes.MOUNTAINS);
		MOUNTAIN_EDGE = biomeId(Biomes.MOUNTAIN_EDGE);
		WOODED_MOUNTAINS = biomeId(Biomes.WOODED_MOUNTAINS);
		GRAVELLY_MOUNTAINS = biomeId(Biomes.GRAVELLY_MOUNTAINS);

		Config.init();
		MOUNTAIN_CONFIG = ConfigUtil.getFromConfig(MountainConfigData.class, Paths.get("config", "simplexterrain", "noisemodifiers", "mountains.json"));
		RIDGES_CONFIG = ConfigUtil.getFromConfig(RidgesConfigData.class, Paths.get("config", "simplexterrain", "noisemodifiers", "ridges.json"));
		DETAIL_CONFIG = ConfigUtil.getFromConfig(DetailsConfigData.class, Paths.get("config", "simplexterrain", "noisemodifiers", "details.json"));
		SimplexScripting.loadScripts(CONFIG.terrainShape);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			levelGeneratorType = new SimplexGenType();
		}
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("simplexterrain:simplex"), SimplexChunkGenerator.CODEC);
		Registry.register(Registry.BIOME_SOURCE, new Identifier("simplexterrain:simplex"), SimplexBiomeSource.CODEC);

		//TODO: custom thread pool thing
		globalThreadPool = new ForkJoinPool(CONFIG.noiseGenerationThreads,
				ForkJoinPool.defaultForkJoinWorkerThreadFactory,
				null, true);

		addDefaultBiomes();
		SimplexBiomes.addReplacementBiome(FOREST, biomeId(Biomes.FLOWER_FOREST), 15);
		SimplexBiomes.addReplacementBiome(BIRCH_FOREST, biomeId(Biomes.TALL_BIRCH_FOREST), 6);
		SimplexBiomes.addReplacementBiome(PLAINS, biomeId(Biomes.SUNFLOWER_PLAINS), 50);
		SimplexBiomes.addReplacementBiome(JUNGLE, biomeId(Biomes.BAMBOO_JUNGLE), 8);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.SNOWY_TUNDRA), biomeId(Biomes.ICE_SPIKES), 20);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.PLAINS), biomeId(Biomes.MUSHROOM_FIELDS), 100);

		if (CONFIG.reloadConfigCommand) {
			ReloadConfigCommand.init();
		}

		//Addition to the chunk generator
		SimplexTerrain.CONFIG.postProcessors.forEach(postProcessors -> SimplexChunkGenerator.addTerrainPostProcessor(postProcessors.postProcessor));
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

		//Add nether biome data
		SimplexNether.setBiomeExpansiveness(Biomes.CRIMSON_FOREST, 1.2);
		SimplexNether.setBiomeExpansiveness(Biomes.WARPED_FOREST, 0.8);
		SimplexNether.setBiomeExpansiveness(Biomes.SOUL_SAND_VALLEY, -0.25);
		SimplexNether.setBiomeExpansiveness(Biomes.BASALT_DELTAS, -0.5);
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

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TROPICAL, desertWeight);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SAVANNA), SimplexClimate.TROPICAL, savannahWeight * 0.8);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight);

		SimplexBiomes.addLowlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addLowlandsBiome(JUNGLE, SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75 / 2);
		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.LUSH_TROPICAL, swampWeight / 2);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BADLANDS_PLATEAU), SimplexClimate.DRY_TEMPERATE, desertWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BADLANDS_PLATEAU), SimplexClimate.DRY_TROPICAL, desertWeight*0.75);

		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.DRY_TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SAVANNA), SimplexClimate.DRY_TROPICAL, savannahWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SAVANNA), SimplexClimate.DRY_TEMPERATE, savannahWeight*0.8);

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
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.GIANT_TREE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.GIANT_SPRUCE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SNOWY_TAIGA), SimplexClimate.SNOWY, snowyTaigaWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SNOWY_TUNDRA), SimplexClimate.SNOWY, snowyTundraWeight);
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

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.SAVANNA_PLATEAU), SimplexClimate.DRY_TROPICAL, savannaPlateauWeight);

		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight * 1.6);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.SAVANNA_PLATEAU), SimplexClimate.TROPICAL, savannaPlateauWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.SAVANNA_PLATEAU), SimplexClimate.DRY_TROPICAL, savannaPlateauWeight*0.75);

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS_PLATEAU), SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS_PLATEAU), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.TROPICAL, jungleWeight*0.8);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TEMPERATE, jungleWeight*0.75 / 2);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.DARK_FOREST), SimplexClimate.LUSH_TROPICAL, darkForestWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.DARK_FOREST), SimplexClimate.TROPICAL, darkForestWeight*0.8);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.TEMPERATE, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight * 0.5);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.TEMPERATE, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.TEMPERATE, forestWeight);
		SimplexBiomes.addMidlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight);

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.DARK_FOREST), SimplexClimate.LUSH_TEMPERATE, darkForestWeight);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.LUSH_TEMPERATE, forestWeight);
		SimplexBiomes.addMidlandsBiome(SWAMP, SimplexClimate.LUSH_TEMPERATE, swampWeight);

		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.DRY_BOREAL, birchForestWeight);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.BOREAL, birchForestWeight);
		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.BOREAL, taigaWeight);

		SimplexBiomes.addMidlandsBiome(BIRCH_FOREST, SimplexClimate.LUSH_BOREAL, birchForestWeight * 1.5);
		SimplexBiomes.addMidlandsBiome(FOREST, SimplexClimate.LUSH_BOREAL, forestWeight * 0.5);
		SimplexBiomes.addMidlandsBiome(TAIGA, SimplexClimate.LUSH_BOREAL, taigaWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.GIANT_TREE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.GIANT_SPRUCE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.SNOWY_TAIGA), SimplexClimate.SNOWY, snowyTaigaWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.SNOWY_TUNDRA), SimplexClimate.SNOWY, snowyTundraWeight);
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
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.SHATTERED_SAVANNA_PLATEAU), SimplexClimate.DRY_TROPICAL, savannaPlateauMWeight);
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TROPICAL, desertWeight);

		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.TROPICAL, plainsWeight);

		SimplexBiomes.addHighlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, plainsWeight);

		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.WOODED_BADLANDS_PLATEAU), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

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
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.GIANT_TREE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 2);
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.GIANT_SPRUCE_TAIGA), SimplexClimate.LUSH_BOREAL, taigaWeight / 3);

		SimplexBiomes.addHighlandsBiome(MOUNTAIN_EDGE, SimplexClimate.SNOWY, mountainEdgeWeight);
		SimplexBiomes.addHighlandsBiome(biomeId(Biomes.SNOWY_TAIGA), SimplexClimate.SNOWY, snowyTaigaWeight);
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
		SimplexBiomes.addMountainPeaksBiome(biomeId(Biomes.BAMBOO_JUNGLE), SimplexClimate.LUSH_TROPICAL, jungleWeight / 2);
		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TROPICAL, gravellyMountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, forestWeight);
		SimplexBiomes.addMountainPeaksBiome(biomeId(Biomes.WOODED_BADLANDS_PLATEAU), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);
		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TROPICAL, gravellyMountainsWeight * 0.4);

		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.DRY_TEMPERATE, gravellyMountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TEMPERATE, gravellyMountainsWeight);
		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.TEMPERATE, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(biomeId(Biomes.WOODED_MOUNTAINS), SimplexClimate.LUSH_TEMPERATE, forestedMountainWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.DRY_BOREAL, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.BOREAL, mountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(biomeId(Biomes.WOODED_MOUNTAINS), SimplexClimate.LUSH_BOREAL, forestedMountainWeight);

		SimplexBiomes.addMountainPeaksBiome(MOUNTAINS, SimplexClimate.SNOWY, mountainsWeight);
		SimplexBiomes.addMountainPeaksBiome(biomeId(Biomes.SNOWY_TUNDRA), SimplexClimate.SNOWY, tundraWeight);
	}

	private static final Identifier biomeId(Biome biome) {
		return Registry.BIOME.getId(biome);
	}
}

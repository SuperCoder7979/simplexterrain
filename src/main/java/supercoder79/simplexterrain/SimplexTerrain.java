package supercoder79.simplexterrain;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.level.generator.v1.LevelGeneratorFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.OverworldChunkGeneratorConfig;
import net.minecraft.world.level.LevelGeneratorType;

import supercoder79.simplexterrain.api.biomes.SimplexBiomes;
import supercoder79.simplexterrain.api.biomes.SimplexClimate;
import supercoder79.simplexterrain.command.ReloadConfigCommand;
import supercoder79.simplexterrain.configs.Config;
import supercoder79.simplexterrain.configs.MainConfigData;
import supercoder79.simplexterrain.world.biomelayers.layers.SimplexClimateLayer;
import supercoder79.simplexterrain.world.gen.SimplexBiomeSource;
import supercoder79.simplexterrain.world.gen.SimplexBiomeSourceConfig;
import supercoder79.simplexterrain.world.gen.SimplexChunkGenerator;

public class SimplexTerrain implements ModInitializer {
	public static final String VERSION = "0.5.0";
	public static final String MOD_ID = "simplexterrain";

	public static ChunkGeneratorType<OverworldChunkGeneratorConfig, SimplexChunkGenerator> SIMPLEX_CHUNKGEN;
	public static LevelGeneratorType SIMPLEX_LEVELGEN;

	public static MainConfigData CONFIG;

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

	public static SimplexClimateLayer climateLayer;

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

		addDefaultBiomes();
		SimplexBiomes.addReplacementBiome(FOREST, biomeId(Biomes.FLOWER_FOREST), 15);
		SimplexBiomes.addReplacementBiome(BIRCH_FOREST, biomeId(Biomes.TALL_BIRCH_FOREST), 6);
		SimplexBiomes.addReplacementBiome(PLAINS, biomeId(Biomes.SUNFLOWER_PLAINS), 50);
		SimplexBiomes.addReplacementBiome(JUNGLE, biomeId(Biomes.BAMBOO_JUNGLE), 8);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.BADLANDS), biomeId(Biomes.BADLANDS_PLATEAU), 6);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.BADLANDS), biomeId(Biomes.WOODED_BADLANDS_PLATEAU), 10);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.SNOWY_TUNDRA), biomeId(Biomes.ICE_SPIKES), 20);
		SimplexBiomes.addReplacementBiome(biomeId(Biomes.PLAINS), biomeId(Biomes.MUSHROOM_FIELDS), 100);

		if (CONFIG.reloadConfigCommand) {
			ReloadConfigCommand.init();
		}

		//Addition to the chunk generator
		SimplexTerrain.CONFIG.postProcessors.forEach(postProcessors -> SimplexChunkGenerator.addTerrainPostProcessor(postProcessors.postProcessor));
		SimplexTerrain.CONFIG.noiseModifiers.forEach(noiseModifiers -> SimplexChunkGenerator.addNoiseModifier(noiseModifiers.noiseModifier));

		Identifier name = new Identifier(MOD_ID, "simplex");
		SIMPLEX_CHUNKGEN = FabricChunkGeneratorType.register(name, SimplexChunkGenerator::new, OverworldChunkGeneratorConfig::new, false);
		SIMPLEX_LEVELGEN = LevelGeneratorFactory.create(name, SIMPLEX_CHUNKGEN, (world ->
						new SimplexBiomeSource(new SimplexBiomeSourceConfig(world.getLevelProperties()))));
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
				addTraverseBiomes();
				System.out.println("Traverse biomes registered!");
			}

			if (FabricLoader.getInstance().isModLoaded("terrestria")) {
				addTerrestriaBiomes();
				System.out.println("Terrestria biomes registered!");
			}
		}

		addVanillaLowlands();
		addVanillaMidlands();
		addVanillaHighlands();
		addVanillaMountainPeaks();
	}

	public static void addTerrestriaBiomes() {
		double cypressForestWeight = 1.0;
		double denseWoodlandsWeight = 0.8;
		double cypressSwampWeight = 1.0;
		double hemlockRainforestWeight = 0.7;
		double mapleForestWeight = 0.9;
		double sakuraForestWeight = 0.9;
		double redwoodRainforestWeight = 0.85;
		double rainbowRainforestWeight = 0.9;
		double redwoodForestWeight = 1.0;
		double snowyhemlockForestWeight = 1.0;

		//Lowlands biomes

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.TEMPERATE, cypressForestWeight*0.9);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.LUSH_TEMPERATE, cypressForestWeight);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.DRY_TEMPERATE, cypressForestWeight*0.5);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.TEMPERATE, denseWoodlandsWeight*0.9);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.LUSH_TEMPERATE, denseWoodlandsWeight*0.65);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.DRY_TEMPERATE, denseWoodlandsWeight*0.8);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.TROPICAL, cypressSwampWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.LUSH_TROPICAL, cypressSwampWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.DRY_TROPICAL, cypressSwampWeight*0.75);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.BOREAL, hemlockRainforestWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.LUSH_BOREAL, hemlockRainforestWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.DRY_BOREAL, hemlockRainforestWeight*0.75);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.TEMPERATE, redwoodRainforestWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.LUSH_TEMPERATE, redwoodRainforestWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.DRY_TEMPERATE, redwoodRainforestWeight*0.75);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "rainbow_rainforest"), SimplexClimate.LUSH_TROPICAL, rainbowRainforestWeight*1.25);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.TEMPERATE, redwoodForestWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.LUSH_TEMPERATE, redwoodForestWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.DRY_TEMPERATE, redwoodForestWeight*0.75);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.TEMPERATE, mapleForestWeight*0.9);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.LUSH_TEMPERATE, mapleForestWeight*0.65);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.DRY_TEMPERATE, mapleForestWeight*0.8);

		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.TEMPERATE, sakuraForestWeight*0.9);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.LUSH_TEMPERATE, sakuraForestWeight*0.65);
		SimplexBiomes.addLowlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.DRY_TEMPERATE, sakuraForestWeight*0.8);

		//Midlands biomes

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.TEMPERATE, denseWoodlandsWeight*0.9);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.LUSH_TEMPERATE, denseWoodlandsWeight);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_forest"), SimplexClimate.DRY_TEMPERATE, denseWoodlandsWeight*0.5);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.TEMPERATE, denseWoodlandsWeight*0.9);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.LUSH_TEMPERATE, denseWoodlandsWeight*0.65);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "dense_woodlands"), SimplexClimate.DRY_TEMPERATE, denseWoodlandsWeight*0.8);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.TROPICAL, cypressSwampWeight*0.5);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.LUSH_TROPICAL, cypressSwampWeight*0.75);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "cypress_swamp"), SimplexClimate.DRY_TROPICAL, cypressSwampWeight*0.25);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.BOREAL, hemlockRainforestWeight*0.75);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.LUSH_BOREAL, hemlockRainforestWeight*1);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "hemlock_rainforest"), SimplexClimate.DRY_BOREAL, hemlockRainforestWeight*0.5);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.TEMPERATE, redwoodRainforestWeight*0.75);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.LUSH_TEMPERATE, redwoodRainforestWeight*1);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "lush_redwood_forest"), SimplexClimate.DRY_TEMPERATE, redwoodRainforestWeight*0.5);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "rainbow_rainforest"), SimplexClimate.LUSH_TROPICAL, rainbowRainforestWeight*1.25);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.TEMPERATE, redwoodForestWeight*0.75);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.LUSH_TEMPERATE, redwoodForestWeight*1);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "redwood_forest"), SimplexClimate.DRY_TEMPERATE, redwoodForestWeight*0.5);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.TEMPERATE, mapleForestWeight*0.9);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.LUSH_TEMPERATE, mapleForestWeight*0.65);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "japanese_maple_forest"), SimplexClimate.DRY_TEMPERATE, mapleForestWeight*0.8);

		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.TEMPERATE, sakuraForestWeight*0.9);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.LUSH_TEMPERATE, sakuraForestWeight*0.65);
		SimplexBiomes.addMidlandsBiome(new Identifier("terrestria", "sakura_forest"), SimplexClimate.DRY_TEMPERATE, sakuraForestWeight*0.8);

		//Highlands biomes
		SimplexBiomes.addHighlandsBiome(new Identifier("terrestria", "snowy_hemlock_forest"), SimplexClimate.TEMPERATE, snowyhemlockForestWeight*0.9);
		SimplexBiomes.addHighlandsBiome(new Identifier("terrestria", "snowy_hemlock_forest"), SimplexClimate.LUSH_TEMPERATE, snowyhemlockForestWeight*0.65);
		SimplexBiomes.addHighlandsBiome(new Identifier("terrestria", "snowy_hemlock_forest"), SimplexClimate.DRY_TEMPERATE, snowyhemlockForestWeight*0.8);

		//replacements

		SimplexBiomes.addReplacementBiome(biomeId(Biomes.JUNGLE), new Identifier("terrestria", "volcanic_island"), 10);
	}

	public static void addTraverseBiomes() {
		double woodLandsWeight = 1.0;
		double rollingHillsWeight = 0.6;
		double plateauWeight = 0.5; //Both plateaus
		double aridHighlandsWeight = 1.0;
		double cliffsWeight = 1.0;
		double coniferousForestWeight = 1.0;
		double lushSwampWeight = 1.0;
		double shrublandWeight = 1.0;
		double miniJungleWeight = 1.0;

		//Lowlands Biomes

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.TEMPERATE, woodLandsWeight*0.9);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.LUSH_TEMPERATE, woodLandsWeight*0.5);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.DRY_TEMPERATE, woodLandsWeight);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "desert_shrubland"), SimplexClimate.TEMPERATE, shrublandWeight*0.9);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "desert_shrubland"), SimplexClimate.DRY_TEMPERATE, shrublandWeight);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.TEMPERATE, rollingHillsWeight*0.75);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.LUSH_TEMPERATE, rollingHillsWeight*0.5);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.TROPICAL, rollingHillsWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.LUSH_TROPICAL, rollingHillsWeight*1.25);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "meadow"), SimplexClimate.TEMPERATE, rollingHillsWeight*0.75);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "meadow"), SimplexClimate.LUSH_TEMPERATE, rollingHillsWeight*0.5);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.TEMPERATE, plateauWeight*0.8);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.TEMPERATE, plateauWeight*0.8);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.DRY_TEMPERATE, plateauWeight*0.65);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.DRY_TEMPERATE, plateauWeight*0.65);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.LUSH_TEMPERATE, plateauWeight*0.65);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.LUSH_TEMPERATE, plateauWeight*0.65);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "arid_highlands"), SimplexClimate.DRY_TEMPERATE, aridHighlandsWeight);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "arid_highlands"), SimplexClimate.TEMPERATE, aridHighlandsWeight*0.8);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "arid_highlands"), SimplexClimate.LUSH_TEMPERATE, aridHighlandsWeight*0.6);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.TROPICAL, lushSwampWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.LUSH_TROPICAL, lushSwampWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.DRY_TROPICAL, lushSwampWeight*0.75);

		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "mini_jungle"), SimplexClimate.TROPICAL, miniJungleWeight*1);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "mini_jungle"), SimplexClimate.LUSH_TROPICAL, miniJungleWeight*1.25);
		SimplexBiomes.addLowlandsBiome(new Identifier("traverse", "mini_jungle"), SimplexClimate.DRY_TROPICAL, miniJungleWeight*0.75);

		//Midlands Biomes

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.TEMPERATE, woodLandsWeight);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.LUSH_TEMPERATE, woodLandsWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "woodlands"), SimplexClimate.DRY_TEMPERATE, woodLandsWeight*1.2);

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.TEMPERATE, rollingHillsWeight*0.8);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.LUSH_TEMPERATE, rollingHillsWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.TROPICAL, rollingHillsWeight*1.2);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "rolling_hills"), SimplexClimate.LUSH_TROPICAL, rollingHillsWeight*1.4);

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "meadow"), SimplexClimate.TEMPERATE, rollingHillsWeight*0.8);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "meadow"), SimplexClimate.LUSH_TEMPERATE, rollingHillsWeight*0.6);

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.TEMPERATE, plateauWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.TEMPERATE, plateauWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.DRY_TEMPERATE, plateauWeight*0.55);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.DRY_TEMPERATE, plateauWeight*0.55);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "wooded_plateau"), SimplexClimate.LUSH_TEMPERATE, plateauWeight*0.65);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "plains_plateau"), SimplexClimate.LUSH_TEMPERATE, plateauWeight*0.65);

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "coniferous_forest"), SimplexClimate.DRY_BOREAL, coniferousForestWeight*0.6);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "coniferous_forest"), SimplexClimate.BOREAL, coniferousForestWeight);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "coniferous_forest"), SimplexClimate.LUSH_BOREAL, coniferousForestWeight*1.3);

		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.TROPICAL, lushSwampWeight*0.5);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.LUSH_TROPICAL, lushSwampWeight*0.75);
		SimplexBiomes.addMidlandsBiome(new Identifier("traverse", "lush_swamp"), SimplexClimate.DRY_TROPICAL, lushSwampWeight*0.25);

		//Highlands Biomes
		SimplexBiomes.addHighlandsBiome(new Identifier("traverse", "cliffs"), SimplexClimate.BOREAL, cliffsWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("traverse", "cliffs"), SimplexClimate.DRY_BOREAL, cliffsWeight);

		SimplexBiomes.addHighlandsBiome(new Identifier("traverse", "snowy_coniferous_forest"), SimplexClimate.DRY_BOREAL, coniferousForestWeight*0.6);
		SimplexBiomes.addHighlandsBiome(new Identifier("traverse", "snowy_coniferous_forest"), SimplexClimate.BOREAL, coniferousForestWeight);
		SimplexBiomes.addHighlandsBiome(new Identifier("traverse", "snowy_coniferous_forest"), SimplexClimate.LUSH_BOREAL, coniferousForestWeight*1.3);
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
		final double swampWeight = 1.3;
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
		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.LUSH_TROPICAL, swampWeight / 2);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.DESERT), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TEMPERATE, desertWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TROPICAL, desertWeight*0.75);

		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.DRY_TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SAVANNA), SimplexClimate.DRY_TROPICAL, savannahWeight);
		SimplexBiomes.addLowlandsBiome(biomeId(Biomes.SAVANNA), SimplexClimate.DRY_TEMPERATE, savannahWeight*0.8);

		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.TEMPERATE, swampWeight);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(FOREST, SimplexClimate.TEMPERATE, forestWeight * 0.8);

		SimplexBiomes.addLowlandsBiome(SWAMP, SimplexClimate.LUSH_TEMPERATE, swampWeight);
		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.LUSH_TEMPERATE, plainsWeight);
		SimplexBiomes.addLowlandsBiome(FOREST, SimplexClimate.LUSH_TEMPERATE, forestWeight * 1.5);

		SimplexBiomes.addLowlandsBiome(PLAINS, SimplexClimate.DRY_BOREAL, plainsWeight);
		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.DRY_BOREAL, taigaWeight);

		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.BOREAL, taigaWeight);
		SimplexBiomes.addLowlandsBiome(BIRCH_FOREST, SimplexClimate.BOREAL, birchForestWeight);

		SimplexBiomes.addLowlandsBiome(TAIGA, SimplexClimate.LUSH_BOREAL, taigaWeight);

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

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TEMPERATE, mesaWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addMidlandsBiome(JUNGLE, SimplexClimate.TROPICAL, jungleWeight*0.8);
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
		SimplexBiomes.addHighlandsBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, plainsWeight);

		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);

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
		SimplexBiomes.addHighlandsBiome(JUNGLE, SimplexClimate.LUSH_TROPICAL, jungleWeight);
		SimplexBiomes.addMountainPeaksBiome(GRAVELLY_MOUNTAINS, SimplexClimate.TROPICAL, gravellyMountainsWeight);

		SimplexBiomes.addMountainPeaksBiome(PLAINS, SimplexClimate.LUSH_TROPICAL, forestWeight);
		SimplexBiomes.addMidlandsBiome(biomeId(Biomes.BADLANDS), SimplexClimate.DRY_TROPICAL, mesaWeight*0.75);
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

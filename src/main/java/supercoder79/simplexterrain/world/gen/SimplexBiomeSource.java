package supercoder79.simplexterrain.world.gen;

import com.google.common.collect.ImmutableList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import supercoder79.simplexterrain.SimplexTerrain;
import supercoder79.simplexterrain.api.Heightmap;
import supercoder79.simplexterrain.noise.gradient.OpenSimplexNoise;
import supercoder79.simplexterrain.world.BiomeData;
import supercoder79.simplexterrain.world.biomelayers.SimplexBiomeLayers;

public class SimplexBiomeSource extends BiomeSource {
	public static final Codec<SimplexBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter(source -> source.biomeRegistry),
			Codec.LONG.fieldOf("seed").stable().forGetter(source -> source.seed))
			.apply(instance, instance.stable(SimplexBiomeSource::new)));
	private final BiomeLayerSampler lowlandsSampler;
	private final BiomeLayerSampler midlandsSampler;
	private final BiomeLayerSampler highlandsSampler;
	private final BiomeLayerSampler toplandsSampler;
	private final BiomeLayerSampler beachSampler;
	private final BiomeLayerSampler oceanSampler;
	private final BiomeLayerSampler deepOceanSampler;

	private final OpenSimplexNoise beachStartSampler;

	private final Registry<Biome> biomeRegistry;
	private final long seed;

	private Heightmap heightmap = Heightmap.NONE;
	private Heightmap continent = Heightmap.NONE;

	public SimplexBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(VanillaLayeredBiomeSource.BIOMES.stream().map((registryKey) -> () -> (Biome)biomeRegistry.getOrThrow(registryKey)));
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;

		BiomeLayerSampler[] biomeLayerSamplers = SimplexBiomeLayers.build(biomeRegistry, seed);

		this.lowlandsSampler = biomeLayerSamplers[0];
		this.midlandsSampler = biomeLayerSamplers[1];
		this.highlandsSampler = biomeLayerSamplers[2];
		this.toplandsSampler = biomeLayerSamplers[3];
		this.beachSampler = biomeLayerSamplers[4];
		this.oceanSampler = biomeLayerSamplers[5];
		this.deepOceanSampler = biomeLayerSamplers[6];

		this.beachStartSampler = new OpenSimplexNoise(seed + 12);
	}

	public void setHeightmap(Heightmap heightmap) {
		this.heightmap = heightmap;
	}

	public void setContinentHeightmap(Heightmap heightmap) {
		this.continent = heightmap;
	}

	@Override
	public Biome getBiomeForNoiseGen(int x, int y, int z) {
		if (heightmap == null) return BuiltinBiomes.PLAINS;
		Biome biome = getBiomeAt(x, z, heightmap.getBiomeData(x << 2, z << 2));
		return biome == null ? BuiltinBiomes.PLAINS : biome;
	}

	public Biome getBiomeAt(int x, int z, BiomeData data) {
		int height = data.getHeight();

		int continent = this.continent.getHeight(x << 2, z << 2);

		if (continent < SimplexTerrain.CONFIG.seaLevel) {
			return this.biomeRegistry.get(BiomeKeys.OCEAN);
		}

		return this.biomeRegistry.get(BiomeKeys.PLAINS);
	}

	@Override
	protected Codec<? extends BiomeSource> getCodec() {
		return CODEC;
	}

	@Override
	public BiomeSource withSeed(long l) {
		return new SimplexBiomeSource(this.biomeRegistry, l);
	}
}

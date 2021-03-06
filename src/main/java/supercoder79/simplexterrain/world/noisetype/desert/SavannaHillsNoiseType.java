package supercoder79.simplexterrain.world.noisetype.desert;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.ChunkRandom;
import supercoder79.simplexterrain.api.noise.OctaveNoiseSampler;
import supercoder79.simplexterrain.noise.gradient.OpenSimplexNoise;
import supercoder79.simplexterrain.world.BiomeData;
import supercoder79.simplexterrain.world.noisetype.NoiseType;

public class SavannaHillsNoiseType implements NoiseType {
    private OctaveNoiseSampler<OpenSimplexNoise> noise;
    private OpenSimplexNoise ridged;

    @Override
    public void init(ChunkRandom random) {
        this.noise = new OctaveNoiseSampler<>(OpenSimplexNoise.class, random, 3, 220, 30, 16);
        this.ridged = new OpenSimplexNoise(random.nextLong());
    }

    @Override
    public double modify(int x, int z, double currentNoiseValue, double weight, BiomeData data) {
        double ridgedNoise = Math.abs(1 - this.ridged.sample(x / 100.0, z / 100.0)) * 12 * weight;
        double addition = MathHelper.lerp(MathHelper.perlinFade(weight), 0, 10);
        double noise = this.noise.sample(x, z) * weight;

        return addition + noise + ridgedNoise;
    }

    @Override
    public RegistryKey<Biome> modifyBiome(int y, RegistryKey<Biome> existing) {
        return y > (85 - 61) ? BiomeKeys.SAVANNA : existing;
    }
}

package supercoder79.simplexterrain.world.noisetype.plains;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.ChunkRandom;
import supercoder79.simplexterrain.api.noise.OctaveNoiseSampler;
import supercoder79.simplexterrain.noise.gradient.OpenSimplexNoise;
import supercoder79.simplexterrain.world.BiomeData;
import supercoder79.simplexterrain.world.noisetype.NoiseType;

public class MountainsNoiseType implements NoiseType {
    private OctaveNoiseSampler<OpenSimplexNoise> noise;
    private OctaveNoiseSampler<OpenSimplexNoise> noise2;
    private OpenSimplexNoise ridged;

    @Override
    public void init(ChunkRandom random) {
        this.noise = new OctaveNoiseSampler<>(OpenSimplexNoise.class, random, 4, 160, 40, 20);
        this.noise2 = new OctaveNoiseSampler<>(OpenSimplexNoise.class, random, 2, 600, 20, 20);
        this.ridged = new OpenSimplexNoise(random.nextLong());
    }

    @Override
    public double modify(int x, int z, double currentNoiseValue, double weight, BiomeData data) {
        double ridgedNoise = Math.abs(1 - this.ridged.sample(x / 120.0, z / 120.0)) * 20 * weight;
        double addition = MathHelper.lerp(MathHelper.perlinFade(weight), 0, 20);
        double noise = this.noise.sample(x, z) * weight;
        noise += this.noise2.sample(x, z) * weight;

        return 3 + addition + noise + ridgedNoise;
    }

    @Override
    public RegistryKey<Biome> modifyBiome(int y, RegistryKey<Biome> existing) {
        if (y > 100 - 61) {
            return BiomeKeys.TAIGA;
        }

        return y > (85 - 61) ? BiomeKeys.FOREST : existing;
    }
}

package supercoder79.simplexterrain.world.noisetype.plains;

import net.minecraft.world.gen.ChunkRandom;
import supercoder79.simplexterrain.api.noise.OctaveNoiseSampler;
import supercoder79.simplexterrain.noise.gradient.OpenSimplexNoise;
import supercoder79.simplexterrain.world.BiomeData;
import supercoder79.simplexterrain.world.noisetype.NoiseType;

public class PlainsNoiseType implements NoiseType {
    private OctaveNoiseSampler<OpenSimplexNoise> noise;
    @Override
    public void init(ChunkRandom random) {
        this.noise = new OctaveNoiseSampler<>(OpenSimplexNoise.class, random, 4, 140, 12, 12);
    }

    @Override
    public double modify(int x, int z, double currentNoiseValue, double weight, BiomeData data) {
        return 5 + this.noise.sample(x, z);
    }
}

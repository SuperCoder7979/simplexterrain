package supercoder79.simplexterrain.configs;

import supercoder79.simplexterrain.api.noise.NoiseType;

public class ConfigData {
    public String configVersion = "0.3.0";
    public boolean doModCompat = true;
    public boolean addDetailNoise = true;

    public boolean sacrificeAccuracyForSpeed = false;

    public NoiseType noiseGenerator = NoiseType.SIMPLEX;

    public int baseHeight = 100;

    public int baseOctaveAmount = 11;
    public int detailOctaveAmount = 2;
    public int scaleOctaveAmount = 2;

    public double baseNoiseFrequencyCoefficient = 0.75;
    public double baseNoiseSamplingFrequency = 1;

    public double detailAmplitudeHigh = 2;
    public double detailAmplitudeLow = 4;
    public double detailFrequency = 20;

    public double scaleAmplitudeHigh = 0.2;
    public double scaleAmplitudeLow = 0.09;
    public double scaleFrequencyExponent = 10;

    public double detailNoiseThreshold = 0.0;
    public double scaleNoiseThreshold  = -0.02;

    public int lowlandStartHeight = 68;
    public int midlandStartHeight = 90;
    public int highlandStartHeight = 140;
    public int toplandStartHeight = 190;

    public int biomeScaleAmount = 7;
}

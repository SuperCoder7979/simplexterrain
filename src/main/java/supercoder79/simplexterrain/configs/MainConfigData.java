package supercoder79.simplexterrain.configs;

import java.util.ArrayList;
import java.util.List;

import supercoder79.simplexterrain.SimplexTerrain;
import supercoder79.simplexterrain.world.postprocessor.PostProcessors;

public class MainConfigData {
	public String configVersion = SimplexTerrain.VERSION;
	public boolean doModCompat = true;
	public boolean reloadConfigCommand = false;
	public boolean threadedNoiseGeneration = true;
	public boolean deleteLakes = false;
	public boolean generateVanillaCaves = true;
	public boolean simplexNetherGeneration = true;

	public int noiseGenerationThreads = 2;

	// Noise type is resolved later
	// It's a string so javascript can go brr
	public String noiseGenerator = "SIMPLEX";

	public List<PostProcessors> postProcessors = new ArrayList<>();

//	public List<NoiseModifiers> noiseModifiers = Arrays.asList(NoiseModifiers.MOUNTAINS, NoiseModifiers.RIDGES, NoiseModifiers.DETAILS);

	public int mainOctaveAmount = 3;
	public double mainFrequency = 3200.0;
	public double mainAmplitudeHigh = 144.0;
	public double mainAmplitudeLow = 32.0;
	public double mainNetherScale = 70.0;
	public double netherThresholdScale = 28;
	public double netherThresholdAmplitude = 0.125;
	public double netherThresholdBase = 0.25;

	public int lowlandStartHeight = 66;
	public int midlandStartHeight = 90;
	public int highlandStartHeight = 140;
	public int toplandStartHeight = 190;

	public int biomeScaleAmount = 8;

	public double temperatureOffset = 0.0;
	public double humidityOffset = 0.0;

	public int seaLevel = 63;

	public int temperatureOctaveAmount = 1;
	public int humidityOctaveAmount = 2;
	public double temperatureFrequency = 15.0;
	public double humidityFrequency = 11.0;
	public double temperatureAmplitude = 1.2;
	public double humidityAmplitude = 1.2;

	public String[] terrainShape = {"baseShape.js", "mountains.js", "ridges.js", "details.js", "sigmoid.js"};
}

package supercoder79.simplexterrain.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.simplexterrain.SimplexTerrain;
import supercoder79.simplexterrain.world.gen.SimplexChunkGenerator;

import java.net.Proxy;
import java.util.Random;
import java.util.function.Predicate;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void hookConstructor(Thread thread, DynamicRegistryManager.Impl impl, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        if (saveProperties.getGeneratorOptions().getChunkGenerator() instanceof SimplexChunkGenerator) {
            SimplexTerrain.isSimplexEnabled = true;
        } else {
            SimplexTerrain.isSimplexEnabled = false;
        }
    }

    @Redirect(method = "setupSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/BiomeSource;locateBiome(IIIILjava/util/List;Ljava/util/Random;)Lnet/minecraft/util/math/BlockPos;"))
    private static BlockPos fixDumbServerCrash(BiomeSource biomeSource, int x, int y, int z, int radius, List<Biome> biomes, Random random, ServerWorld serverWorld, ServerWorldProperties serverWorldProperties, boolean bl, boolean bl2, boolean bl3) {
        try {
            SimplexChunkGenerator.init(serverWorld.getSeed());
            return biomeSource.locateBiome(x, y, z, radius, biomes, random);
        } catch (Exception e) {
            System.out.println("[Simplex Terrain] If your server stalls here i'm really sorry but you're gonna have to kill it and restart it.");
            System.out.println("[Simplex Terrain] I have no clue why this happens but it does.");
            System.out.println("[Simplex Terrain] I'll try to get it fixed, but for now this is the workaround - SuperCoder79");
            System.out.println("[Simplex Terrain] If somehow this still triggers then I don't even understand anything anymore - Arc'blroth");
            e.printStackTrace();
            return new BlockPos(0, 63, 0);
        }
    }
}

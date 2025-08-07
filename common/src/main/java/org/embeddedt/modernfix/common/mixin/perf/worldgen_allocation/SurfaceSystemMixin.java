package org.embeddedt.modernfix.common.mixin.perf.worldgen_allocation;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.embeddedt.modernfix.world.gen.CachingNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(SurfaceSystem.class)
public class SurfaceSystemMixin {
    @ModifyArg(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;<init>(Lnet/minecraft/world/level/levelgen/SurfaceSystem;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Ljava/util/function/Function;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;)V"), index = 4)
    private Function<BlockPos, Holder<Biome>> createBiomeManagerBoundToChunk(Function<BlockPos, Holder<Biome>> biomeGetter, @Local(ordinal = 0, argsOnly = true) BiomeManager manager, @Local(ordinal = 0, argsOnly = true) ChunkAccess chunk) {
        var chunkPos = chunk.getPos();
        return manager.withDifferentSource(new CachingNoiseBiomeSource(
                manager::getNoiseBiomeAtQuart,
                QuartPos.fromSection(chunkPos.x),
                QuartPos.fromSection(chunkPos.z),
                chunk.getHeight(),
                Math.abs(chunk.getMinBuildHeight())
        ))::getBiome;
    }
}

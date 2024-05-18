package com.adibtw.pistonvolume.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PistonBaseBlock.class)
public class PistonVolumeMixin {
    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "net.minecraft.world.level.Level.playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            method = "triggerEvent(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z", index = 4)
    public float triggerEventSoundArgChange(float f)
    {
        // Set the original volume to zero
        return 0f;
    }

    @Unique
    private static PistonStructureResolver v$LatestResolver = null;

    @Inject(
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z", shift = At.Shift.AFTER),
        method = "moveBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z",
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos2, PistonStructureResolver pistonStructureResolver)
    {
        v$LatestResolver = pistonStructureResolver;
    }

    @Unique
    private static final float PUSH_LIMIT = 12;

    // There's nothing specific about this value, it's just the one that I thought gave the best falloff
    // Lower values make the zero-block case approach zero volume, while higher values approach the max value
    @Unique
    private static final float OFFSET_FACTOR = 24;

    @Unique
    private static final float MAX_VOLUME = 0.5f;

    @Unique
    private static final float MIN_PITCH = 0.55f;

    @Unique
    private static final float PITCHING_FACTOR = 0.25f;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
            method = "triggerEvent(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z")
    public void triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j,  CallbackInfoReturnable<Boolean> cir) {
        int numPushed = 0;
        if (v$LatestResolver != null) {
            numPushed = v$LatestResolver.getToPush().size();
            v$LatestResolver = null; // Reset so that the default value can be used if no blocks get pulled (retraction without blocks)
        }

        // Get volume on a 0-1 scale
        float volume = (numPushed / OFFSET_FACTOR) + (OFFSET_FACTOR - PUSH_LIMIT)/OFFSET_FACTOR;
        volume *= volume;

        float minimumPitch = 1 - volume;
        minimumPitch *= PITCHING_FACTOR;
        minimumPitch = MIN_PITCH / (1 - minimumPitch);

        // Apply max volume
        volume *= MAX_VOLUME;

        level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, volume, level.random.nextFloat() * 0.25F + minimumPitch);
    }
}

package me.flashyreese.mods.sodiumextra.mixin.cloud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
    @WrapMethod(method = "addCloudsPass")
    private void modifyCloudHeight(FrameGraphBuilder frame, CloudStatus cloudStatus, Vec3 cameraPosition, long gameTime, float partialTicks, int cloudColor, float cloudHeight, int cloudRange, Operation<Void> original) {
        // todo: don't force overwrite
        cloudHeight = SodiumExtraClientMod.options().extraSettings.cloudHeight + 0.33F;
        original.call(frame, cloudStatus, cameraPosition, gameTime, partialTicks, cloudColor, cloudHeight, cloudRange);
    }
}

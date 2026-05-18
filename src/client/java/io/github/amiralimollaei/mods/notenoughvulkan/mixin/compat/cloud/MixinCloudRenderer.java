package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.cloud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.vulkanmod.render.sky.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
    @WrapMethod(method = "renderClouds", require = 1)
    private void modifyCloudHeight(float cloudHeight, int cloudColor, double camX, double camY, double camZ, long gameTime, float partialTicks, Operation<Void> original) {
        // todo: don't force overwrite
        original.call((float) SodiumExtraClientMod.options().extraSettings.cloudHeight + 0.33F, cloudColor, camX, camY, camZ, gameTime, partialTicks);
    }
}

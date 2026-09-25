package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod.profiler;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyboardHandler.class, remap = false, priority = 1500)
public class KeyboardHandlerMMixin {
    @TargetHandler(
            mixin = "net.vulkanmod.mixin.profiling.KeyboardHandlerM",
            name = "injOverlayToggle"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void notEnoughVulkan$skipProfilerHardcodedKeybinds(long l, int i, KeyEvent keyEvent, CallbackInfo ci, CallbackInfo ci2) {
        ci2.cancel();
    }
}

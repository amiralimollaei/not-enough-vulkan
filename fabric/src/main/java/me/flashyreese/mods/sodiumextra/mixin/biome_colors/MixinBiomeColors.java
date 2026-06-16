package me.flashyreese.mods.sodiumextra.mixin.biome_colors;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {
    @Inject(method = "getAverageGrassColor", at = @At("RETURN"), cancellable = true)
    private static void grassColor(CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(9551193); // 9551193 5877296
        }
    }

    @Inject(method = "getAverageWaterColor", at = @At("RETURN"), cancellable = true)
    private static void waterColor(CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(4159204);
        }
    }

    @Inject(method = "getAverageFoliageColor", at = @At("RETURN"), cancellable = true)
    private static void foliageColor(CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(5877296);
        }
    }
}

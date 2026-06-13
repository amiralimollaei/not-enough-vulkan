package me.flashyreese.mods.sodiumextra.mixin.sky_colors;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinAtmosphericFogEnvironment {
    @ModifyArg(method = "getBaseColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/AtmosphericFogEnvironment;applyWeatherDarken(IFF)I"), index = 0)
    public int modifySkyColor(int original) {
        if (!SodiumExtraClientMod.options().detailSettings.skyColors) {
            return 7907327;
        }
        return original;
    }
}

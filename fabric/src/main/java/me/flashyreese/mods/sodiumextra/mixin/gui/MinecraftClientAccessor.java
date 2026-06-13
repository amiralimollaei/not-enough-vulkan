package me.flashyreese.mods.sodiumextra.mixin.gui;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {
    @Accessor("fps")
    static int getFPS() {
        return 0;
    }
}

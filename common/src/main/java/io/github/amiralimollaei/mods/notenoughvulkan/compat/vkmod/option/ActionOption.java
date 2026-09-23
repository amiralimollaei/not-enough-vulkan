package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option;

import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.Option;

// we can't extend Option<Void> because we must be able to instantiate the type of the option
public final class ActionOption extends Option<Boolean> {
    private final Runnable action;

    public ActionOption(Component name, Runnable action) {
        super(name, _ -> { }, () -> false, _ -> Component.literal(">"));
        this.action = action;
    }

    void run() {
        this.action.run();
    }

    @Override
    protected OptionWidget<?> createWidget() {
        return new ActionOptionWidget(this);
    }
}

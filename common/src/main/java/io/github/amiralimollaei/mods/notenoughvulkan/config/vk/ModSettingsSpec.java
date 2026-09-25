package io.github.amiralimollaei.mods.notenoughvulkan.config.vk;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.VkModOptionRegistry;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option.ActionOption;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option.KeybindOption;
import io.github.amiralimollaei.mods.notenoughvulkan.config.VkKeybind;
import io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod.AccessorVulkanRangeOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.vulkanmod.config.api.VkModSettingsEntryBuilder;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.PerformanceImpact;
import net.vulkanmod.config.option.RangeOption;
import net.vulkanmod.config.option.SwitchOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Declarative settings DSL on top of VulkanMod's own config API
 * ({@code net.vulkanmod.config.*}). Adds features VulkanMod lacks, such as
 * dependency-driven availability ("enabled provider") and dynamic spans.
 */
public final class ModSettingsSpec implements SettingsState {
    private final Component displayName;
    private final Map<Identifier, OptionSpec<?>> ownOptions = new LinkedHashMap<>();
    private final List<PageSpec> pages = new ArrayList<>();
    private final Set<SettingsSaveHook> saveHooks = new LinkedHashSet<>();
    private final List<Option<?>> liveOptions = new ArrayList<>();
    private Identifier icon = Identifier.parse("not-enough-vulkan:textures/icon.png");

    private ModSettingsSpec(Component displayName) {
        this.displayName = displayName;
    }

    /** Creates a settings spec for a mod. */
    public static ModSettingsSpec create(Component displayName) {
        return new ModSettingsSpec(displayName);
    }

    public ModSettingsSpec icon(Identifier icon) {
        this.icon = icon;
        return this;
    }

    public PageSpec page(Component title) {
        PageSpec page = new PageSpec(title);
        this.pages.add(page);
        return page;
    }

    /**
     * Builds the {@link ModSettingsEntry} consumed by VulkanMod's
     * {@code VkModSettingsFactory} service.
     */
    public ModSettingsEntry build(VkModSettingsEntryBuilder builder) {
        this.liveOptions.clear();
        builder.setModName(this.displayName);
        builder.setIcon(this.icon);
        builder.setOnApply(this::saveChanges);
        builder.setOptionPageSupplier(this::buildPages);
        return builder.build();
    }

    public List<OptionPage> buildPages() {
        this.liveOptions.clear();
        var result = new ArrayList<OptionPage>();
        for (PageSpec page : this.pages) {
            OptionBlock[] blocks = page.groups.stream()
                    .map(group -> {
                        List<Option<?>> options = new ArrayList<>();
                        for (Object entry : group.entries) {
                            if (entry instanceof OptionSpec<?> spec) {
                                options.add(spec.instantiate(this));
                            } else if (entry instanceof ButtonSpec button) {
                                options.add(button.instantiate(this));
                            }
                        }
                        return new OptionBlock(titleText(group.title),
                                options.toArray(Option<?>[]::new));
                    })
                    .toArray(OptionBlock[]::new);
            result.add(new OptionPage(titleText(page.title), blocks));
        }
        return result;
    }

    private static String titleText(Component component) {
        return component == null ? "" : component.getString();
    }

    private void track(Option<?> option) {
        this.liveOptions.add(option);
        option.getWidget();
        option.setOnChange(() -> this.liveOptions.forEach(Option::updateActiveState));
    }

    private void saveChanges() {
        this.saveHooks.forEach(SettingsSaveHook::afterSave);
    }

    @Override
    public boolean readBool(Identifier id) {
        Object value = readValue(id);
        return value instanceof Boolean bool && bool;
    }

    @Override
    public int readInt(Identifier id) {
        Object value = readValue(id);
        return value instanceof Integer integer ? integer : 0;
    }

    @Override
    public <E extends Enum<E>> E readEnum(Identifier id, Class<E> type) {
        Object value = readValue(id);
        return type.isInstance(value) ? type.cast(value) : type.getEnumConstants()[0];
    }

    @Override
    public int maxOf(Identifier id, int fallback) {
        if (VkModOptionRegistry.getOption(id) instanceof RangeOption rangeOption) {
            return ((AccessorVulkanRangeOption) rangeOption).notEnoughVulkan$getMax();
        }
        OptionSpec<?> spec = this.ownOptions.get(id);
        if (spec instanceof SliderSpec slider) {
            return slider.span.max();
        }
        return fallback;
    }

    private Object readValue(Identifier id) {
        Option<?> option = VkModOptionRegistry.getOption(id);
        if (option != null) {
            return option.getNewValue();
        }
        OptionSpec<?> spec = this.ownOptions.get(id);
        return spec == null ? null : spec.currentValue();
    }

    public ToggleSpec toggle(Identifier id) {
        ToggleSpec spec = new ToggleSpec(id);
        this.ownOptions.put(id, spec);
        return spec;
    }

    public SliderSpec slider(Identifier id) {
        SliderSpec spec = new SliderSpec(id);
        this.ownOptions.put(id, spec);
        return spec;
    }

    public <E extends Enum<E>> ChoiceSpec<E> choice(Identifier id, Class<E> type) {
        ChoiceSpec<E> spec = new ChoiceSpec<>(id, type);
        this.ownOptions.put(id, spec);
        return spec;
    }

    public ButtonSpec button() {
        return new ButtonSpec();
    }

    public KeybindSpec keybind(Identifier id) {
        KeybindSpec spec = new KeybindSpec(id);
        this.ownOptions.put(id, spec);
        return spec;
    }

    public abstract static class OptionSpec<V> {
        final Identifier id;
        Component title;
        Function<V, Component> hint;
        boolean enabled = true;
        Function<SettingsState, Boolean> enabledWhen;
        SettingsSaveHook saveHook;
        PerformanceImpact impact;
        V fallbackValue;
        Consumer<V> setter;
        Supplier<V> getter;
        Option<V> live;

        OptionSpec(Identifier id) {
            this.id = id;
        }

        public OptionSpec<V> title(Component title) {
            this.title = title;
            return this;
        }

        public OptionSpec<V> hint(Component hint) {
            this.hint = ignored -> hint;
            return this;
        }

        public OptionSpec<V> hint(Function<V, Component> hint) {
            this.hint = hint;
            return this;
        }

        public OptionSpec<V> enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /** Availability computed from the current option values. */
        public final OptionSpec<V> enabledWhen(Function<SettingsState, Boolean> provider) {
            this.enabledWhen = provider;
            return this;
        }

        public OptionSpec<V> onSave(SettingsSaveHook hook) {
            this.saveHook = hook;
            return this;
        }

        public OptionSpec<V> impact(PerformanceImpact impact) {
            this.impact = impact;
            return this;
        }

        public OptionSpec<V> defaults(V value) {
            this.fallbackValue = value;
            return this;
        }

        public OptionSpec<V> bind(Consumer<V> setter, Supplier<V> getter) {
            this.setter = setter;
            this.getter = getter;
            return this;
        }

        Object currentValue() {
            return this.live != null ? this.live.getNewValue()
                    : this.getter != null ? this.getter.get() : this.fallbackValue;
        }

        Option<V> instantiate(ModSettingsSpec owner) {
            Supplier<V> valueGetter = this.getter != null ? this.getter : () -> this.fallbackValue;
            Consumer<V> applyingSetter = this.setter != null ? this.setter : ignored -> { };
            Option<V> option = this.create(owner, applyingSetter, valueGetter);
            this.live = option;
            if (this.hint != null) {
                option.setTooltip(this.hint);
            }
            if (this.impact != null) {
                option.setImpact(this.impact);
            }
            option.setActivationFn(() -> this.enabled
                    && (this.enabledWhen == null || Boolean.TRUE.equals(this.enabledWhen.apply(owner))));
            if (this.saveHook != null) {
                owner.saveHooks.add(this.saveHook);
            }
            owner.track(option);
            return option;
        }

        abstract Option<V> create(ModSettingsSpec owner, Consumer<V> applyingSetter, Supplier<V> valueGetter);
    }

    public static final class ToggleSpec extends OptionSpec<Boolean> {
        ToggleSpec(Identifier id) {
            super(id);
        }

        @Override
        Option<Boolean> create(ModSettingsSpec owner, Consumer<Boolean> applyingSetter, Supplier<Boolean> valueGetter) {
            return new ResettableSwitch(this.title, applyingSetter, valueGetter,
                    this.fallbackValue != null && this.fallbackValue);
        }
    }

    public static final class KeybindSpec extends OptionSpec<VkKeybind> {
        KeybindSpec(Identifier id) {
            super(id);
        }

        @Override
        Option<VkKeybind> create(ModSettingsSpec owner, Consumer<VkKeybind> applyingSetter, Supplier<VkKeybind> valueGetter) {
            return new KeybindOption(this.title, applyingSetter, valueGetter, this.fallbackValue);
        }
    }

    public static final class SliderSpec extends OptionSpec<Integer> {
        InclusiveIntSpan span = new InclusiveIntSpan(0, 100, 1);
        Function<SettingsState, ? extends InclusiveIntSpan> spanProvider;
        ValueFormatter formatter = ValueFormatter.plainNumber();

        SliderSpec(Identifier id) {
            super(id);
        }

        public SliderSpec span(int min, int max, int step) {
            this.span = new InclusiveIntSpan(min, max, step);
            return this;
        }

        public SliderSpec span(InclusiveIntSpan span) {
            this.span = span;
            return this;
        }

        /** Dynamic span computed when the settings pages are built. */
        public SliderSpec spanProvider(Function<SettingsState, ? extends InclusiveIntSpan> provider) {
            this.spanProvider = provider;
            return this;
        }

        public SliderSpec format(ValueFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        @Override
        Option<Integer> create(ModSettingsSpec owner, Consumer<Integer> applyingSetter, Supplier<Integer> valueGetter) {
            InclusiveIntSpan actual = this.spanProvider != null ? this.spanProvider.apply(owner) : this.span;
            return new ResettableRange(this.title, actual.min(), actual.max(), actual.step(),
                    this.formatter::format, applyingSetter, valueGetter,
                    this.fallbackValue != null ? this.fallbackValue : actual.min());
        }
    }

    public static final class ChoiceSpec<E extends Enum<E>> extends OptionSpec<E> {
        final Class<E> type;
        Collection<E> allowed;
        Function<E, Component> labels;

        ChoiceSpec(Identifier id, Class<E> type) {
            super(id);
            this.type = type;
        }

        public ChoiceSpec<E> allowed(Collection<E> values) {
            this.allowed = values;
            return this;
        }

        public ChoiceSpec<E> labels(Function<E, Component> labels) {
            this.labels = labels;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        Option<E> create(ModSettingsSpec owner, Consumer<E> applyingSetter, Supplier<E> valueGetter) {
            Collection<E> source = this.allowed != null ? this.allowed
                    : Arrays.asList(this.type.getEnumConstants());
            E[] values = source.toArray(size ->
                    (E[]) java.lang.reflect.Array.newInstance(this.type, size));
            ResettableChoice<E> option = new ResettableChoice<>(this.title, values,
                    applyingSetter, valueGetter, this.fallbackValue);
            option.setTranslator(value -> this.labels != null ? this.labels.apply(value)
                    : value instanceof LocalizedName named ? named.getLocalizedName()
                    : Component.literal(value.name()));
            return option;
        }
    }

    public static final class ButtonSpec {
        Component title;
        Component hint;
        boolean enabled = true;
        Runnable action = () -> { };

        public ButtonSpec title(Component title) {
            this.title = title;
            return this;
        }

        public ButtonSpec hint(Component hint) {
            this.hint = hint;
            return this;
        }

        public ButtonSpec enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ButtonSpec onClick(Runnable action) {
            this.action = action;
            return this;
        }

        Option<?> instantiate(ModSettingsSpec owner) {
            ActionOption option = new ActionOption(this.title, this.action);
            if (this.hint != null) {
                option.setTooltip(ignored -> this.hint);
            }
            option.setActivationFn(() -> this.enabled);
            owner.track(option);
            return option;
        }
    }

    public static final class PageSpec {
        final Component title;
        final List<GroupSpec> groups = new ArrayList<>();

        PageSpec(Component title) {
            this.title = title;
        }

        public PageSpec group(Component title, Consumer<GroupSpec> contents) {
            GroupSpec group = new GroupSpec(title);
            contents.accept(group);
            this.groups.add(group);
            return this;
        }
    }

    public static final class GroupSpec {
        final Component title;
        final List<Object> entries = new ArrayList<>();

        GroupSpec(Component title) {
            this.title = title;
        }

        public GroupSpec add(OptionSpec<?> option) {
            this.entries.add(option);
            return this;
        }

        public GroupSpec add(ButtonSpec button) {
            this.entries.add(button);
            return this;
        }
    }

    private static final class ResettableSwitch extends SwitchOption implements ResettableValue {
        private final boolean fallback;

        ResettableSwitch(Component title, Consumer<Boolean> setter, Supplier<Boolean> getter, boolean fallback) {
            super(title, setter, getter);
            this.fallback = fallback;
        }

        @Override
        public boolean resetToDefault() {
            this.setNewValue(this.fallback);
            return true;
        }
    }

    private static final class ResettableRange extends RangeOption implements ResettableValue {
        private final int fallback;

        ResettableRange(Component title, int min, int max, int step,
                Function<Integer, Component> formatter, Consumer<Integer> setter,
                Supplier<Integer> getter, int fallback) {
            super(title, min, max, step, formatter, setter, getter);
            this.fallback = fallback;
        }

        @Override
        public boolean resetToDefault() {
            this.setNewValue(this.fallback);
            return true;
        }
    }

    private static final class ResettableChoice<E> extends CyclingOption<E> implements ResettableValue {
        private final E fallback;

        ResettableChoice(Component title, E[] values, Consumer<E> setter,
                Supplier<E> getter, E fallback) {
            super(title, values, setter, getter);
            this.fallback = fallback;
        }

        @Override
        public boolean resetToDefault() {
            if (this.fallback == null) {
                return false;
            }
            this.setNewValue(this.fallback);
            return true;
        }
    }

    /** Enums that provide their own display label. */
    public interface LocalizedName {
        Component getLocalizedName();
    }
}

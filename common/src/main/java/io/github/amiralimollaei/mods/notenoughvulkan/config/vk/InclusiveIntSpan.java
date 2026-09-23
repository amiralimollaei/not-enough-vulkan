package io.github.amiralimollaei.mods.notenoughvulkan.config.vk;

/**
 * Inclusive integer span with a step, used to validate and render ranged options.
 */
public record InclusiveIntSpan(int min, int max, int step) {
    public InclusiveIntSpan {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive");
        }
    }
}

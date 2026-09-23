package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.bobby;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import de.johni0702.minecraft.bobby.FakeChunkManager;
import net.minecraft.world.level.chunk.LevelChunk;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FakeChunkManager.class, remap = false)
public abstract class FakeChunkManagerMixin {
    @WrapMethod(method = "load", require = 0)
    private void notEnoughVulkan$onFakeChunkLoad(int x, int z, LevelChunk chunk, Operation<Void> original) {
        original.call(x, z, chunk);
        if (ChunkStatusMap.INSTANCE != null) {
            ChunkStatusMap.INSTANCE.setChunkStatus(x, z, ChunkStatusMap.CHUNK_READY);
        }
    }

    @WrapMethod(method = "unload")
    private boolean notEnoughVulkan$onFakeChunkUnload(int x, int z, boolean willBeReplaced, Operation<Boolean> original) {
        boolean unloaded = original.call(x, z, willBeReplaced);
        if (unloaded && ChunkStatusMap.INSTANCE != null) {
            ChunkStatusMap.INSTANCE.resetChunkStatus(x, z, ChunkStatusMap.CHUNK_READY);
        }
        return unloaded;
    }
}

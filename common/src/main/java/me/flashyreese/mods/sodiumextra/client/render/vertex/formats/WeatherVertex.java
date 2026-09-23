package me.flashyreese.mods.sodiumextra.client.render.vertex.formats;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.system.MemoryUtil;

public final class WeatherVertex {
    public static final VertexFormat FORMAT = DefaultVertexFormat.PARTICLE;
    public static final int STRIDE = 28;
    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_TEXTURE = 12;
    private static final int OFFSET_COLOR = 20;
    private static final int OFFSET_LIGHT = 24;

    public static void put(long ptr, float x, float y, float z, float u, float v, int color, int light) {
        putPositionAndTexture(ptr, x, y, z, u, v);
        MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);
        MemoryUtil.memPutInt(ptr + OFFSET_LIGHT, light);
    }

    public static void put(long ptr, float x, float y, float z, float u, float v, int color, int lightU, int lightV) {
        putPositionAndTexture(ptr, x, y, z, u, v);
        MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);
        MemoryUtil.memPutShort(ptr + OFFSET_LIGHT, (short) lightU);
        MemoryUtil.memPutShort(ptr + OFFSET_LIGHT + 2, (short) lightV);
    }

    private static void putPositionAndTexture(long ptr, float x, float y, float z, float u, float v) {
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION, x);
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);
        MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE, u);
        MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 4, v);
    }
}

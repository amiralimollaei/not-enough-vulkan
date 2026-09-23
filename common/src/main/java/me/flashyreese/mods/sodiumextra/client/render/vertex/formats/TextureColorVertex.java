package me.flashyreese.mods.sodiumextra.client.render.vertex.formats;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class TextureColorVertex {
    public static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_TEX_COLOR;

    public static final int STRIDE = 24;

    private static final int OFFSET_POSITION = 0;
    private static final int OFFSET_TEXTURE = 12;
    private static final int OFFSET_COLOR = 20;

    public static void write(long ptr, Matrix4f matrix, float x, float y, float z, int color, float u, float v) {
        float xt = matrix.m00() * x + (matrix.m10() * y + (matrix.m20() * z + matrix.m30()));
        float yt = matrix.m01() * x + (matrix.m11() * y + (matrix.m21() * z + matrix.m31()));
        float zt = matrix.m02() * x + (matrix.m12() * y + (matrix.m22() * z + matrix.m32()));

        write(ptr, xt, yt, zt, color, u, v);
    }

    public static void write(long ptr, float x, float y, float z, int color, float u, float v) {
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION, x);
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 4, y);
        MemoryUtil.memPutFloat(ptr + OFFSET_POSITION + 8, z);


        MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE, u);
        MemoryUtil.memPutFloat(ptr + OFFSET_TEXTURE + 4, v);

        MemoryUtil.memPutInt(ptr + OFFSET_COLOR, color);
    }
}

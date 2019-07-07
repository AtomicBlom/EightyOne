package com.github.atomicblom.eightyone.client.tesrmodels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class CubeVertexBuffer extends VertexBufferFactory {
    private final String bleah = new ResourceLocation("minecraft", "blocks/concrete_white").toString();

    public BufferBuilder create() {
        final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        final TextureAtlasSprite sprite = textureMapBlocks.getAtlasSprite(bleah);

        final BufferBuilder bufferbuilder = new ReusableBufferBuilder(2097152);

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        final float offset = -0.5f;
        final float spacing = (4.0f - (3.0f)) / 2.0f;

        final float minX = -0.5f;
        final float minY = -0.5f;
        final float minZ = -0.5f;

        final float maxZ = 0.5f;
        final float maxY = 0.5f;
        final float maxX = 0.5f;

        final float uA = 0 / 4f * 16;
        final float uB = 1 / 4f * 16;
        final float uC = 2 / 4f * 16;
        final float uD = 3 / 4f * 16;
        final float uE = 4 / 4f * 16;

        final float vA = 3 / 4f * 16;
        final float vB = 2 / 4f * 16;
        final float vC = 1 / 4f * 16;
        final float vD = 0 / 4f * 16;

        Matrix4f transform = new Matrix4f();
        final Vector3f scaleVec = new Vector3f(0.25f, 0.25f, 0.25f);
        final Vector3f translateVec = new Vector3f();

        Vector3f normal;

        normal = normalize(
                new Vector3f(minX, minY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ)
        );
        makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, 16, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, 0, 0, normal.x, normal.y, normal.z);

        normal = normalize(
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, minY, minZ)
        );
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, 0, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, 16, 16, normal.x, normal.y, normal.z);

        normal = normalize(
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, minY, maxZ)
        );
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, 0, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, 16, 16, normal.x, normal.y, normal.z);

        normal = normalize(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ)
        );
        makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, 16, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, 0, 0, normal.x, normal.y, normal.z);

        normal = normalize(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, minY, maxZ)
        );
        makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, 16, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, 0, 0, normal.x, normal.y, normal.z);

        normal = normalize(
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, maxY, minZ)
        );
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, 0, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, 16, 16, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, 16, 0, normal.x, normal.y, normal.z);
        makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, 0, 0, normal.x, normal.y, normal.z);

        return bufferbuilder;
    }
}

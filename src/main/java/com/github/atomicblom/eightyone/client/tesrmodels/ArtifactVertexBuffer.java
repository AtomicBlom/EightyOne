package com.github.atomicblom.eightyone.client.tesrmodels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class ArtifactVertexBuffer extends VertexBufferFactory {
    private String portalTexture;

    public ArtifactVertexBuffer(String portalTexture) {

        this.portalTexture = portalTexture;
    }

    public BufferBuilder create()
    {
        final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        final TextureAtlasSprite sprite = textureMapBlocks.getAtlasSprite(portalTexture);

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


        for (float cx = 0; cx < 3; ++cx)
        {
            for (float cy = 0; cy < 3; ++cy)
            {
                for (float cz = 0; cz < 3; ++cz)
                {

                    Matrix4f.setIdentity(transform);

                    translateVec.set(
                            (cx - 1) + (cx * spacing) + offset,
                            (cy - 1) + (cy * spacing) + offset,
                            (cz - 1) + (cz * spacing) + offset
                    );

                    transform = transform.scale(scaleVec);
                    transform = transform.translate(translateVec);
                    transform = transform.rotate((float)(cx * (Math.PI / 2)), new Vector3f(1.0f, 0.0f, 0.0f));
                    transform = transform.rotate((float)(cy * (Math.PI / 2)), new Vector3f(0.0f, 1.0f, 0.0f));
                    transform = transform.rotate((float)((cz + 1) * (Math.PI / 2)), new Vector3f(0.0f, 0.0f, 1.0f));

                    Vector3f normal;

                    normal = normalize(
                            new Vector3f(minX, minY, maxZ),
                            new Vector3f(maxX, minY, maxZ),
                            new Vector3f(maxX, maxY, maxZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, normal.x, normal.y, normal.z);

                    normal = normalize(
                            new Vector3f(minX, maxY, minZ),
                            new Vector3f(maxX, maxY, minZ),
                            new Vector3f(maxX, minY, minZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uE, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uD, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uD, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uE, vB, normal.x, normal.y, normal.z);

                    normal = normalize(
                            new Vector3f(maxX, maxY, minZ),
                            new Vector3f(maxX, maxY, maxZ),
                            new Vector3f(maxX, minY, maxZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uD, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uD, vB, normal.x, normal.y, normal.z);

                    normal = normalize(
                            new Vector3f(minX, minY, minZ),
                            new Vector3f(minX, minY, maxZ),
                            new Vector3f(minX, maxY, maxZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uA, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uA, vC, normal.x, normal.y, normal.z);

                    normal = normalize(
                            new Vector3f(minX, minY, minZ),
                            new Vector3f(maxX, minY, minZ),
                            new Vector3f(maxX, minY, maxZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uB, vA, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uC, vA, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, normal.x, normal.y, normal.z);

                    normal = normalize(
                            new Vector3f(minX, maxY, maxZ),
                            new Vector3f(maxX, maxY, maxZ),
                            new Vector3f(maxX, maxY, minZ)
                    );
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uC, vD, normal.x, normal.y, normal.z);
                    makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uB, vD, normal.x, normal.y, normal.z);
                }
            }
        }
        return bufferbuilder;
    }
}

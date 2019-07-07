package com.github.atomicblom.eightyone.client.tesrmodels;

import com.github.atomicblom.eightyone.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class PortalFrameVertexBuffer extends VertexBufferFactory {
    private final String portalTexture_frame = new ResourceLocation(Reference.MOD_ID, "blocks/portal_frame").toString();

    public BufferBuilder create() {
        final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        final TextureAtlasSprite sprite = textureMapBlocks.getAtlasSprite(portalTexture_frame);

        final BufferBuilder bufferbuilder = new ReusableBufferBuilder(2097152);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        final Matrix4f transform = new Matrix4f();
        Matrix4f.setIdentity(transform);

        final float minX = -4;
        final float maxX = 5;
        final float minZ = -4;
        final float maxZ = 5;
        final float minY = -2;
        final float maxY = 4;

        Vector3f normal = normalize(
                new Vector3f(minX, minY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ)
        );

        for(final float z : new float[] { minZ + 1 + 0.01f, maxZ + 0.01f})
        {
            for (float x = minX; x < maxX; ++x)
            {
                makeVertex(transform, bufferbuilder, sprite, x, minY, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, minY, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, minY + 1, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, maxY - 1, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, maxY, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY, z, 16, 16, normal.x, normal.y, normal.z);
            }

            for (float y = minY; y < maxY; ++y)
            {
                makeVertex(transform, bufferbuilder, sprite, minX, y, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y + 1, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX, y + 1, z, 0, 16, normal.x, normal.y, normal.z);


                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y + 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y + 1, z, 16, 16, normal.x, normal.y, normal.z);
            }
        }

        normal = normalize(
                new Vector3f(minX + 1, minY, minZ),
                new Vector3f(maxX - 1, minY, minZ),
                new Vector3f(maxX + 1, minY + 1, minZ)
        );
        for (final float z : new float[] { minZ - 0.01f, maxZ - 1 - 0.01f})
        {
            for (float x = minX; x < maxX; ++x)
            {
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, minY + 1, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, minY, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY, z, 16, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, maxY, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, maxY, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, maxY - 1, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z, 0, 16, normal.x, normal.y, normal.z);
            }
            for (float y = minY; y < maxY; ++y)
            {
                makeVertex(transform, bufferbuilder, sprite, minX, y + 1, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y + 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX, y, z, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y + 1, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y + 1, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z, 16, 16, normal.x, normal.y, normal.z);
            }
        }

        normal = normalize(
                new Vector3f(minX, minY + 1, minZ),
                new Vector3f(minX, minY + 1, maxZ - 1),
                new Vector3f(minX, minY, maxZ - 1)
        );
        for(final float x : new float[] { minX + 1 + 0.01f, maxX + 0.01f})
        {
            for (float z = minZ; z < maxZ; ++z)
            {
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY, z + 1, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY, z, 16, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, maxY, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY, z + 1, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z, 0, 0, normal.x, normal.y, normal.z);
            }
            for (float y = minY; y < maxY; ++y)
            {
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, minZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, minZ + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, y + 1, maxZ - 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, maxZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ - 1, 16, 0, normal.x, normal.y, normal.z);
            }
        }

        normal = normalize(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ)
        );

        for(final float x : new float[] { minX - 0.01f, maxX - 1 - 0.01f})
        {
            for (float z = minZ; z < maxZ; ++z)
            {
                makeVertex(transform, bufferbuilder, sprite, x, minY, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY, z + 1, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, minY + 1, z, 0, 0, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY - 1, z + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY, z + 1, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, maxY, z, 16, 16, normal.x, normal.y, normal.z);
            }
            for (float y = minY; y < maxY; ++y)
            {
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, minZ + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, minZ, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ - 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, maxZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y + 1, maxZ - 1, 16, 0, normal.x, normal.y, normal.z);
            }
        }

        /////////////////// Bottom //////////////////////
        normal = normalize(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, minY, maxZ)
        );
        for(final float y : new float[] { minY - 0.01f, maxY - 1 - 0.01f })
        {
            for (float z = minZ; z < maxZ; ++z)
            {
                makeVertex(transform, bufferbuilder, sprite, minX, y, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX, y, z + 1, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z + 1, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z + 1, 16, 0, normal.x, normal.y, normal.z);
            }
            for (float x = minX; x < maxX; ++x)
            {
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, minZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, minZ + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ + 1, 0, 0, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ - 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, maxZ - 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, maxZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ, 16, 16, normal.x, normal.y, normal.z);
            }
        }

        ////////////////////////TOP/////////////////////////
        normal = normalize(
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, maxY, minZ)
        );
        for(final float y : new float[] { minY + 1 + 0.01f, maxY + 0.01f})
        {
            for (float z = minZ; z < maxZ; ++z)
            {
                makeVertex(transform, bufferbuilder, sprite, minX, y, z + 1, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX + 1, y, z, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, minX, y, z, 0, 16, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z + 1, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX, y, z, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, maxX - 1, y, z, 16, 0, normal.x, normal.y, normal.z);
            }
            for (float x = minX; x < maxX; ++x)
            {
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, maxZ, 16, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, maxZ - 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, maxZ - 1, 0, 0, normal.x, normal.y, normal.z);

                makeVertex(transform, bufferbuilder, sprite, x, y, minZ + 1, 16, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, minZ + 1, 0, 0, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x + 1, y, minZ, 0, 16, normal.x, normal.y, normal.z);
                makeVertex(transform, bufferbuilder, sprite, x, y, minZ, 16, 16, normal.x, normal.y, normal.z);
            }
        }
        return bufferbuilder;
    }
}

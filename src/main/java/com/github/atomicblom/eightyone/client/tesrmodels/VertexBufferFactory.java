package com.github.atomicblom.eightyone.client.tesrmodels;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public abstract class VertexBufferFactory {

    public abstract BufferBuilder create();

    protected Vector3f normalize(Vector3f v1, Vector3f v2, Vector3f v3)
    {
        final Vector3f edge1 = Vector3f.sub(v2, v1, new Vector3f());
        final Vector3f edge2 = Vector3f.sub(v3, v1, new Vector3f());
        final Vector3f cross = Vector3f.cross(edge1, edge2, new Vector3f());
        return cross.normalise(new Vector3f());
    }

    private final Vector4f untransformed = new Vector4f(0, 0, 0, 1);
    private final Vector4f pos = new Vector4f();
    private final Vector4f normal = new Vector4f();

    protected void makeVertex(Matrix4f transform, BufferBuilder bufferbuilder, TextureAtlasSprite sprite, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ)
    {
        untransformed.set(x, y, z);
        Matrix4f.transform(transform, untransformed, pos);

        untransformed.set(normalX, normalY, normalZ);
        Matrix4f.transform(transform, untransformed, normal);

        bufferbuilder
                .pos(pos.x, pos.y, pos.z)
                .tex(sprite.getInterpolatedU(u), sprite.getInterpolatedV(v))
                .normal(normal.x, normal.y, normal.z)
                .endVertex();
    }
}

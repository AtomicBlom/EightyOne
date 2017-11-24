package com.github.atomicblom.eightyone.client;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class PortalTESR extends TileEntitySpecialRenderer<PortalTileEntity>
{
	String portalTexture = new ResourceLocation(Reference.MOD_ID, "blocks/portal3").toString();
	private BufferBuilder buffer;

	@Override
	public void render(PortalTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alphalpha)
	{

		if (buffer == null) {
			buffer = createBuffer();
		}
		WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		final float yRotation = te.getYRotation() + partialTicks;
		te.setYRotation(yRotation);

		final long currentPulse = Minecraft.getSystemTime() / 3000;
		if (te.getPulse() != currentPulse) {
			te.setPulse(currentPulse);
			te.setPulseRotation(te.getYRotation());
		}
		final long currentPulseTime = Minecraft.getSystemTime() % 3000;
		final float pulseRotation = te.getPulseRotation() + partialTicks / 2;
		te.setPulseRotation(pulseRotation);

		float scale = 1;

		//calculate heartbeat
		final int shrinkStart = 2900;
		final int shrinkEnd = 2950;
		final int shrinkLength = shrinkEnd - shrinkStart;
		final int expandStart = shrinkEnd;
		final int expandEnd = 3000;
		final int expandLength = expandEnd - expandStart;
		final float expansionFactor = 50.0f;

		if (currentPulseTime >= shrinkStart && currentPulseTime < shrinkEnd) {
			scale = 1 - (currentPulseTime - shrinkStart) / (float)shrinkLength / expansionFactor;
		}
		if (currentPulseTime >= expandStart && currentPulseTime < expandEnd) {
			scale = 1 - (expandLength - (currentPulseTime - expandStart)) / (float)expandLength / expansionFactor;
		}

		//Solid Render Pass
		renderRotatingPortal(uploader, yRotation, scale, 1.0f);

		scale = 1 + currentPulseTime / 3000.0f;
		final float alpha = 1 - (currentPulseTime / 1000.0f);
		if (alpha > 0)
		{
			//Translucent Render Pass A - fill Z-Buffer
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.colorMask(false, false, false, false);

			renderRotatingPortal(uploader, pulseRotation, scale, alpha);

			//Translucent Render Pass B - Render portal
			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0f);
			GlStateManager.colorMask(true, true, true, true);
			renderRotatingPortal(uploader, pulseRotation, scale, alpha);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}
		GlStateManager.popMatrix();
	}

	private void renderRotatingPortal(WorldVertexBufferUploader uploader, float yRotation, float scale, float alpha)
	{
		GlStateManager.pushMatrix();

		GlStateManager.translate(0.5, 0.5, 0.5);
		GlStateManager.rotate(yRotation, 0, 1, 0);
		GlStateManager.rotate(yRotation / 2, 1, 0, 0);
		GlStateManager.scale(scale, scale, scale);

		GlStateManager.color(
				(alpha / 4) + 0.75f,
				1.0F,
				(alpha / 4) + 0.75f,
				alpha);

		uploader.draw(buffer);

		GlStateManager.popMatrix();
	}

	private BufferBuilder createBuffer()
	{
		final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
		final TextureAtlasSprite sprite = textureMapBlocks.getAtlasSprite(portalTexture);

		final BufferBuilder bufferbuilder = new ReusableBufferBuilder(2097152);

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

		float offset = -0.5f;
		float spacing = (4.0f - (3.0f)) / 2.0f;

		float minX = -0.5f;
		float minY = -0.5f;
		float minZ = -0.5f;

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

	private Vector3f normalize(Vector3f v1, Vector3f v2, Vector3f v3)
	{
		final Vector3f edge1 = Vector3f.sub(v2, v1, new Vector3f());
		final Vector3f edge2 = Vector3f.sub(v3, v1, new Vector3f());
		final Vector3f cross = Vector3f.cross(edge1, edge2, new Vector3f());
		return cross.normalise(new Vector3f());
	}

	private final Vector4f untransformed = new Vector4f(0, 0, 0, 1);
	private final Vector4f pos = new Vector4f();
	private final Vector4f normal = new Vector4f();

	private void makeVertex(Matrix4f transform, BufferBuilder bufferbuilder, TextureAtlasSprite sprite, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ)
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

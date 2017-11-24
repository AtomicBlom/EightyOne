package com.github.atomicblom.eightyone.client;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
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

	@Override
	public void render(PortalTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alphalpha)
	{

		final TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
		final TextureAtlasSprite sprite = textureMapBlocks.getAtlasSprite(portalTexture);
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
		renderRotatingPortal(te, sprite, yRotation, scale, 1.0f);

		//Translucent Render Pass A
		scale = 1 + currentPulseTime / 3000.0f;
		float alpha = 1 - (currentPulseTime / 1000.0f);

		if (alpha > 0)
		{
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.colorMask(false, false, false, false);

			renderRotatingPortal(te, sprite, pulseRotation, scale, alpha);

			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.colorMask(true, true, true, true);
			renderRotatingPortal(te, sprite, pulseRotation, scale, alpha);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}
		GlStateManager.popMatrix();
	}

	private void renderRotatingPortal(PortalTileEntity te, TextureAtlasSprite sprite, float yRotation, float scale, float alpha)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5, 0.5, 0.5);

		GlStateManager.rotate(yRotation, 0, 1, 0);
		GlStateManager.rotate(yRotation / 2, 1, 0, 0);

		GlStateManager.scale(scale, scale, scale);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		float r = (alpha / 4) + 0.75f;
		float g = 1;
		float b = (alpha / 4) + 0.75f;
		float a = alpha;

		float cubeSize = 1f;

		float offset = -0.5f;
		float spacing = (4 - (cubeSize * 3)) / 2;

		for (float cx = 0; cx < 3; ++cx)
		{
			for (float cy = 0; cy < 3; ++cy)
			{
				for (float cz = 0; cz < 3; ++cz)
				{

					float minXa = (cx - 1) + (cx * spacing) + offset;// / 3.0f + cx * offset;
					float minYa = (cy - 1) + (cy * spacing) + offset;// / 3.0f + cy * offset;
					float minZa = (cz - 1) + (cz * spacing) + offset;// / 3.0f + cz * offset;

					float minX = -0.5f;
					float minY = -0.5f;
					float minZ = -0.5f;

					final float maxZ = minZ + 1;
					final float maxY = minY + 1;
					final float maxX = minX + 1;

					final float uA = 0 / 4f * 16;
					final float uB = 1 / 4f * 16;
					final float uC = 2 / 4f * 16;
					final float uD = 3 / 4f * 16;
					final float uE = 4 / 4f * 16;

					final float vA = 3 / 4f * 16;
					final float vB = 2 / 4f * 16;
					final float vC = 1 / 4f * 16;
					final float vD = 0 / 4f * 16;

					Matrix4f transform = Matrix4f.setIdentity(new Matrix4f());

					transform = transform.scale(new Vector3f(0.25f, 0.25f, 0.25f));
					transform = transform.translate(new Vector3f(minXa, minYa, minZa));

					transform = transform.rotate((float)(cx * (Math.PI / 2)), new Vector3f(1.0f, 0.0f, 0.0f));
					transform = transform.rotate((float)(cy * (Math.PI / 2)), new Vector3f(0.0f, 1.0f, 0.0f));
					transform = transform.rotate((float)((cz + 1) * (Math.PI / 2)), new Vector3f(0.0f, 0.0f, 1.0f));


					makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, r, g, b, a, 0, 0, -1);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, r, g, b, a, 0, 0, -1);
					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, r, g, b, a, 0, 0, -1);
					makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, r, g, b, a, 0, 0, -1);

					makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uE, vC, r, g, b, a, 0, 0, 1);
					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uD, vC, r, g, b, a, 0, 0, 1);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uD, vB, r, g, b, a, 0, 0, 1);
					makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uE, vB, r, g, b, a, 0, 0, 1);

					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uD, vC, r, g, b, a, -1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, r, g, b, a, -1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, r, g, b, a, -1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uD, vB, r, g, b, a, -1, 0, 0);

					makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uA, vB, r, g, b, a, 1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, r, g, b, a, 1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, r, g, b, a, 1, 0, 0);
					makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uA, vC, r, g, b, a, 1, 0, 0);

					makeVertex(transform, bufferbuilder, sprite, minX, minY, minZ, uB, vA, r, g, b, a, 0, -1, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, minZ, uC, vA, r, g, b, a, 0, -1, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, minY, maxZ, uC, vB, r, g, b, a, 0, -1, 0);
					makeVertex(transform, bufferbuilder, sprite, minX, minY, maxZ, uB, vB, r, g, b, a, 0, -1, 0);

					makeVertex(transform, bufferbuilder, sprite, minX, maxY, maxZ, uB, vC, r, g, b, a, 0, 1, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, maxZ, uC, vC, r, g, b, a, 0, 1, 0);
					makeVertex(transform, bufferbuilder, sprite, maxX, maxY, minZ, uC, vD, r, g, b, a, 0, 1, 0);
					makeVertex(transform, bufferbuilder, sprite, minX, maxY, minZ, uB, vD, r, g, b, a, 0, 1, 0);
				}
			}
		}
		tessellator.draw();
		GlStateManager.popMatrix();
	}

	Vector4f untransformed = new Vector4f(0, 0, 0, 1);
	Vector4f pos = new Vector4f();
	Vector4f normal = new Vector4f();

	private void makeVertex(Matrix4f transform, BufferBuilder bufferbuilder, TextureAtlasSprite sprite, float x, float y, float z, float u, float v, float r, float g, float b, float a, int normalX, int normalY, int normalZ)
	{
		untransformed.set(x, y, z);
		Matrix4f.transform(transform, untransformed, pos);

		untransformed.set(normalX, normalY, normalZ);
		Matrix4f.transform(transform, untransformed, normal);

		bufferbuilder.pos(pos.x, pos.y, pos.z).tex(sprite.getInterpolatedU(u), sprite.getInterpolatedV(v)).color(r, g, b, a).normal(normal.x, normal.y, normal.z).endVertex();

	}
}

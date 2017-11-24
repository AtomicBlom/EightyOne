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

		//GlStateManager.enableLighting();
		//GlStateManager.enableTexture2D();
		//GlStateManager.depthMask(true);
	}

	private void renderRotatingPortal(PortalTileEntity te, TextureAtlasSprite sprite, float yRotation, float scale, float alpha)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5, 0.5, 0.5);

		//GlStateManager.rotate(90, 0, 0, 1);
		GlStateManager.rotate(yRotation, 0, 1, 0);
		GlStateManager.rotate(yRotation / 2, 1, 0, 0);

		GlStateManager.scale(scale, scale, scale);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		float r = 1;
		float g = 1;
		float b = 1;
		float a = alpha;

		float cubeSize = 0.25f;

		final float v = (1 - (cubeSize * 3)) / 6;
		float offset = -0.5f + v;

		for (float cx = 0; cx < 3; ++cx)
		{
			for (float cy = 0; cy < 3; ++cy)
			{
				for (float cz = 0; cz < 3; ++cz)
				{
					float minX = cx / 3.0f + offset;
					float minY = cy / 3.0f + offset;
					float minZ = cz / 3.0f + offset;

					final float maxZ = minZ + cubeSize;
					final float maxY = minY + cubeSize;
					final float maxX = minX + cubeSize;

					final float uA = 0 / 4f * 16;
					final float uB = 1 / 4f * 16;
					final float uC = 2 / 4f * 16;
					final float uD = 3 / 4f * 16;
					final float uE = 4 / 4f * 16;

					final float vA = 3 / 4f * 16;
					final float vB = 2 / 4f * 16;
					final float vC = 1 / 4f * 16;
					final float vD = 0 / 4f * 16;

					bufferbuilder.pos(minX, minY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, 0, -1).endVertex();
					bufferbuilder.pos(maxX, minY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, 0, -1).endVertex();
					bufferbuilder.pos(maxX, maxY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 0, -1).endVertex();
					bufferbuilder.pos(minX, maxY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 0, -1).endVertex();

					bufferbuilder.pos(minX, maxY, minZ).tex(sprite.getInterpolatedU(uE), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 0, 1).endVertex();
					bufferbuilder.pos(maxX, maxY, minZ).tex(sprite.getInterpolatedU(uD), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 0, 1).endVertex();
					bufferbuilder.pos(maxX, minY, minZ).tex(sprite.getInterpolatedU(uD), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, 0, 1).endVertex();
					bufferbuilder.pos(minX, minY, minZ).tex(sprite.getInterpolatedU(uE), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, 0, 1).endVertex();

					bufferbuilder.pos(maxX, maxY, minZ).tex(sprite.getInterpolatedU(uD), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(-1, 0, 0).endVertex();
					bufferbuilder.pos(maxX, maxY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(-1, 0, 0).endVertex();
					bufferbuilder.pos(maxX, minY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(-1, 0, 0).endVertex();
					bufferbuilder.pos(maxX, minY, minZ).tex(sprite.getInterpolatedU(uD), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(-1, 0, 0).endVertex();

					bufferbuilder.pos(minX, minY, minZ).tex(sprite.getInterpolatedU(uA), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(1, 0, 0).endVertex();
					bufferbuilder.pos(minX, minY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(1, 0, 0).endVertex();
					bufferbuilder.pos(minX, maxY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(1, 0, 0).endVertex();
					bufferbuilder.pos(minX, maxY, minZ).tex(sprite.getInterpolatedU(uA), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(1, 0, 0).endVertex();

					bufferbuilder.pos(minX, minY, minZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vA)).color(r, g, b, a).normal(0, -1, 0).endVertex();
					bufferbuilder.pos(maxX, minY, minZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vA)).color(r, g, b, a).normal(0, -1, 0).endVertex();
					bufferbuilder.pos(maxX, minY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, -1, 0).endVertex();
					bufferbuilder.pos(minX, minY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vB)).color(r, g, b, a).normal(0, -1, 0).endVertex();

					bufferbuilder.pos(minX, maxY, maxZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 1, 0).endVertex();
					bufferbuilder.pos(maxX, maxY, maxZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vC)).color(r, g, b, a).normal(0, 1, 0).endVertex();
					bufferbuilder.pos(maxX, maxY, minZ).tex(sprite.getInterpolatedU(uC), sprite.getInterpolatedV(vD)).color(r, g, b, a).normal(0, 1, 0).endVertex();
					bufferbuilder.pos(minX, maxY, minZ).tex(sprite.getInterpolatedU(uB), sprite.getInterpolatedV(vD)).color(r, g, b, a).normal(0, 1, 0).endVertex();

				}
			}
		}
		tessellator.draw();
		GlStateManager.popMatrix();
	}
}

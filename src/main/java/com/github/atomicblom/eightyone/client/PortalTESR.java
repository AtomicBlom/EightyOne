package com.github.atomicblom.eightyone.client;

import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class PortalTESR extends TileEntitySpecialRenderer<PortalTileEntity>
{
	@Override
	public void render(PortalTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();

		final float yRotation = te.getYRotation() + partialTicks;
		te.setYRotation(yRotation);

		GlStateManager.disableBlend();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);


		GlStateManager.translate(0.5, 0.5, 0.5);

		GlStateManager.rotate(90, 0, 0, 1);
		GlStateManager.rotate(yRotation, 0, 1, 0);
		GlStateManager.rotate(yRotation/2, 1, 0, 0);

		//GlStateManager.scale(0.9, 0.9, 0.9);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder bufferbuilder = tessellator.getBuffer();


		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		float r = 1;
		float g = 0;
		float b = 0;
		float a = 1f;

		float cubeSize = 0.25f;

		float offset = -0.5f;

		for (float cx = 0; cx < 3; ++cx) {
			for (float cy = 0; cy < 3; ++cy) {
				for (float cz = 0; cz < 3; ++cz) {
					float cubeX = cx / 3.0f + offset;
					float cubeY = cy / 3.0f + offset;
					float cubeZ = cz / 3.0f + offset;

					bufferbuilder.pos(cubeX, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();

					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY, cubeZ).color(r, g, b, a).endVertex();

					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ).color(r, g, b, a).endVertex();

					bufferbuilder.pos(cubeX, cubeY, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();

					bufferbuilder.pos(cubeX, cubeY, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY, cubeZ + cubeSize).color(r, g, b, a).endVertex();

					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ + cubeSize).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX + cubeSize, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();
					bufferbuilder.pos(cubeX, cubeY + cubeSize, cubeZ).color(r, g, b, a).endVertex();

				}
			}
		}
		tessellator.draw();

		GlStateManager.popMatrix();

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		//GlStateManager.depthMask(true);
	}
}

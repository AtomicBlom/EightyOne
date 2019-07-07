package com.github.atomicblom.eightyone.client;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.tileentity.PortalProgressData;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import com.github.atomicblom.eightyone.client.tesrmodels.ArtifactVertexBuffer;
import com.github.atomicblom.eightyone.client.tesrmodels.CubeVertexBuffer;
import com.github.atomicblom.eightyone.client.tesrmodels.PortalFrameVertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("ALL")
public class PortalTESR extends TileEntitySpecialRenderer<TileEntityPortal>
{
	private final String portalTexture_active = new ResourceLocation(Reference.MOD_ID, "blocks/portal_active").toString();
	private final String portalTexture_disabled = new ResourceLocation(Reference.MOD_ID, "blocks/portal_disabled").toString();


	private BufferBuilder activePortalArtifact = null;
	private BufferBuilder disabledPortalArtifact = null;
	private BufferBuilder portalFrame = null;
	private BufferBuilder cube = null;

	private Comparator<PortalProgressData> viewComparator = new Comparator<PortalProgressData>() {
		@Override
		public int compare(PortalProgressData portalProgressData, PortalProgressData t1)
		{
			return Double.compare(portalProgressData.distanceFromPlayer, t1.distanceFromPlayer);
		}
	};

	PortalProgressData[] progressDataEntries = null;
	final WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();

	@Override
	public void render(TileEntityPortal te, double x, double y, double z, float partialTicks, int destroyStage, float alphalpha)
	{
		//Ensure our vertex buffers are created if neccessary
		if (activePortalArtifact == null) {
			activePortalArtifact = new ArtifactVertexBuffer(portalTexture_active).create();
		}
		if (disabledPortalArtifact == null) {
			disabledPortalArtifact = new ArtifactVertexBuffer(portalTexture_disabled).create();
		}
		if (portalFrame == null) {
			portalFrame = new PortalFrameVertexBuffer().create();
		}
		if (cube == null) {
			cube = new CubeVertexBuffer().create();
		}

		//All our textures come from the block texture map
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x, y, z);
			renderIncompleteFrame(te);
			renderArtifact(te, partialTicks);
		}
		GlStateManager.popMatrix();

		renderBuildProgress(te, x, y, z);
	}

	private void renderIncompleteFrame(TileEntityPortal te) {
		//If the
		if (!te.isValid())
		{
			//Translucent Render Pass A - fill Z-Buffer
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.colorMask(false, false, false, false);

			renderFrame(uploader, 0.7f);

			//Translucent Render Pass B - Render portal
			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0f);
			GlStateManager.colorMask(true, true, true, true);
			renderFrame(uploader, 0.7f);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}
	}

	private void renderArtifact(TileEntityPortal te, float partialTicks) {
		long systemTime = Minecraft.getSystemTime();

		//calculate heartbeat
		// The portal beats like a heart wich some very subtle effects.
		// A beat happens every 3 seconds. From 0 to 2.8 seconds, it just simply rotates
		// from 2.8 to 2.95 seconds the cube contracts
		// from 2.95 to 3 the cube rapidly expands back to it's original size.
		// After 3 seconds, it continues to simply rotate
		//     However a secondary "shadow" artifact is rendered from that point, spinning at a slightly slower rate.
		//     This will be present for about a second.
		final int shrinkStart = 2800;
		final int shrinkEnd = 2950;
		final int shrinkLength = shrinkEnd - shrinkStart;
		final int expandStart = shrinkEnd;
		final int expandEnd = 3000;
		final int expandLength = expandEnd - expandStart;
		final float expansionFactor = 20.0f;

		final long currentPulse = systemTime / 3000;
		// We are at the start of a new pulse
		if (te.getPulse() != currentPulse) {
			te.setPulse(currentPulse);
			// Capture the point of rotation of the shadow.
			te.setShadowRotation(te.getArtifactRotation());
		}
		final long currentPulseTime = systemTime % 3000;
		float artifactRotation = te.getArtifactRotation() + partialTicks / 2;
		// Shadow spins at half the speed of the artifact rotation
		float shadowRotation = te.getShadowRotation() + partialTicks / 4;

		//If the portal isn't valid
		if (!te.isValid()) {
			//Force all rotations to 0.
			shadowRotation = 0;
			systemTime = 0;
			artifactRotation = 0;
		}

		te.setArtifactRotation(artifactRotation);
		te.setShadowRotation(shadowRotation);

		float artifactScale = 1;

		if (currentPulseTime >= shrinkStart && currentPulseTime < shrinkEnd) {
			// Calculate the contraction scale
			artifactScale = 1 - (currentPulseTime - shrinkStart) / (float)shrinkLength / expansionFactor;
		} else if (currentPulseTime >= expandStart && currentPulseTime < expandEnd) {
			// Calculate the expansion scale
			artifactScale = 1 - (expandLength - (currentPulseTime - expandStart)) / (float)expandLength / expansionFactor;
		}

		BufferBuilder artifactModel = te.isValid() ? activePortalArtifact : disabledPortalArtifact;

		//Solid version of the artifact
		renderRotatingPortal(uploader, artifactModel, artifactRotation, artifactScale, 1.0f);


		float shadowScale = 1 + currentPulseTime / 3000.0f;
		final float alpha = 1 - (currentPulseTime / 1000.0f);

		// There will be 2 seconds that the alpha is less than 0, so don't bother rendering it.
		if (alpha > 0 && te.isValid())
		{
			//Translucent Render Pass A - fill Z-Buffer
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableAlpha();

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0f);
			GlStateManager.colorMask(false, false, false, false);

			renderRotatingPortal(uploader, disabledPortalArtifact, shadowRotation, shadowScale, alpha);

			//Translucent Render Pass B - Render portal
			GlStateManager.depthFunc(GL11.GL_EQUAL);
			GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0f);
			GlStateManager.colorMask(true, true, true, true);

			renderRotatingPortal(uploader, disabledPortalArtifact, shadowRotation, shadowScale, alpha);

			GlStateManager.depthFunc(GL11.GL_LEQUAL);
			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();
		}
	}

	private void renderFrame(WorldVertexBufferUploader uploader, float alpha)
	{
		GlStateManager.color(
				(alpha / 4) + 0.75f,
				1.0F,
				(alpha / 4) + 0.75f,
				alpha);

		uploader.draw(portalFrame);
	}

	private void renderRotatingPortal(WorldVertexBufferUploader uploader, BufferBuilder model, float yRotation, float scale, float alpha)
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

		uploader.draw(model);

		GlStateManager.popMatrix();
	}

	/**
	 * Shows players which blocks do not make a valid portal frame
	 *
	 * The progress is staggered
	 */
	private void renderBuildProgress(TileEntityPortal te, double x, double y, double z) {
		final PortalProgressData[] originalProgressData = te.getProgressData();

		if (originalProgressData == null || te.isValid()) return;

		if (progressDataEntries == null || progressDataEntries.length != originalProgressData.length) {
			progressDataEntries = new PortalProgressData[originalProgressData.length];
		}

		//Sort the cubes by their distance to the player to avoid any weird artifacts.
		for (int i = 0; i < progressDataEntries.length; ++i) {
			progressDataEntries[i] = originalProgressData[i];
			progressDataEntries[i].distanceFromPlayer = progressDataEntries[i].pos.distanceSq(x, y, z);
		}
		Arrays.sort(progressDataEntries, viewComparator);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		double speed = 3.0f;

		double delayBlocks = 18;//blocks delay between cycles
		double largestSize = 1.3f;//Scale compared to a block
		double tailLength = 8;//renderSets to show at a time.
		double scaleFactor = largestSize / tailLength;

		final long totalWorldTime = Minecraft.getSystemTime();
		double seconds = (totalWorldTime /1000.0)*speed;
		final BlockPos tePos = te.getPos();

		GlStateManager.disableDepth();

		for (PortalProgressData progressData : progressDataEntries)
		{
			//If the block is valid, don't render a cube
			if (progressData.currentlyValid) continue;

			float red = 0.75f;
			float green = 0.75f;
			float blue = 0.75f;

			// The renderSet dictates the staggering.
			float blockScale = (float)(((progressData.renderSet + seconds) % delayBlocks) * scaleFactor) ;

			float alpha = 1;

			// Air blocks will be rendered a shade of green
			if (progressData.currentlyAir)
			{

				// rendering only happens betweetn 0 and 120% scaling
				if (blockScale >= 1.2) continue;
				if (blockScale <= 0) continue;

				if (blockScale > 1)
				{
					alpha = 1 - ((blockScale - 1) * 5);
				} else if (blockScale < 0.2)
				{
					alpha = ((blockScale) * 5);
				}

				green = 1;
				alpha /= 2;
			} else {
				// Otherwise, the colouring will be more towards red.
				if (blockScale >= 1.5) continue;


				if (blockScale > 1)
				{
					alpha = 1 - ((blockScale - 1) * 2.5f);
				}
				if (blockScale <= 1) {
					//Use the scale to dictate the alpha instead
					alpha = blockScale;
					//and always render a full block instead.
					blockScale = 1.00f;
				};

				green = 0.25f;
				red = 1;
				blue = 0.25f;
				alpha /= 2;
			}

			GlStateManager.color(
					red,
					green,
					blue,
					alpha);

			GlStateManager.pushMatrix();

			GlStateManager.translate(x, y, z);

			GlStateManager.translate(progressData.pos.getX() - tePos.getX() + 0.5, progressData.pos.getY()- tePos.getY() + 0.5, progressData.pos.getZ() - tePos.getZ() + 0.5);
			GlStateManager.scale(blockScale, blockScale, blockScale);

			uploader.draw(cube);

			GlStateManager.popMatrix();
		}

		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
	}

}

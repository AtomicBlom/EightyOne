package com.github.atomicblom.eightyone.client;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.properties.MimicItemStackUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

/**
 * Created by TheGreyGhost on 19/04/2015.
 * This class is used to customise the rendering of the camouflage block, based on the block it is copying.
 */
public class MimicBakedModel implements IBakedModel {

	private ItemStack itemStack;
	IBlockState NOT_MIMICKING = Blocks.AIR.getDefaultState();

	public MimicBakedModel(IBakedModel unCamouflagedModel)
	{
		modelWhenNotCamouflaged = unCamouflagedModel;

	}

	public MimicBakedModel(IBakedModel bakedModelIn, ItemStack itemStackIn) {
		if (bakedModelIn instanceof MimicBakedModel) {
			modelWhenNotCamouflaged = ((MimicBakedModel)bakedModelIn).modelWhenNotCamouflaged;
		} else {
			modelWhenNotCamouflaged = bakedModelIn;
		}

		itemStack = itemStackIn;
	}

	public List<BakedQuad> getQuads(@Nullable IBlockState iBlockState, @Nullable EnumFacing side, long rand)
	{
		IBlockState mimicBlockState = null;
		List<BakedQuad> quads = Lists.newArrayList();

		final BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
		Minecraft mc = Minecraft.getMinecraft();
		BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
		BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();

		//Mimic
		if (iBlockState instanceof IExtendedBlockState)
		{
			IExtendedBlockState extendedBlockState = (IExtendedBlockState) iBlockState;
			mimicBlockState = extendedBlockState.getValue(Reference.Blocks.MIMIC);
		}

		if (iBlockState == null && itemStack != null) {
			mimicBlockState = MimicItemStackUtil.getMimickedBlock(itemStack);
			Item item = itemStack.getItem();
			if (item instanceof ItemBlock) {
				iBlockState = ((ItemBlock) item).getBlock().getDefaultState();
			}
		}

		if (mimicBlockState != NOT_MIMICKING && mimicBlockState != null)
		{
			if (mimicBlockState.getBlock().canRenderInLayer(mimicBlockState, renderLayer) || renderLayer == null)
			{
				IBakedModel mimicBakedModel = blockModelShapes.getModelForState(mimicBlockState);
				quads.addAll(mimicBakedModel.getQuads(mimicBlockState, side, rand));
			}
		}

		//Overlay
		final Minecraft minecraft = Minecraft.getMinecraft();
		final boolean isCreativeThisFrame = minecraft.player.isCreative();

		if (isCreativeThisFrame && (renderLayer == BlockRenderLayer.CUTOUT || renderLayer == null) && iBlockState != null) {
			final IBlockState state = iBlockState.getBlock().getDefaultState().withProperty(Reference.Blocks.OVERLAY, true);
			final IBakedModel modelForState = blockModelShapes.getModelForState(state);
			quads.addAll(modelForState.getQuads(state, side, rand));
		}

		return quads;
	}

	private IBakedModel modelWhenNotCamouflaged;

	// getTexture is used directly when player is inside the block.  The game will crash if you don't use something
	//   meaningful here.
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return modelWhenNotCamouflaged.getParticleTexture();
	}

	// ideally, this should be changed for different blocks being camouflaged, but this is not supported by vanilla
	@Override
	public boolean isAmbientOcclusion()
	{
		return modelWhenNotCamouflaged.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return modelWhenNotCamouflaged.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return modelWhenNotCamouflaged.isBuiltInRenderer();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return new SampleItemOverrideList(Collections.EMPTY_LIST);
	}

	@Override
	@Deprecated
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return modelWhenNotCamouflaged.getItemCameraTransforms();
	}

	/** this method is necessary because Forge has deprecated getItemCameraTransforms(), and modelCore.getItemCameraTransforms()
	 *    may not return anything meaningful.  But if the base model doesn't implement IPerspectiveAwareModel then you
	 *    need to generate it.
	 * @param cameraTransformType
	 * @return
	 */
	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		Matrix4f matrix4f = modelWhenNotCamouflaged.handlePerspective(cameraTransformType).getRight();
		return Pair.of(this, matrix4f);
	}

	public class SampleItemOverrideList extends ItemOverrideList {

		public SampleItemOverrideList(List<ItemOverride> overridesIn) {
			super(overridesIn);
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			return new MimicBakedModel(originalModel, stack);
		}
	}
}
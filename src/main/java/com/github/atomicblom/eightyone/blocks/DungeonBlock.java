package com.github.atomicblom.eightyone.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DungeonBlock extends Block implements ITileEntityProvider
{
	public static final UnlistedPropertyCopiedBlock COPIEDBLOCK = new UnlistedPropertyCopiedBlock();

	public DungeonBlock()
	{
		super(Material.ROCK);
		setHardness(2.0F);
		setSoundType(SoundType.STONE);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setUnlocalizedName("bedrock");
		disableStats();
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		final IProperty [] listedProperties = new IProperty[0]; // no listed properties
		final IUnlistedProperty [] unlistedProperties = {COPIEDBLOCK};
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final TileEntity te = world.getTileEntity(pos);
			if (te instanceof IPaintableTileEntity) {
				IExtendedBlockState retval = (IExtendedBlockState)state;
				final IBlockState copiedBlock = ((IPaintableTileEntity) te).getPaintSource();
				retval = retval.withProperty(COPIEDBLOCK, copiedBlock);
				return retval;
			}

		}
		return state;
	}

	@Override
	public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
		return true;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	// set to true because this block is opaque and occupies the entire 1x1x1 space
	// not strictly required because the default (super method) is true
	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState iBlockState) {
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		final IBlockState paintSource = getPaintSource(world, pos);
		if (paintSource != null)
		{
			return paintSource.getBlock().doesSideBlockRendering(paintSource, world, pos, face);
		}
		return super.doesSideBlockRendering(state, world, pos, face);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityDungeonBlock();
	}

	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase player,
	                            @Nonnull ItemStack stack) {
		setPaintSource(world, pos, PainterUtil2.getSourceBlock(stack));
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}


	private static void setPaintSource(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable IBlockState paintSource) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IPaintableTileEntity) {
			((IPaintableTileEntity) te).setPaintSource(paintSource);
		}
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
	                              @Nonnull EntityPlayer player) {
		final ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
		PainterUtil2.setSourceBlock(pickBlock, getPaintSource(world, pos));
		return pickBlock;
	}

	private static IBlockState getPaintSource(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IPaintableTileEntity) {
			return ((IPaintableTileEntity) te).getPaintSource();
		}
		return null;
	}
}

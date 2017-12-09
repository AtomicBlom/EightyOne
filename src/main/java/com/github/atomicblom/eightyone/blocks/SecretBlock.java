package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.material.SecretMaterial;
import com.github.atomicblom.eightyone.blocks.properties.MimicItemStackUtil;
import com.github.atomicblom.eightyone.blocks.properties.IMimicTileEntity;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SecretBlock extends Block implements ITileEntityProvider
{
//	public SecretBlock()
//	{
//		super(new SecretMaterial());
//
//		//setBlockUnbreakable();
//
//
//	}

	public SecretBlock()
	{
		super(Material.AIR);
		setSoundType(SoundType.STONE);
		setResistance(6000000.0F);
		disableStats();
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		setDefaultState(getDefaultState().withProperty(Reference.Blocks.OVERLAY, false));
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}

	@Nullable
	@Override
	@Deprecated
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
	{
		return Block.NULL_AABB;
	}

	@Override
	@Deprecated
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
	}

	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
	{
		return true;
	}

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		final IProperty [] listedProperties = {Reference.Blocks.OVERLAY}; // no listed properties
		final IUnlistedProperty [] unlistedProperties = {Reference.Blocks.MIMIC};
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final TileEntity te = world.getTileEntity(pos);
			if (te instanceof IMimicTileEntity) {
				IExtendedBlockState retval = (IExtendedBlockState)state;
				final IBlockState copiedBlock = ((IMimicTileEntity) te).getCopiedBlock();
				retval = retval.withProperty(Reference.Blocks.MIMIC, copiedBlock);
				return retval;
			}

		}
		return state;
	}

	@Override
	public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
		return true;
	}

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
		setPaintSource(world, pos, MimicItemStackUtil.getMimickedBlock(stack));
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}


	private static void setPaintSource(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable IBlockState paintSource) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IMimicTileEntity) {
			((IMimicTileEntity) te).setCopiedBlock(paintSource);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced)
	{
		final IBlockState sourceBlock = MimicItemStackUtil.getMimickedBlock(stack);
		if (sourceBlock == null) {
			tooltip.add("Empty");
		} else {
			ItemStack pickBlock = sourceBlock.getBlock().getPickBlock(sourceBlock, null, world, BlockPos.ORIGIN, null);

			tooltip.add("Contains: " + I18n.translateToLocal(pickBlock.getDisplayName()));
		}

	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
	                              @Nonnull EntityPlayer player) {
		final ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
		MimicItemStackUtil.setMimickedBlock(pickBlock, getPaintSource(world, pos));
		return pickBlock;
	}

	private static IBlockState getPaintSource(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IMimicTileEntity) {
			return ((IMimicTileEntity) te).getCopiedBlock();
		}
		return null;
	}
}

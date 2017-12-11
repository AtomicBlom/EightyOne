package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.material.DungeonMaterial;
import com.github.atomicblom.eightyone.blocks.properties.IMimicTileEntity;
import com.github.atomicblom.eightyone.blocks.properties.MimicItemStackUtil;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
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

public class UnbreakableBlock extends Block
{


	public UnbreakableBlock()
	{
		super(new DungeonMaterial());
		setHardness(2.0F);
		setSoundType(SoundType.STONE);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		disableStats();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		final IProperty[] listedProperties = {};
		final IUnlistedProperty[] unlistedProperties = {Reference.Blocks.MIMIC};
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

	/**
	 * Called When an Entity Collided with the Block
	 */
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		final IBlockState mimicBlockState = getMimicBlock(worldIn, pos);
		if (mimicBlockState != null) {
			mimicBlockState.getBlock().onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		}
	}

	@Override
	public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity) {
		final IBlockState mimicBlockState = getMimicBlock(world, pos);
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getSlipperiness(state, world, pos, entity);
		}
		return super.getSlipperiness(state, world, pos, entity);
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		final IBlockState mimicBlockState = getMimicBlock(blockAccess, pos);
		if (mimicBlockState != null)
		{
			if (useGlassBehaviour(mimicBlockState)) {
				//TODO: Glass - See BlockBreakable
				return mimicBlockState.getBlock().doesSideBlockRendering(mimicBlockState, blockAccess, pos, side);
			} else {
				return mimicBlockState.getBlock().doesSideBlockRendering(mimicBlockState, blockAccess, pos, side);
			}
		}
		return super.doesSideBlockRendering(state, blockAccess, pos, side);
	}

	private boolean useGlassBehaviour(IBlockState mimicBlockState) {
		Block block = mimicBlockState.getBlock();
		return block == Blocks.GLASS || block == Blocks.STAINED_GLASS;
	}

	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase player,
	                            @Nonnull ItemStack stack) {
		setMimicBlock(world, pos, MimicItemStackUtil.getMimickedBlock(stack));
		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, state, state, 3);
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

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
	                              EntityPlayer player) {
		final ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
		MimicItemStackUtil.setMimickedBlock(pickBlock, getMimicBlock(world, pos));
		return pickBlock;
	}

	private static void setMimicBlock(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable IBlockState paintSource) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IMimicTileEntity) {
			((IMimicTileEntity) te).setCopiedBlock(paintSource);
		}
	}

	private static IBlockState getMimicBlock(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		final TileEntity te = world.getTileEntity(pos);
		if (te instanceof IMimicTileEntity) {
			return ((IMimicTileEntity) te).getCopiedBlock();
		}
		return null;
	}
}

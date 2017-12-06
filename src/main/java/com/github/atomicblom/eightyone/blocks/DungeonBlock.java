package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.blocks.properties.CopiedBlockProperty;
import com.github.atomicblom.eightyone.blocks.properties.CopiedBlockUtil;
import com.github.atomicblom.eightyone.blocks.properties.IMimicTileEntity;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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

public class DungeonBlock extends Block implements ITileEntityProvider
{
	public static final CopiedBlockProperty COPIEDBLOCK = new CopiedBlockProperty();
	public static final PropertyBool SOURCEIMAGE = PropertyBool.create("is_lock_model");

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
		setDefaultState(getDefaultState().withProperty(SOURCEIMAGE, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		final IProperty [] listedProperties = {SOURCEIMAGE}; // no listed properties
		final IUnlistedProperty [] unlistedProperties = {COPIEDBLOCK};
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final TileEntity te = world.getTileEntity(pos);
			if (te instanceof IMimicTileEntity) {
				IExtendedBlockState retval = (IExtendedBlockState)state;
				final IBlockState copiedBlock = ((IMimicTileEntity) te).getCopiedBlock();
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
		setPaintSource(world, pos, CopiedBlockUtil.getCopiedBlock(stack));
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
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced)
	{
		final IBlockState sourceBlock = CopiedBlockUtil.getCopiedBlock(stack);
		if (sourceBlock == null) {
			tooltip.add("Empty");
		} else {
			final Block block = sourceBlock.getBlock();
			final int metaFromState = block.getMetaFromState(sourceBlock);

			Item i = Item.getItemFromBlock(block);


			tooltip.add("Contains: " + I18n.translateToLocal(block.getUnlocalizedName() + ".name"));
		}

	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos,
	                              @Nonnull EntityPlayer player) {
		final ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
		CopiedBlockUtil.setCopiedBlock(pickBlock, getPaintSource(world, pos));
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

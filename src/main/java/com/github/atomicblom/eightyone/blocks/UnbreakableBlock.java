package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.material.DungeonMaterial;
import com.github.atomicblom.eightyone.blocks.properties.IMimicTileEntity;
import com.github.atomicblom.eightyone.blocks.properties.MimicItemStackUtil;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
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


	private final List<NBTTagCompound> dungeonStates;
	private final IBlockState[] mimicStates;
	private final int blockSlice;

	public UnbreakableBlock(List<NBTTagCompound> dungeonStates, int blockSlice)
	{
		super(new DungeonMaterial());
		this.dungeonStates = dungeonStates;
		this.blockSlice = blockSlice;

		int totalStates = dungeonStates.size() - blockSlice;
		if (totalStates > 16) totalStates = 16;

		mimicStates = new IBlockState[totalStates];

		setHardness(2.0F);
		setSoundType(SoundType.STONE);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		disableStats();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		final IProperty[] listedProperties = {Reference.Blocks.VARIATION};
		final IUnlistedProperty[] unlistedProperties = {Reference.Blocks.MIMIC};
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final Integer value = state.getValue(Reference.Blocks.VARIATION);


			IExtendedBlockState retval = (IExtendedBlockState)state;
			retval = retval.withProperty(Reference.Blocks.MIMIC, this.mimicStates[value]);
			return retval;
		}
		return state;
	}

	@Override
	public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
		return true;
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		if (mimicStates == null) return false;
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isOpaqueCube(state);
		}
		return false;
	}

	@Override
	@Deprecated
	public int getLightOpacity(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getLightOpacity(state);
		}
		return 0;
	}

	@Override
	@Deprecated
	public Material getMaterial(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getMaterial(state);
		}
		return Material.GROUND;
	}

	@Override
	@Deprecated
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getBlockFaceShape(worldIn, state, pos, face);
		}
		return super.getBlockFaceShape(worldIn, state, pos, face);
	}

	@Override
	@Deprecated
	public int getLightValue(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getLightValue(state);
		}
		return super.getLightValue(state);
	}

	@Override
	@Deprecated
	public boolean isFullBlock(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isFullBlock(state);
		}
		return super.isFullBlock(state);
	}

	@Override
	@Deprecated
	public boolean isTopSolid(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isTopSolid(state);
		}
		return super.isTopSolid(state);
	}

	@Override
	@Deprecated
	public boolean isTranslucent(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isTranslucent(state);
		}
		return super.isTranslucent(state);
	}

	@Override
	@Deprecated
	public boolean isNormalCube(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isNormalCube(state);
		}
		return super.isNormalCube(state);
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isFullCube(state);
		}
		return super.isFullCube(state);
	}

	@Override
	@Deprecated
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().isSideSolid(state, world, pos, side);
		}
		return super.isSideSolid(state, world, pos, side);
	}

	/**
	 * Called When an Entity Collided with the Block
	 */
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			mimicBlockState.getBlock().onEntityCollision(worldIn, pos, state, entityIn);
		}
	}

	@Override
	public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity) {
		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
		if (mimicBlockState != null) {
			return mimicBlockState.getBlock().getSlipperiness(state, world, pos, entity);
		}
		return super.getSlipperiness(state, world, pos, entity);
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{


		final IBlockState mimicBlockState = mimicStates[state.getValue(Reference.Blocks.VARIATION)];
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

/*	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase player,
	                            @Nonnull ItemStack stack) {
		//setMimicBlock(world, pos, MimicItemStackUtil.getMimickedBlock(stack));

		if (!world.isRemote) {
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}*/

	@Override
	@Deprecated
	public IBlockState getStateFromMeta(int meta)
	{
		if (meta >= this.mimicStates.length) {
			meta = 0;
		}
		return getDefaultState().withProperty(Reference.Blocks.VARIATION, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(Reference.Blocks.VARIATION);
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
		return pickBlock;
	}

	public void materialize()
	{
		for (int i = this.blockSlice, j = 0; i < this.dungeonStates.size() && j < mimicStates.length; i++, j++)
		{
			NBTTagCompound dungeonState = dungeonStates.get(i);
			mimicStates[j] = NBTUtil.readBlockState(dungeonState);
		}
	}

	public IBlockState getMimicStateForBlockState(IBlockState sourceState) {
		for (int i = 0; i < mimicStates.length; ++i) {
			if (mimicStates[i] == sourceState) {
				return this.getDefaultState().withProperty(Reference.Blocks.VARIATION, i);
			}
		}
		return null;
	}
}

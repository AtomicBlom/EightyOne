package com.github.atomicblom.eightyone.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import javax.annotation.Nullable;

public class TileEntityDungeonBlock extends TileEntity implements IPaintableTileEntity
{
	private IBlockState paintSource = Blocks.AIR.getDefaultState();

	public TileEntityDungeonBlock() {
	}

	public void setPaintSource(@Nullable IBlockState paintSource) {
		this.paintSource = paintSource;
		markDirty();
	}

	public IBlockState getPaintSource() {
		return paintSource;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		if (compound.hasKey("source")) {
			paintSource = NBTUtil.readBlockState(compound.getCompoundTag("source"));
		}
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag)
	{
		if (tag.hasKey("source")) {
			paintSource = NBTUtil.readBlockState(tag.getCompoundTag("source"));
		}
		super.handleUpdateTag(tag);
	}


	@Override
	public NBTTagCompound getUpdateTag()
	{
		final NBTTagCompound updateTag = super.getUpdateTag();
		if (paintSource != null)
		{
			final NBTTagCompound sourceTag = new NBTTagCompound();
			NBTUtil.writeBlockState(sourceTag, paintSource);
			updateTag.setTag("source", sourceTag);
		}
		return updateTag;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
		if (paintSource != null)
		{
			final NBTTagCompound sourceTag = new NBTTagCompound();
			NBTUtil.writeBlockState(sourceTag, paintSource);
			nbtTagCompound.setTag("source", sourceTag);
		}
		return nbtTagCompound;
	}
}
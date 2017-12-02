package com.github.atomicblom.eightyone.blocks;

import net.minecraft.block.state.IBlockState;

public interface IPaintableTileEntity
{
	void setPaintSource(IBlockState paintSource);

	IBlockState getPaintSource();
}

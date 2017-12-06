package com.github.atomicblom.eightyone.blocks.properties;

import net.minecraft.block.state.IBlockState;

public interface IMimicTileEntity
{
	void setCopiedBlock(IBlockState paintSource);

	IBlockState getCopiedBlock();
}

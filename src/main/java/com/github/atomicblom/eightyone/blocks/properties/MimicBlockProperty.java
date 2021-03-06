package com.github.atomicblom.eightyone.blocks.properties;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class MimicBlockProperty implements IUnlistedProperty<IBlockState>
{
	@Override
	public String getName() {
		return "CopiedBlock";
	}

	@Override
	public boolean isValid(IBlockState value) {
		return true;
	}

	@Override
	public Class<IBlockState> getType() {
		return IBlockState.class;
	}

	@Override
	public String valueToString(IBlockState value) {
		return value.toString();
	}
}

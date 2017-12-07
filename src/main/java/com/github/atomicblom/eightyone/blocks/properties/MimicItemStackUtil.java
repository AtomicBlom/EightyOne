package com.github.atomicblom.eightyone.blocks.properties;

import com.github.atomicblom.eightyone.util.NbtValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import javax.annotation.Nonnull;

public class MimicItemStackUtil
{
	public static IBlockState getMimickedBlock(@Nonnull ItemStack itemStack)
	{
		final NBTTagCompound tag = NbtValue.BLOCKSTATE.getTag(itemStack.getTagCompound());
		if (tag != null)
		{
			final IBlockState mimicBlockState = NBTUtil.readBlockState(tag);
			if (mimicBlockState != Blocks.AIR.getDefaultState())
			{
				return mimicBlockState;
			}
		}
		return null;
	}

	public static void setMimickedBlock(@Nonnull ItemStack itemStack, IBlockState mimicBlockState)
	{
		if (itemStack.isEmpty())
		{
			return;
		}
		if (mimicBlockState == null || mimicBlockState == Blocks.AIR)
		{
			NbtValue.BLOCKSTATE.removeTag(itemStack);
			return;
		} else
		{
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			if (tagCompound == null)
			{
				itemStack.setTagCompound(tagCompound = new NBTTagCompound());
			}
			NbtValue.BLOCKSTATE.setTag(tagCompound, NBTUtil.writeBlockState(new NBTTagCompound(), mimicBlockState));
		}
	}
}

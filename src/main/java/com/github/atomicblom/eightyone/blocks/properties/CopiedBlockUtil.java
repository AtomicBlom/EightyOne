package com.github.atomicblom.eightyone.blocks.properties;

import com.github.atomicblom.eightyone.util.NbtValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import javax.annotation.Nonnull;

public class CopiedBlockUtil
{
	public static IBlockState getCopiedBlock(@Nonnull ItemStack itemStack)
	{
		final NBTTagCompound tag = NbtValue.BLOCKSTATE.getTag(itemStack.getTagCompound());
		if (tag != null)
		{
			final IBlockState copiedBlockState = NBTUtil.readBlockState(tag);
			if (copiedBlockState != Blocks.AIR.getDefaultState())
			{
				return copiedBlockState;
			}
		}
		return null;
	}

	public static void setCopiedBlock(@Nonnull ItemStack itemStack, IBlockState copiedBlockState)
	{
		if (itemStack.isEmpty())
		{
			return;
		}
		if (copiedBlockState == null || copiedBlockState == Blocks.AIR)
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
			NbtValue.BLOCKSTATE.setTag(tagCompound, NBTUtil.writeBlockState(new NBTTagCompound(), copiedBlockState));
		}
	}
}

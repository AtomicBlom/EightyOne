package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.util.NbtValue;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPiston;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import javax.annotation.Nonnull;

public class PainterUtil2
{
	public static IBlockState getSourceBlock(@Nonnull ItemStack itemStack)
	{
		return readNbt(itemStack.getTagCompound());
	}

	public static IBlockState readNbt(NBTTagCompound nbtRoot)
	{
		final NBTTagCompound tag = NbtValue.BLOCKSTATE.getTag(nbtRoot);
		if (tag != null)
		{
			final IBlockState paint = NBTUtil.readBlockState(tag);
			if (paint != Blocks.AIR.getDefaultState())
			{
				return paint;
			}
		}
		return null;
	}

	public static void setSourceBlock(@Nonnull ItemStack itemStack, IBlockState paintSource)
	{
		if (itemStack.isEmpty())
		{
			return;
		}
		if (paintSource == null || paintSource == Blocks.AIR)
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
			NbtValue.BLOCKSTATE.setTag(tagCompound, NBTUtil.writeBlockState(new NBTTagCompound(), paintSource));
		}
	}
}

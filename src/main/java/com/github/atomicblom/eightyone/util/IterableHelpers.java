package com.github.atomicblom.eightyone.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import java.util.Set;

public final class IterableHelpers
{
	public static Boolean distinctNbt(NBTTagCompound tagCompound, Set<NBTTagCompound> seen) {

		for (final NBTTagCompound existingTagCompound : seen)
		{
			if (NBTUtil.areNBTEquals(tagCompound, existingTagCompound, true)) {
				return false;
			}
		}
		seen.add(tagCompound);
		return true;
	}
}

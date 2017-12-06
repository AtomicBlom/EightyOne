package com.github.atomicblom.eightyone.util;

import com.github.atomicblom.eightyone.Reference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public enum NbtValue {
	BLOCKSTATE("paint");

	@Nonnull
	private final String key;

	NbtValue(@Nonnull String key) {
		this.key = Reference.MOD_ID + ":" + key.toLowerCase(Locale.ENGLISH);
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK TAGCOMPOUND
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	@Nonnull
	public void removeTag(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			stack.getTagCompound().removeTag(key);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// NBT TAGCOMPOUND
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	@Nullable
	public void removeTag(@Nullable NBTTagCompound tag) {
		if (tag != null && tag.hasKey(key)) {
			tag.removeTag(key);
		}
	}

	@Nullable
	public NBTTagCompound getTag(@Nullable NBTTagCompound tag) {
		return getTag(tag, new NBTTagCompound());
	}

	@Nullable
	public NBTTagCompound getTag(@Nullable NBTTagCompound tag, @Nullable NBTTagCompound defaultTag) {
		if (tag != null && tag.hasKey(key)) {
			return (NBTTagCompound) tag.getTag(key);
		}
		setTag(tag, defaultTag);
		return defaultTag;
	}

	public void setTag(@Nullable NBTTagCompound tag, @Nullable NBTTagCompound value) {
		if (tag != null) {
			if (value == null) {
				removeTag(tag);
			} else {
				tag.setTag(key, value);
			}
		}
	}
}
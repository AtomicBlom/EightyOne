package com.github.atomicblom.eightyone.util;

import com.github.atomicblom.eightyone.Reference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public enum NbtValue { // TODO: DONE111
	GLINT("glinted"),
	CAPNAME("capname"),
	CAPNO("capno"),
	/**
	 * Used on item stacks to signal the renderer that the stack is not real but is used as a GUI element. The effects are specific to the item, e.g. the tank
	 * only renders the fluid.
	 */
	FAKE("fake"),
	REMOTE_X("x"),
	REMOTE_Y("y"),
	REMOTE_Z("z"),
	REMOTE_D("d"),
	REMOTE_POS("pos"),
	REMOTE_NAME("name"),
	REMOTE_ICON("icon"),
	ENERGY("Energy"),
	FLUIDAMOUNT("famount"),
	BLOCKSTATE("paint"),
	DISPLAYMODE("displaymode"),
	MAGNET_ACTIVE("magnetActive"),
	LAST_USED_TICK("lastUsedAt"),
	FILTER("filter"),
	FILTER_CLASS("class"),
	FILTER_BLACKLIST("isBlacklist"),
	FILTER_META("matchMeta"),
	FILTER_NBT("matchNBT"),
	FILTER_OREDICT("useOreDict"),
	FILTER_STICKY("sticky"),
	FILTER_ADVANCED("isAdvanced"),
	FILTER_LIMITED("isLimited"),
	FILTER_DAMAGE("damageMode"),

	;

	private final @Nonnull String key;

	private NbtValue(@Nonnull String key) {
		this.key = Reference.MOD_ID + ":" + key.toLowerCase(Locale.ENGLISH);
	}

	public @Nonnull String getKey() {
		return key;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK STRING
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public @Nonnull String getString(@Nonnull ItemStack stack, @Nonnull String _default) {
		if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			return stack.getTagCompound().getString(key);
		}
		return _default;
	}

	public String getString(@Nonnull ItemStack stack) {
		return getString(stack, "");
	}

	@SuppressWarnings("null")
	public @Nonnull ItemStack setString(@Nonnull ItemStack stack, String value) {
		if (!stack.isEmpty() && value != null) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setString(key, value);
		} else {
			removeTag(stack);
		}
		return stack;
	}

	public @Nonnull ItemStack setStringCopy(@Nonnull ItemStack stack, String value) {
		return setString(stack.copy(), value);
	}

	public @Nonnull ItemStack setStringCopy(@Nonnull ItemStack stack, String value, int stackSize) {
		final ItemStack stack2 = setStringCopy(stack, value);
		stack2.setCount(stackSize);
		return stack2;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK INT
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public int getInt(@Nonnull ItemStack stack, int _default) {
		if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			return stack.getTagCompound().getInteger(key);
		}
		return _default;
	}

	public int getInt(@Nonnull ItemStack stack) {
		return getInt(stack, 0);
	}

	@SuppressWarnings("null")
	public @Nonnull ItemStack setInt(@Nonnull ItemStack stack, int value) {
		if (!stack.isEmpty()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setInteger(key, value);
		}
		return stack;
	}

	public @Nonnull ItemStack setIntCopy(@Nonnull ItemStack stack, int value) {
		return setInt(stack.copy(), value);
	}

	public @Nonnull ItemStack setIntCopy(@Nonnull ItemStack stack, int value, int stackSize) {
		final ItemStack stack2 = setIntCopy(stack, value);
		stack2.setCount(stackSize);
		return stack2;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK LONG
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public long getLong(@Nonnull ItemStack stack, long _default) {
		if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			return stack.getTagCompound().getLong(key);
		}
		return _default;
	}

	public long getLong(@Nonnull ItemStack stack) {
		return getLong(stack, 0L);
	}

	@SuppressWarnings("null")
	public @Nonnull ItemStack setLong(@Nonnull ItemStack stack, long value) {
		if (!stack.isEmpty()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setLong(key, value);
		}
		return stack;
	}

	public @Nonnull ItemStack setLongCopy(@Nonnull ItemStack stack, long value) {
		return setLong(stack.copy(), value);
	}

	public @Nonnull ItemStack setLongCopy(@Nonnull ItemStack stack, long value, int stackSize) {
		final ItemStack stack2 = setLongCopy(stack, value);
		stack2.setCount(stackSize);
		return stack2;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK BOOL
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public boolean getBoolean(@Nonnull ItemStack stack, boolean _default) {
		if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			return stack.getTagCompound().getBoolean(key);
		}
		return _default;
	}

	public boolean getBoolean(@Nonnull ItemStack stack) {
		return getBoolean(stack, false);
	}

	@SuppressWarnings("null")
	public @Nonnull ItemStack setBoolean(@Nonnull ItemStack stack, boolean value) {
		if (!stack.isEmpty()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setBoolean(key, value);
		}
		return stack;
	}

	public @Nonnull ItemStack setBooleanCopy(@Nonnull ItemStack stack, boolean value) {
		return setBoolean(stack.copy(), value);
	}

	public @Nonnull ItemStack setBooleanCopy(@Nonnull ItemStack stack, boolean value, int stackSize) {
		final ItemStack stack2 = setBooleanCopy(stack, value);
		stack2.setCount(stackSize);
		return stack2;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK TAGCOMPOUND
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public boolean hasTag(@Nonnull ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(key);
	}

	@SuppressWarnings("null")
	public @Nonnull ItemStack removeTag(@Nonnull ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey(key)) {
			stack.getTagCompound().removeTag(key);
		}
		return stack;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// NBT ITEMSTACK
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	public @Nonnull ItemStack getStack(@Nullable NBTTagCompound tag, @Nonnull ItemStack _default) {
		if (tag != null && tag.hasKey(key)) {
			return new ItemStack(tag.getCompoundTag(key));
		}
		return _default;
	}

	public @Nonnull ItemStack getStack(@Nullable NBTTagCompound tag) {
		return getStack(tag, ItemStack.EMPTY);
	}

	public @Nullable NBTTagCompound setStack(@Nullable NBTTagCompound tag, @Nonnull ItemStack value) {
		if (tag != null) {
			tag.setTag(key, value.writeToNBT(new NBTTagCompound()));
		}
		return tag;
	}

	public @Nullable NBTTagCompound setStackCopy(@Nullable NBTTagCompound tag, @Nonnull ItemStack value) {
		return tag != null ? setStack(tag.copy(), value) : null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// NBT TAGCOMPOUND
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean hasTag(@Nullable NBTTagCompound tag) {
		return tag != null && tag.hasKey(key);
	}

	public @Nullable NBTTagCompound removeTag(@Nullable NBTTagCompound tag) {
		if (tag != null && tag.hasKey(key)) {
			tag.removeTag(key);
		}
		return tag;
	}

	public @Nullable NBTTagCompound removeTagCopy(@Nullable NBTTagCompound tag) {
		return tag != null ? removeTag(tag.copy()) : null;
	}

	public @Nullable NBTTagCompound getTag(@Nullable NBTTagCompound tag) {
		return getTag(tag, new NBTTagCompound());
	}

	public @Nullable NBTTagCompound getTag(@Nullable NBTTagCompound tag, @Nullable NBTTagCompound _default) {
		if (tag != null && tag.hasKey(key)) {
			return (NBTTagCompound) tag.getTag(key);
		}
		setTag(tag, _default);
		return _default;
	}

	public NBTTagCompound setTag(@Nullable NBTTagCompound tag, @Nullable NBTTagCompound value) {
		if (tag != null) {
			if (value == null) {
				removeTag(tag);
			} else {
				tag.setTag(key, value);
			}
		}
		return tag;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	// ITEMSTACK ROOT
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("null")
	public static @Nonnull NBTTagCompound getRoot(@Nonnull ItemStack stack) {
		if (!stack.isEmpty()) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			return stack.getTagCompound();
		}
		return new NBTTagCompound();
	}

}
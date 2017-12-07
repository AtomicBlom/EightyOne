package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.ItemLibrary;
import com.github.atomicblom.eightyone.blocks.properties.MimicItemStackUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class DungeonRecipe extends Impl<IRecipe> implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn)
	{
		boolean hasDungeonBlock = false;
		boolean hasBlock = false;
		for (int i = 0; i < inv.getHeight(); ++i)
		{
			for (int j = 0; j < inv.getWidth(); ++j)
			{
				final ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

				if (!itemstack.isEmpty())
				{
					final Item item = itemstack.getItem();
					if (item == ItemLibrary.dungeon_block || item == ItemLibrary.secret_block) {
						if (hasDungeonBlock) {
							return false;
						} else {
							hasDungeonBlock = true;
						}
					} else if (item instanceof ItemBlock) {
						if (hasBlock) {
							return false;
						} else {
							hasBlock = true;
						}
					}
				}
			}
		}
		return hasDungeonBlock && hasBlock;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack dungeonBlock = ItemStack.EMPTY;
		IBlockState blockState = null;
		for (int i = 0; i < inv.getHeight(); ++i)
		{
			for (int j = 0; j < inv.getWidth(); ++j)
			{
				final ItemStack itemstack = inv.getStackInRowAndColumn(j, i);

				if (!itemstack.isEmpty())
				{
					final Item item = itemstack.getItem();
					if (item == ItemLibrary.dungeon_block || item == ItemLibrary.secret_block) {
						dungeonBlock = itemstack.copy();
					} else if (item instanceof ItemBlock) {
						final Block block = ((ItemBlock) item).getBlock();
						blockState = block.getStateFromMeta(item.getMetadata(itemstack.getMetadata()));
					}
				}
			}
		}

		if (!dungeonBlock.isEmpty() && blockState != null) {
			MimicItemStackUtil.setMimickedBlock(dungeonBlock, blockState);
		}
		return dungeonBlock;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width > 1 || height > 1;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return new ItemStack(ItemLibrary.dungeon_block, 1);
	}

	@Override
	public boolean isDynamic()
	{
		return true;
	}
}

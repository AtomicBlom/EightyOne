package com.github.atomicblom.eightyone.world.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import javax.annotation.Nullable;

public class NxNTemplate extends Template
{

	private final int yOffset;
	private final int height;

	public NxNTemplate(StructureProperties structureProperties)
	{
		yOffset = structureProperties.yOffset;
		height = structureProperties.height;
	}

	public int getHeight()
	{
		return height;
	}

	public boolean isCustomRoom() {
		final BlockPos size = getSize();
		return size.getX() > 7 || size.getZ() > 7;
	}

	@Override
	public void addBlocksToWorldChunk(World worldIn, BlockPos pos, PlacementSettings placementIn)
	{
		super.addBlocksToWorldChunk(worldIn, pos, placementIn);
	}

	private BlockPos offset(BlockPos pos)
	{
		final int xOffset = (9 - getSize().getX()) / 2;
		final int zOffset = (9 - getSize().getZ()) / 2;
		return pos.add(xOffset, yOffset + 1, zOffset);
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn)
	{
		super.addBlocksToWorld(worldIn, pos, placementIn);
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags)
	{
		super.addBlocksToWorld(worldIn, pos, placementIn, flags);
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, @Nullable ITemplateProcessor templateProcessor, PlacementSettings placementIn, int flags)
	{
		super.addBlocksToWorld(worldIn, offset(pos), templateProcessor, placementIn, 0);
	}
}

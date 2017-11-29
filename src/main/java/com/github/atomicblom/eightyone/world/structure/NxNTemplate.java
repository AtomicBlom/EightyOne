package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import java.util.Map;

public class NxNTemplate extends Template
{
	private final boolean[] openEntrances = new boolean[EnumFacing.HORIZONTALS.length];
	private int yOffset;
	private boolean spawnable = true;
	private TemplateCharacteristics characteristics;

	public NxNTemplate() {
		characteristics = EntranceHelper.calculateCharacteristics(openEntrances);
	}

	public int getHeight()
	{
		return getSize().getY();
	}

	public boolean isCustomRoom() {
		final BlockPos size = getSize();
		return size.getX() > 7 || size.getZ() > 7;
	}

	@Override
	public void addBlocksToWorldChunk(World worldIn, BlockPos pos, PlacementSettings placementIn)
	{
		placementIn.setBoundingBoxFromChunk();
		super.addBlocksToWorldChunk(worldIn, offset(pos), placementIn);
	}

	public BlockPos offset(BlockPos pos)
	{
		final int xOffset = (9 - getSize().getX()) / 2;
		final int zOffset = (9 - getSize().getZ()) / 2;
		return pos.add(xOffset, yOffset + 1, zOffset);
	}

	public boolean isSpawnable()
	{
		return spawnable;
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags)
	{
		super.addBlocksToWorld(worldIn, pos, placementIn, 12);
	}

	@Override
	public void read(NBTTagCompound compound)
	{
		super.read(compound);

		final PlacementSettings placementSettings = new PlacementSettings();
		final Map<BlockPos, String> dataBlocks = getDataBlocks(BlockPos.ORIGIN, placementSettings);
		for (final Map.Entry<BlockPos, String> dataBlock : dataBlocks.entrySet()) {
			final String dataValue = dataBlock.getValue().toLowerCase();
			final BlockPos location = dataBlock.getKey();
			if (dataValue.startsWith("doorway")) {
				final EnumFacing quadrant = getQuadrant(location, getSize().getZ());
				if (quadrant == null) {
					Logger.warning("    It looks like a structure has an doorway placed on a diagonal. This is not supported, location %s", location);
				} else
				{
					Logger.info("    data block at location %s is an entrance for %s", location, quadrant);
					addEntrance(quadrant);
				}
			} else if (dataValue.startsWith("not_spawnable")){
				spawnable = false;
			}
		}
	}

	private static EnumFacing getQuadrant(BlockPos location, int length) {
		final int qa = location.getX() - location.getZ();
		final int qb = location.getX() - ((length - 1) - location.getZ());

		if (qa > 0 && qb < 0) {
			return EnumFacing.NORTH;
		}
		if (qa > 0 && qb > 0) {
			return EnumFacing.EAST;
		}
		if (qa <0 && qb < 0) {
			return EnumFacing.WEST;
		}
		if (qa < 0 && qb > 0) {
			return EnumFacing.SOUTH;
		}

		return null;
	}


	public void setYOffset(int offset) {
		yOffset = offset;
	}

	public void addEntrance(EnumFacing direction) {
		openEntrances[direction.getHorizontalIndex()] = true;
		characteristics = EntranceHelper.calculateCharacteristics(openEntrances);
	}

	public TemplateCharacteristics getCharacteristics()
	{
		return characteristics;
	}
}

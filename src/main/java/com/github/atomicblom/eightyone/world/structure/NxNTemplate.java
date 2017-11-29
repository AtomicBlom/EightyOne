package com.github.atomicblom.eightyone.world.structure;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import javax.annotation.Nullable;
import java.util.List;

public class NxNTemplate extends Template
{
	private boolean[] openEntrances = new boolean[EnumFacing.HORIZONTALS.length];
	private int yOffset;
	private Shape shape;
	private List<Rotation> currentRotations = Lists.newArrayList();

	public NxNTemplate() {}

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
		super.addBlocksToWorldChunk(worldIn, offset(pos), placementIn);
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
		super.addBlocksToWorld(worldIn, pos, placementIn, 12);
	}

	public void setYOffset(int offset) {
		yOffset = offset;
	}

	public void addEntrance(EnumFacing direction) {
		openEntrances[direction.getHorizontalIndex()] = true;
		calculateShape();
	}

	private void calculateShape() {
		int entrances = 0;
		for (boolean openEntrance : openEntrances) {
			entrances += openEntrance ? 1 : 0;
		}
		boolean north = openEntrances[EnumFacing.NORTH.getHorizontalIndex()];
		boolean south = openEntrances[EnumFacing.SOUTH.getHorizontalIndex()];
		boolean east = openEntrances[EnumFacing.EAST.getHorizontalIndex()];
		boolean west = openEntrances[EnumFacing.WEST.getHorizontalIndex()];

		switch (entrances) {
			case 0:
				this.shape = Shape.Closed;
			case 1:
				this.shape = Shape.DeadEnd;
				if (north) { this.currentRotations.add(Rotation.NONE); }
				if (east) { this.currentRotations.add(Rotation.CLOCKWISE_90); }
				if (south) { this.currentRotations.add(Rotation.CLOCKWISE_180); }
				if (west) { this.currentRotations.add(Rotation.COUNTERCLOCKWISE_90); }
				break;
			case 2:
				if (north && south) {
					this.shape = Shape.Straight;
					this.currentRotations.add(Rotation.NONE);
					this.currentRotations.add(Rotation.CLOCKWISE_180);
				} else if (east && west) {
					this.shape = Shape.Straight;
					this.currentRotations.add(Rotation.CLOCKWISE_90);
					this.currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				} else if (north && east) {
					this.shape = Shape.Corner;
					this.currentRotations.add(Rotation.NONE);
				} else if (east && south) {
					this.shape = Shape.Corner;
					this.currentRotations.add(Rotation.CLOCKWISE_90);
				} else if (south && west) {
					this.shape = Shape.Corner;
					this.currentRotations.add(Rotation.CLOCKWISE_180);
				} else if (west && north) {
					this.shape = Shape.Corner;
					this.currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				} else {
					throw new RuntimeException("Unknown rotation, please ask AtomicBlom to investigate.");
				}
				break;
			case 3:
				this.shape = Shape.TIntersection;
				if (!north) { this.currentRotations.add(Rotation.NONE); }
				if (!east) { this.currentRotations.add(Rotation.CLOCKWISE_90); }
				if (!south) { this.currentRotations.add(Rotation.CLOCKWISE_180); }
				if (!west) { this.currentRotations.add(Rotation.COUNTERCLOCKWISE_90); }
				break;
			case 4:
				this.shape = Shape.Cross;
				this.currentRotations.add(Rotation.NONE);
				this.currentRotations.add(Rotation.CLOCKWISE_90);
				this.currentRotations.add(Rotation.CLOCKWISE_180);
				this.currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				break;
			default:
				throw new RuntimeException("wtf, a room with >4 entrances?");
		}
	}

	public enum Shape {
		Closed,
		DeadEnd,
		Cross,
		TIntersection,
		Corner,
		Straight
	}


}

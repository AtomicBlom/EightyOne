package com.github.atomicblom.eightyone.util;

import com.github.atomicblom.eightyone.world.structure.Shape;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import java.util.List;

public final class EntranceHelper
{
	private EntranceHelper() {}
	public static TemplateCharacteristics calculateCharacteristics(boolean[] openEntrances) {
		int entrances = 0;
		for (final boolean openEntrance : openEntrances) {
			entrances += openEntrance ? 1 : 0;
		}
		final boolean north = openEntrances[EnumFacing.NORTH.getHorizontalIndex()];
		final boolean south = openEntrances[EnumFacing.SOUTH.getHorizontalIndex()];
		final boolean east = openEntrances[EnumFacing.EAST.getHorizontalIndex()];
		final boolean west = openEntrances[EnumFacing.WEST.getHorizontalIndex()];

		Shape shape;
		List<Rotation> currentRotations = Lists.newArrayListWithExpectedSize(2);

		switch (entrances) {
			case 0:
				shape = Shape.Closed;
				break;
			case 1:
				shape = Shape.DeadEnd;
				if (north) {
					currentRotations.add(Rotation.NONE); }
				if (east) {
					currentRotations.add(Rotation.CLOCKWISE_90); }
				if (south) {
					currentRotations.add(Rotation.CLOCKWISE_180); }
				if (west) {
					currentRotations.add(Rotation.COUNTERCLOCKWISE_90); }
				break;
			case 2:
				if (north && south) {
					shape = Shape.Straight;
					currentRotations.add(Rotation.NONE);
					currentRotations.add(Rotation.CLOCKWISE_180);
				} else if (east && west) {
					shape = Shape.Straight;
					currentRotations.add(Rotation.CLOCKWISE_90);
					currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				} else if (north && east) {
					shape = Shape.Corner;
					currentRotations.add(Rotation.NONE);
				} else if (east && south) {
					shape = Shape.Corner;
					currentRotations.add(Rotation.CLOCKWISE_90);
				} else if (south && west) {
					shape = Shape.Corner;
					currentRotations.add(Rotation.CLOCKWISE_180);
				} else if (west && north) {
					shape = Shape.Corner;
					currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				} else {
					throw new RuntimeException("Unknown rotation, please ask AtomicBlom to investigate.");
				}
				break;
			case 3:
				shape = Shape.TIntersection;
				if (!south) { currentRotations.add(Rotation.CLOCKWISE_180); }
				if (!west) { currentRotations.add(Rotation.COUNTERCLOCKWISE_90); }
				if (!north) { currentRotations.add(Rotation.NONE); }
				if (!east) { currentRotations.add(Rotation.CLOCKWISE_90); }
				
				break;
			case 4:
				shape = Shape.Cross;
				currentRotations.add(Rotation.NONE);
				currentRotations.add(Rotation.CLOCKWISE_90);
				currentRotations.add(Rotation.CLOCKWISE_180);
				currentRotations.add(Rotation.COUNTERCLOCKWISE_90);
				break;
			default:
				throw new RuntimeException("wtf, a room with >4 entrances?");
		}
		return new TemplateCharacteristics(shape, currentRotations);
	}
}

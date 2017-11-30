package com.github.atomicblom.eightyone.world;

import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.github.atomicblom.eightyone.world.structure.RoomTemplate;
import net.minecraft.util.EnumFacing;

public class Room
{
	private final int id;
	private final int x;
	private final int z;
	private final int width;
	private final int length;
	private final double templateChance;
	private RoomTemplate templateName;
	private boolean present;
	private boolean[] doorways = new boolean[EnumFacing.HORIZONTALS.length];

	public Room(int id, int x, int z, int width, int length, double templateChance) {

		this.id = id;
		this.x = x;
		this.z = z;
		this.width = width;
		this.length = length;
		this.templateChance = templateChance;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public boolean contains(int x, int z) {
		return x >= this.x && x < this.x + width
				&& z >= this.z && z < this.z + length;
	}

	public boolean isWall(int x, int z) {
		return x == this.x || x == (this.x + width - 1) ||
				z == this.z || z == (this.z + length - 1);
	}

	@Override
	public String toString()
	{
		return "Room {" +
				"id=" + id +
				", x=" + x +
				", z=" + z + '}';
	}

	public int getXOffset(int worldX)
	{
		return worldX - x;
	}

	public int getZOffset(int worldZ)
	{
		return worldZ - z;
	}

	public boolean hasSpecificTemplate()
	{
		return templateName != null;
	}

	public double getTemplateChance()
	{
		return templateChance;
	}

	public void setTemplate(RoomTemplate roomTemplate) {

		this.templateName = roomTemplate;
	}

	public RoomTemplate getTemplate() {
		return this.templateName;
	}

	public void setPresent(boolean present)
	{
		this.present = present;
	}

	public boolean isPresent()
	{
		return present;
	}

	public void setDoorwayPresent(EnumFacing direction, boolean isPresent)
	{
		this.doorways[direction.getHorizontalIndex()] = isPresent;
	}

	public boolean isDoorwayPresent(EnumFacing direction) {
		return this.doorways[direction.getHorizontalIndex()];
	}

	public TemplateCharacteristics getCharacteristics()
	{
		return EntranceHelper.calculateCharacteristics(doorways);
	}
}

package com.github.atomicblom.eightyone.world;

import java.util.BitSet;

public class Room
{
	private final int id;
	private final int x;
	private final int z;
	private final int width;
	private final int length;
	private final double templateChance;
	private final long properties;
	private String templateName;

	public Room(int id, int x, int z, int width, int length, double templateChance, long properties) {

		this.id = id;
		this.x = x;
		this.z = z;
		this.width = width;
		this.length = length;
		this.templateChance = templateChance;
		this.properties = properties;
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

	public boolean hasProperty(RoomProperties property) {
		return (properties & property.getBitMask()) != 0;
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

	public void setTemplate(String templateName) {

		this.templateName = templateName;
	}

	public String getTemplate() {
		return this.templateName;
	}
}

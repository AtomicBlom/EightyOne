package com.github.atomicblom.eightyone.world;

public enum RoomProperties
{
	VerticalExit(1),
	HorizontalExit(2),
	IsPresent(4|8)
	;

	private final int bitMask;

	RoomProperties(int bitMask)
	{

		this.bitMask = bitMask;
	}

	public long getBitMask()
	{
		return bitMask;
	}
}

package com.github.atomicblom.eightyone.blocks.tileentity;

import net.minecraft.tileentity.TileEntity;

public class TileEntityPortal extends TileEntity
{
	private float yRotation;
	private long pulse;
	private float pulseRotation;

	@Override
	public boolean hasFastRenderer()
	{
		return super.hasFastRenderer();
	}

	public float getYRotation() {
		return yRotation;
	}

	public void setYRotation(float yRotation) {
		this.yRotation = yRotation;
	}

	public void setPulse(long pulse)
	{
		this.pulse = pulse;
	}

	public long getPulse()
	{
		return pulse;
	}

	public void setPulseRotation(float pulseRotation)
	{
		this.pulseRotation = pulseRotation;
	}

	public float getPulseRotation()
	{
		return pulseRotation;
	}
}

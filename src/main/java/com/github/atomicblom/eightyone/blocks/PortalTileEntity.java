package com.github.atomicblom.eightyone.blocks;

import net.minecraft.tileentity.TileEntity;

public class PortalTileEntity extends TileEntity
{
	private float yRotation;

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


}

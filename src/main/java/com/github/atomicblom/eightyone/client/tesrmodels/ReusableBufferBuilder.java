package com.github.atomicblom.eightyone.client.tesrmodels;

import net.minecraft.client.renderer.BufferBuilder;

public class ReusableBufferBuilder extends BufferBuilder
{
	public ReusableBufferBuilder(int bufferSizeIn) {
		super(bufferSizeIn);
	}

	@Override
	public void reset() {

	}
}

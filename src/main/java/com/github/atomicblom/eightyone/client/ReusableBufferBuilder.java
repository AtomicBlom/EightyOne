package com.github.atomicblom.eightyone.client;

import net.minecraft.client.renderer.BufferBuilder;

class ReusableBufferBuilder extends BufferBuilder
{
	public ReusableBufferBuilder(int bufferSizeIn) {
		super(bufferSizeIn);
	}

	@Override
	public void reset() {

	}
}

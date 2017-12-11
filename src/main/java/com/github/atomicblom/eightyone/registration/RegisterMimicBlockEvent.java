package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.blocks.UnbreakableBlock;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RegisterMimicBlockEvent extends Event
{
	private final UnbreakableBlock unbreakable;

	public RegisterMimicBlockEvent(UnbreakableBlock unbreakable)
	{

		this.unbreakable = unbreakable;
	}

	public UnbreakableBlock getBlock()
	{
		return unbreakable;
	}
}

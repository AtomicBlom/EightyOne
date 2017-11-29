package com.github.atomicblom.eightyone.util;

import com.github.atomicblom.eightyone.world.structure.Shape;
import net.minecraft.util.Rotation;

public class TemplateCharacteristics
{
	private final Shape shape;
	private final Iterable<Rotation> templateRotations;

	public TemplateCharacteristics(Shape shape, Iterable<Rotation> templateRotations)
	{

		this.shape = shape;
		this.templateRotations = templateRotations;
	}

	public Iterable<Rotation> getTemplateRotations()
	{
		return templateRotations;
	}

	public Shape getShape()
	{
		return shape;
	}
}

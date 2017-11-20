package com.github.atomicblom.eightyone.world.structure;

import net.minecraft.world.gen.structure.template.Template;

public class TemplateAndProperties
{
	private final Template template;
	private final StructureProperties structureProperties;

	public TemplateAndProperties(Template template, StructureProperties structureProperties)
	{
		this.template = template;
		this.structureProperties = structureProperties;
	}

	public Template getTemplate()
	{
		return template;
	}

	public StructureProperties getStructureProperties()
	{
		return structureProperties;
	}
}

package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.gen.structure.template.Template;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TemplateManager
{
	private static final LoadingCache<String, TemplateAndProperties> TemplateCache;

	static {
		TemplateCache = CacheBuilder.newBuilder()
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.maximumSize(1000)
				.build(new TemplateLoader());
	}

	private static Map<String, StructureProperties> validStructures = Maps.newHashMap();
	private static List<String> validStructureNames = Lists.newArrayList();

	public static void getValidTemplates() {
		final Map<String, StructureProperties> validStructures = Maps.newHashMap();
		final List<String> validStructureNames = Lists.newArrayList();
		final StructureList structureList = getStructureList();
		if (structureList == null) {
			return;
		}

		for (final Entry<String, StructureProperties> structureFile : structureList.structureList.entrySet())
		{
			final StructureProperties value = structureFile.getValue();
			final String key = structureFile.getKey();
			if (canUseStructure(key, value)) {
				validStructures.put(key, value);
				validStructureNames.add(key);
			}
		}

		TemplateManager.validStructures = validStructures;
		TemplateManager.validStructureNames = validStructureNames;
	}

	private static StructureList getStructureList()
	{
		final Minecraft minecraft = Minecraft.getMinecraft();
		final IResourceManager resourceManager = minecraft.getResourceManager();
		try
		{
			final IResource eightyone = resourceManager.getResource(new ResourceLocation("eightyone", "structurelist.json"));
			final Gson gson = new GsonBuilder()
					.setPrettyPrinting()
					.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
					.create();
			final InputStreamReader reader = new InputStreamReader(eightyone.getInputStream());
			return gson.fromJson(reader, StructureList.class);
		} catch (final IOException e)
		{
			Logger.severe("Could not read the structure list from EightyOne. No internal structures can be rendered.");
			return null;
		}
	}

	public static TemplateAndProperties getTemplateByName(String filename) {
		try
		{
			return TemplateCache.get(filename);

		} catch (final Exception e)
		{
			return null;
		}
	}

	public static TemplateAndProperties getTemplateByChance(double templateChance)
	{
		final List<String> vsn = validStructureNames;
		final double v = vsn.size() * templateChance;
		final String selectedStructure = vsn.get((int) v);

		try
		{
			return TemplateCache.get(selectedStructure);
		} catch (final ExecutionException e)
		{
			return null;
		}
	}

	private static boolean canUseStructure(String fileName, StructureProperties properties)
	{
		try
		{
			final NBTTagCompound rawTemplate;
			final NBTTagList palette;
			final NBTTagList sizeNbt;
			try (IResource structureFileResource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("eightyone", "structures/" + fileName + ".nbt")))
			{
				rawTemplate = CompressedStreamTools.readCompressed(structureFileResource.getInputStream());
			}

			palette = rawTemplate.getTagList("palette", 10);
			for (int i = 0; i < palette.tagCount(); ++i)
			{
				final NBTTagCompound paletteEntry = palette.getCompoundTagAt(i);
				//We're going to use minecraft air to detect blocks that can't be used, so we need to explicitly ok air here
				if (paletteEntry.getString("Name") == "minecraft:air") continue;

				final IBlockState iBlockState = NBTUtil.readBlockState(paletteEntry);
				if (iBlockState == Blocks.AIR) {
					Logger.warning("Cannot use structure " + fileName + " because blockstate " + iBlockState + " is not present.");
					return false;
				}
			}

			sizeNbt = rawTemplate.getTagList("size", 3);
			properties.height = sizeNbt.getIntAt(1);

			return true;
		} catch (final IOException e) {
			return false;
		}
	}

	private static class TemplateLoader extends CacheLoader<String, TemplateAndProperties> {
		@Override
		public TemplateAndProperties load(String key) throws Exception
		{
			try
			{
				final Minecraft minecraft = Minecraft.getMinecraft();
				final DataFixer dataFixer = minecraft.getDataFixer();
				final IResourceManager resourceManager = minecraft.getResourceManager();

				final NBTTagCompound nbttagcompound;
				try (IResource eightyone = resourceManager.getResource(new ResourceLocation("eightyone", "structures/" + key + ".nbt")))
				{
					nbttagcompound = CompressedStreamTools.readCompressed(eightyone.getInputStream());
				}

				final Template template = new Template();
				template.read(dataFixer.process(FixTypes.STRUCTURE, nbttagcompound));
				return new TemplateAndProperties(template, validStructures.get(key));

			} catch (IOException exception) {

			}

			return null;
		}
	}
}

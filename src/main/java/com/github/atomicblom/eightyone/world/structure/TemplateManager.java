package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.IterableHelpers;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class TemplateManager
{
	private static final Map<ResourceLocation, NxNTemplate> TemplateCache = Maps.newHashMap();
	private static final Pattern PATH_SEPERATOR = Pattern.compile("\\\\");

	private static final List<ResourceLocation> spawnableStructureNames = Lists.newArrayList();
	private static File configurationDirectory = new File("./");

	private TemplateManager() {}

	public static void catalogueValidStructures()
	{
		Logger.info("Cataloging structures for The Labyrinth");
		for (final NxNTemplate template : TemplateCache.values())
		{
			final ResourceLocation key = template.getResourceLocation();
			if (!template.areBlocksAvailable()) {
				Logger.info("    structure %s is not valid", key);
			} else if (!template.isSpawnable())
			{
				Logger.info("    structure %s is valid, but it is not in the spawnable list", key);
			} else
			{
				Logger.info("    structure %s is valid", key);
				spawnableStructureNames.add(key);
			}
		}
	}

	public static Iterable<NBTTagCompound> catalogueMimicBlockStates() {
		Iterable<NBTTagCompound> uniqueStates = Lists.newArrayList();

		for (final NxNTemplate template : TemplateCache.values())
		{

			final List<NBTTagCompound> blockStatePalette = template.getMimicBlockStates();
			if (!blockStatePalette.isEmpty())
			{
				uniqueStates = Iterables.concat(uniqueStates, blockStatePalette);
			}
		}

		final Iterable<NBTTagCompound> finalSet = uniqueStates;

		Set<NBTTagCompound> seen = ConcurrentHashMap.newKeySet();
		return Lists.newArrayList(Streams.stream(finalSet).filter(x -> IterableHelpers.distinctNbt(x, seen)).iterator());
	}

	private static class ConfigFileModContainer extends DummyModContainer {
		@Override
		public File getSource()
		{
			return configurationDirectory;
		}

		@Override
		public String getModId()
		{
			return "eightyoneconfig";
		}

		@Override
		public String getName()
		{
			return "EightyOne Config file structures";
		}
	}

	public static void findTemplates(boolean readFromConfigDirectory) {
		final Minecraft minecraft = Minecraft.getMinecraft();
		final DataFixer dataFixer = minecraft.getDataFixer();

		spawnableStructureNames.clear();
		TemplateCache.clear();

		final List<ModContainer> activeModList = Lists.newArrayList(Loader.instance().getActiveModList());
		if (readFromConfigDirectory) {
			activeModList.clear();
			activeModList.add(new ConfigFileModContainer());

			final File nxnstructures = new File(configurationDirectory, "nxnstructures");
			if (!nxnstructures.exists())
			{
				if (!nxnstructures.mkdirs()) {
					Logger.warning("Could not create " + nxnstructures.getAbsolutePath());
				}
			}
		}

		for (final ModContainer mod : activeModList)
		{
			final String base = readFromConfigDirectory ?
					"nxnstructures" :
					"assets/" + mod.getModId() + "/nxnstructures"
					;
			CraftingHelper.findFiles(mod, base, null,
					(root, file) -> {
						final String relative = root.relativize(file).toString();
						if (!"nbt".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_"))
							return true;

						final String name = PATH_SEPERATOR.matcher(FilenameUtils.removeExtension(relative)).replaceAll("/");
						final ResourceLocation key = new ResourceLocation(mod.getModId(), name);

						Logger.info("Loading structure %s", key);

						InputStream reader = null;
						try
						{
							reader = Files.newInputStream(file);
							final NBTTagCompound rawTemplate = CompressedStreamTools.readCompressed(reader);

							final NxNTemplate template = new NxNTemplate(key);
							template.read(dataFixer.process(FixTypes.STRUCTURE, rawTemplate));

							final TemplateCharacteristics characteristics = template.getCharacteristics();

							Logger.info("    Template's shape is %s", characteristics.getShape());
							for (final Rotation rotation : characteristics.getTemplateRotations())
							{
								Logger.info("    Template could be interpreted as being rotated %s", rotation);
							}

							TemplateCache.put(key, template);

						}
						catch (final IOException ioexception)
						{
							Logger.severe("Couldn't read structure " + key + " from " + file, (Throwable)ioexception);
							return false;
						}
						finally
						{
							IOUtils.closeQuietly(reader);
						}

						return true;
					},
					true,
					true);
		}
	}

	public static NxNTemplate getTemplateByName(ResourceLocation filename) {
		return TemplateCache.get(filename);
	}

	public static RoomTemplate getTemplateByChance(TemplateCharacteristics roomCharacteristics, double templateChance, RoomPurpose purpose)
	{
		final List<RoomTemplate> validStructures = Lists.newArrayList();
		final Shape roomShape = roomCharacteristics.getShape();
		final List<Rotation> roomRotations = Lists.newArrayList(roomCharacteristics.getTemplateRotations());

		final Rotation roomRotation =
				roomRotations.isEmpty() ? Rotation.NONE :
				roomRotations.get((int)(roomRotations.size() * Math.abs(templateChance)));

		for (final ResourceLocation structureName : spawnableStructureNames)
		{
			final NxNTemplate template = TemplateCache.get(structureName);

			if (template.getPurpose() != purpose) continue;

			final TemplateCharacteristics characteristics = template.getCharacteristics();

			if (characteristics.getShape() == roomShape) {
				final List<Rotation> templateRotations = Lists.newArrayList(characteristics.getTemplateRotations());
				final Rotation templateRotation =
						templateRotations.isEmpty() ? Rotation.NONE :
						templateRotations.get((int)(templateRotations.size() * Math.abs(templateChance)));

				final Rotation rotationToApply = getRotationToApply(templateRotation, roomRotation);

				final RoomTemplate roomTemplate = new RoomTemplate(structureName, template, rotationToApply, Mirror.NONE);
				validStructures.add(roomTemplate);
			}
		}

		if (validStructures.isEmpty()) {
			return null;
		}

		final double v = validStructures.size() * Math.abs(templateChance);
		return validStructures.get((int) v);
	}

	private static final Rotation[] rotations = Rotation.values();
	private static Rotation getRotationToApply(Rotation templateRotation, Rotation roomRotation) {
		int newOrdinal = roomRotation.ordinal() - templateRotation.ordinal();
		if (newOrdinal < 0) {
			newOrdinal += 4;
		}
		return rotations[newOrdinal];
	}

	public static void setConfigurationDirectory(File configurationDirectory)
	{
		TemplateManager.configurationDirectory = configurationDirectory;
	}
}

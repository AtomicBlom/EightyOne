package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class TemplateManager
{
	//private static final LoadingCache<ResourceLocation, NxNTemplate> TemplateCache;
	private static final Map<ResourceLocation, NxNTemplate> TemplateCache = new HashMap<>();
	private static final Pattern PATH_SEPERATOR = Pattern.compile("\\\\");

//	static {
//		TemplateCache = CacheBuilder.newBuilder()
//				.expireAfterAccess(10, TimeUnit.MINUTES)
//				.maximumSize(1000)
//				.build(new TemplateLoader());
//	}

	//private static Map<ResourceLocation, NxNTemplate> validStructures = Maps.newHashMap();
	//private static List<ResourceLocation> validStructureNames = Lists.newArrayList();
	private static final List<ResourceLocation> spawnableStructureNames = Lists.newArrayList();
	private static File configurationDirectory;

	private TemplateManager() {}

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

	public static void getValidTemplates(boolean readFromConfigDirectory) {
		//final Map<ResourceLocation, NxNTemplate> validStructures = Maps.newHashMap();
		//final List<ResourceLocation> validStructureNames = Lists.newArrayList();

		final Minecraft minecraft = Minecraft.getMinecraft();
		final DataFixer dataFixer = minecraft.getDataFixer();

		spawnableStructureNames.clear();
		//TemplateCache.invalidateAll();
		//TemplateCache.cleanUp();
		TemplateCache.clear();

		final List<ModContainer> activeModList = Lists.newArrayList(Loader.instance().getActiveModList());
		if (readFromConfigDirectory) {
			activeModList.clear();
			activeModList.add(new ConfigFileModContainer());

			final File nxnstructures = new File(configurationDirectory, "nxnstructures");
			if (!nxnstructures.exists())
			{
				nxnstructures.mkdirs();
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
							if (isValidNBT(key, rawTemplate)) {
								final NxNTemplate template = new NxNTemplate(key);
								template.read(dataFixer.process(FixTypes.STRUCTURE, rawTemplate));

								final TemplateCharacteristics characteristics = template.getCharacteristics();

								Logger.info("    Template's shape is %s", characteristics.getShape());
								for (final Rotation rotation : characteristics.getTemplateRotations())
								{
									Logger.info("    Template could be interpreted as being rotated %s", rotation);
								}

								TemplateCache.put(key, template);

								//validStructureNames.add(key);
								if (template.isSpawnable())
								{
									Logger.info("    structure is valid", key);
									spawnableStructureNames.add(key);
								} else {
									Logger.info("    structure is valid, but it is not in the spawnable list", key);
								}
							} else {
								Logger.info("    structure did not have valid NBT", key);
							}
						}
						catch (final IOException ioexception)
						{
							Logger.severe("Couldn't read advancement " + key + " from " + file, (Throwable)ioexception);
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

		//TemplateManager.validStructures = validStructures;
		//TemplateManager.validStructureNames = validStructureNames;
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

	private static boolean isValidNBT(ResourceLocation resource, NBTTagCompound rawTemplate)
	{
		final NBTTagList palette = rawTemplate.getTagList("palette", 10);
		for (int i = 0; i < palette.tagCount(); ++i)
		{
			final NBTTagCompound paletteEntry = palette.getCompoundTagAt(i);
			//We're going to use minecraft air to detect blocks that can't be used, so we need to explicitly ok air here
			if (paletteEntry.getString("Name") == "minecraft:air") continue;

			final IBlockState iBlockState = NBTUtil.readBlockState(paletteEntry);
			if (iBlockState == Blocks.AIR) {
				Logger.warning("Cannot use structure " + resource + " because blockstate " + iBlockState + " is not present.");
				return false;
			}
		}

		return true;
	}

	public static void setConfigurationDirectory(File configurationDirectory)
	{
		TemplateManager.configurationDirectory = configurationDirectory;
	}

//	private static class TemplateLoader extends CacheLoader<ResourceLocation, NxNTemplate> {
//		@Override
//		public NxNTemplate load(ResourceLocation key) throws Exception
//		{
//			try
//			{
//				final Minecraft minecraft = Minecraft.getMinecraft();
//				final DataFixer dataFixer = minecraft.getDataFixer();
//				final IResourceManager resourceManager = minecraft.getResourceManager();
//
//				final NBTTagCompound nbttagcompound;
//				try (IResource eightyone = resourceManager.getResource(key))//new ResourceLocation(Reference.MOD_ID, "nxnstructures/" +key + ".nbt")))
//				{
//					nbttagcompound = CompressedStreamTools.readCompressed(eightyone.getInputStream());
//				}
//
//				final NxNTemplate template = new NxNTemplate(key);
//				template.read(dataFixer.process(FixTypes.STRUCTURE, nbttagcompound));
//				return template;
//
//			} catch (final IOException ignored) {
//
//			}
//
//			return null;
//		}
//	}
}

package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.EightyOne;
import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.UnbreakableBlock;
import com.github.atomicblom.eightyone.registration.RegisterMimicBlockEvent;
import com.github.atomicblom.eightyone.util.IterableHelpers;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.google.common.collect.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber
public final class TemplateManager
{
	private static final Map<ResourceLocation, NxNTemplate> TemplateCache = Maps.newHashMap();
	private static final Pattern PATH_SEPERATOR = Pattern.compile("\\\\");

	private static final List<ResourceLocation> spawnableStructureNames = Lists.newArrayList();
	private static File configurationDirectory = new File("./");

	private TemplateManager() {}


	private static List<UnbreakableBlock> mimicBlocks = Lists.newArrayList();

	@SubscribeEvent
	public static void onRegisterMimicBlock(RegisterMimicBlockEvent event) {
		mimicBlocks.add(event.getBlock());
	}


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

			final List<NBTTagCompound> blockStatePalette = getMimicBlockStates(template);
			if (!blockStatePalette.isEmpty())
			{
				uniqueStates = Iterables.concat(uniqueStates, blockStatePalette);
			}
		}

		final Iterable<NBTTagCompound> finalSet = uniqueStates;

		Set<NBTTagCompound> seen = ConcurrentHashMap.newKeySet();
		return Lists.newArrayList(Streams.stream(finalSet).filter(x -> IterableHelpers.distinctNbt(x, seen)).iterator());
	}

	public static void reload() {
		for (NxNTemplate template : TemplateManager.TemplateCache.values()) {
			NBTTagCompound sourceTagCompound = template.getSourceTagCompound();
			NBTTagList palette = sourceTagCompound.getTagList("palette", 10);
			NBTTagList blocks = sourceTagCompound.getTagList("blocks", 10);
			HashMap<IBlockState, Integer> remapping = Maps.newHashMap();

			final String dungeonBlockRegistryName = Reference.Blocks.DUNGEON_BLOCK.toString();
			final String secretBlockRegistryName = Reference.Blocks.SECRET_BLOCK.toString();
			boolean hasMimicBlocks = false;
			for (int i = 0; i < palette.tagCount(); i++)
			{
				final NBTTagCompound blockStateNbt = palette.getCompoundTagAt(i);
				final String name = blockStateNbt.getString("Name");

				if (name.equals(dungeonBlockRegistryName) || name.equals(secretBlockRegistryName)) {
					hasMimicBlocks = true;
				}
			}
			if (!hasMimicBlocks) continue;

			for (int i = 0; i < blocks.tagCount(); i++)
			{
				final NBTTagCompound blockStateNbt = blocks.getCompoundTagAt(i);
				if (blockStateNbt.hasKey("nbt")) {
					final NBTTagCompound tileEntityNbt = blockStateNbt.getCompoundTag("nbt");

					final String id = tileEntityNbt.getString("id");
					if ("minecraft:dungeon_block".equals(id)) {
						if (tileEntityNbt.hasKey("source")) {
							IBlockState sourceState = NBTUtil.readBlockState(tileEntityNbt.getCompoundTag("source"));

							if (!remapping.containsKey(sourceState)) {
								for (UnbreakableBlock mimicBlock : mimicBlocks) {
									IBlockState newState = mimicBlock.getMimicStateForBlockState(sourceState);

									if (newState != null) {
										NBTTagCompound paletteTag = new NBTTagCompound();
										NBTUtil.writeBlockState(paletteTag, newState);
										int index = palette.tagCount();
										palette.appendTag(paletteTag);
										remapping.put(sourceState, index);
										break;
									}
								}
							}
							Integer paletteIndex = remapping.get(sourceState);
							if (paletteIndex != null) {
								//Time to overwrite some data!
								blockStateNbt.removeTag("nbt");
								blockStateNbt.setInteger("state", paletteIndex);
							}
						}
					}
				}
			}

			template.read(sourceTagCompound);
		}

		//findTemplates(false);
	}

	public static List<NBTTagCompound> getMimicBlockStates(NxNTemplate template) {
		NBTTagCompound sourceTagCompound = template.getSourceTagCompound();
		final NBTTagList palette = sourceTagCompound.getTagList("palette", 10);


		String[] blockNames = new String[palette.tagCount()];

		boolean hasMimicBlocks = false;

		final String dungeonBlockRegistryName = Reference.Blocks.DUNGEON_BLOCK.toString();
		final String secretBlockRegistryName = Reference.Blocks.SECRET_BLOCK.toString();

		for (int i = 0; i < palette.tagCount(); i++)
		{
			final NBTTagCompound blockStateNbt = palette.getCompoundTagAt(i);
			final String name = blockStateNbt.getString("Name");

			if (name.equals(dungeonBlockRegistryName) || name.equals(secretBlockRegistryName)) {
				blockNames[i] = name;
				hasMimicBlocks = true;
			}
		}

		final List<NBTTagCompound> returnedList = Lists.newArrayList();

		if (!hasMimicBlocks) return returnedList;


		final NBTTagList blocks = sourceTagCompound.getTagList("blocks", 10);

		for (int i = 0; i < blocks.tagCount(); i++)
		{
			final NBTTagCompound blockStateNbt = blocks.getCompoundTagAt(i);
			if (blockStateNbt.hasKey("nbt")) {
				final NBTTagCompound tileEntityNbt = blockStateNbt.getCompoundTag("nbt");

				final String id = tileEntityNbt.getString("id");
				if ("minecraft:dungeon_block".equals(id)) {
					if (tileEntityNbt.hasKey("source")) {
						final NBTTagCompound source = tileEntityNbt.getCompoundTag("source").copy();
						source.setString("Type", blockNames[blockStateNbt.getInteger("state")]);
						returnedList.add(source);
					}
				}
			}
		}

		Set<NBTTagCompound> seen = ConcurrentHashMap.newKeySet();
		return Lists.newArrayList(returnedList.stream().filter(x -> IterableHelpers.distinctNbt(x, seen)).iterator());
	}

	static Set<ResourceLocation> seenLootTables = Sets.newConcurrentHashSet();
	public static void notifyLootTable(ResourceLocation lootTableName)
	{

		if (!seenLootTables.contains(lootTableName))
		{
			Logger.info("Registering new Loot Table %s", lootTableName);
			seenLootTables.add(lootTableName);
			try
			{
				LootTableList.register(lootTableName);
			} catch (Exception e) {}
		}
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
		final DataFixer dataFixer;
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			dataFixer = Minecraft.getMinecraft().getDataFixer();
		} else {
			dataFixer= FMLServerHandler.instance().getServer().getDataFixer();
		}

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

		Logger logger = EightyOne.DEBUG_SHOW_ROOM_INFO ? Logger.INSTANCE : Logger.NO_LOG;

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

						logger.info("Loading structure %s", key);

						InputStream reader = null;
						try
						{
							reader = Files.newInputStream(file);
							final NBTTagCompound rawTemplate = CompressedStreamTools.readCompressed(reader);

							final NxNTemplate template = new NxNTemplate(key);
							template.read(dataFixer.process(FixTypes.STRUCTURE, rawTemplate));

							final TemplateCharacteristics characteristics = template.getCharacteristics();

							logger.info("    Template's shape is %s", characteristics.getShape());
							for (final Rotation rotation : characteristics.getTemplateRotations())
							{
								logger.info("    Template could be interpreted as being rotated %s", rotation);
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

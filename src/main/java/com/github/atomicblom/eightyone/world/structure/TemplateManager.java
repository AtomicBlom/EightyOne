package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.FileSystem;
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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TemplateManager
{
	private static final LoadingCache<ResourceLocation, NxNTemplate> TemplateCache;

	static {
		TemplateCache = CacheBuilder.newBuilder()
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.maximumSize(1000)
				.build(new TemplateLoader());
	}

	private static Map<ResourceLocation, NxNTemplate> validStructures = Maps.newHashMap();
	private static List<ResourceLocation> validStructureNames = Lists.newArrayList();
	private static List<ResourceLocation> spawnableStructureNames = Lists.newArrayList();

	public static void getValidTemplates() {
		final Map<ResourceLocation, NxNTemplate> validStructures = Maps.newHashMap();
		final List<ResourceLocation> validStructureNames = Lists.newArrayList();

		for (final ModContainer mod : Loader.instance().getActiveModList())
		{
			CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "nxnstructures", null,
					(root, file) -> {
						final String relative = root.relativize(file).toString();
						if (!"nbt".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_"))
							return true;

						final String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
						final ResourceLocation key = new ResourceLocation(mod.getModId(), name);
						InputStream reader = null;

						try
						{

							reader = Files.newInputStream(file);
							final NBTTagCompound rawTemplate = CompressedStreamTools.readCompressed(reader);
							if (isValidNBT(key, rawTemplate)) {
								NxNTemplate template = new NxNTemplate();
								template.read(rawTemplate);

								validStructures.put(key, template);
								validStructureNames.add(key);
								if (template.isSpawnable())
								{
									spawnableStructureNames.add(key);
								}
							}
						}
						catch (IOException ioexception)
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

		TemplateManager.validStructures = validStructures;
		TemplateManager.validStructureNames = validStructureNames;
	}

	public static NxNTemplate getTemplateByName(ResourceLocation filename) {
		try
		{
			return TemplateCache.get(filename);

		} catch (final Exception e)
		{
			return null;
		}
	}

	public static NxNTemplate getTemplateByChance(double templateChance)
	{
		final List<ResourceLocation> vsn = spawnableStructureNames;
		final double v = vsn.size() * Math.abs(templateChance);
		final ResourceLocation selectedStructure = vsn.get((int) v);

		try
		{
			return TemplateCache.get(selectedStructure);
		} catch (final ExecutionException e)
		{
			return null;
		}
	}

	private static boolean isValidNBT(ResourceLocation resource, NBTTagCompound rawTemplate)
	{
		final NBTTagList palette;

		palette = rawTemplate.getTagList("palette", 10);
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

	private static class TemplateLoader extends CacheLoader<ResourceLocation, NxNTemplate> {
		@Override
		public NxNTemplate load(ResourceLocation key) throws Exception
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

				final NxNTemplate template = validStructures.get(key);
				template.read(dataFixer.process(FixTypes.STRUCTURE, nbttagcompound));
				return template;

			} catch (IOException exception) {

			}

			return null;
		}
	}
}

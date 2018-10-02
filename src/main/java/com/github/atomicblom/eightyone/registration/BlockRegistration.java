package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.*;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPlaceholderLootChest;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import java.util.List;

@EventBusSubscriber
public class BlockRegistration
{
	@SubscribeEvent
	public static void onMaterializeMimicBlocks(MaterializeMimicBlocks event) {
		for (final UnbreakableBlock mimicBlock : mimicBlocks)
		{
			mimicBlock.materialize();
		}
	}

	private static final List<UnbreakableBlock> mimicBlocks = Lists.newArrayList();

	@SubscribeEvent
	public static void onRegisterBlocks(Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registerBlock(registry, new PortalBlock(), Reference.Blocks.PORTAL)
			.setCreativeTab(Reference.CREATIVE_TAB);
		registerBlock(registry, new DungeonBlock(), Reference.Blocks.DUNGEON_BLOCK);
		registerBlock(registry, new SecretBlock(), Reference.Blocks.SECRET_BLOCK);
		registerBlock(registry, new PlaceholderLootChest(), Reference.Blocks.PLACEHOLDER_LOOT_CHEST);
		registerBlock(registry, new SecretPressurePlate(), Reference.Blocks.SECRET_PRESSURE_PLATE);

		// registerBlock(registry, new DarkAirBlock(), Reference.Blocks.DARK_AIR);


		final Iterable<NBTTagCompound> mimicBlockStates = TemplateManager.catalogueMimicBlockStates();

		final String dungeonBlockRegistryName = Reference.Blocks.DUNGEON_BLOCK.toString();
		final String secretBlockRegistryName = Reference.Blocks.SECRET_BLOCK.toString();

		final List<NBTTagCompound> dungeonStates = Lists.newArrayList();
		final List<NBTTagCompound> secretStates = Lists.newArrayList();

		for (final NBTTagCompound mimicTagCompound : mimicBlockStates)
		{
			if (dungeonBlockRegistryName.equals(mimicTagCompound.getString("Type")))
			{
				Logger.info("Found dungeon block %s", mimicTagCompound);
				dungeonStates.add(mimicTagCompound);
			} else if (secretBlockRegistryName.equals(mimicTagCompound.getString("Type"))) {
				Logger.info("Found secret block %s", mimicTagCompound);
				secretStates.add(mimicTagCompound);
			} else {
				Logger.warning("Found unknown block %s", mimicTagCompound);
			}
		}

		final ResourceLocation dungeonBlock = Reference.Blocks.DUNGEON_BLOCK;
		for (int blockSlice = 0, blockId = 0; blockSlice < dungeonStates.size();
		     blockSlice += 16, blockId++ ) {
			final UnbreakableBlock unbreakable = new UnbreakableBlock(dungeonStates, blockSlice);
			registerBlock(registry, unbreakable, new ResourceLocation(dungeonBlock.getNamespace(), dungeonBlock.getPath() + '_' + blockId));

			MinecraftForge.EVENT_BUS.post(new RegisterMimicBlockEvent(unbreakable));

			mimicBlocks.add(unbreakable);
		}


		GameRegistry.registerTileEntity(TileEntityPortal.class, "portal");
		GameRegistry.registerTileEntity(TileEntityDungeonBlock.class, "dungeon_block");
		GameRegistry.registerTileEntity(TileEntityPlaceholderLootChest.class, "placeholder_loot_chest");
	}

	private static Block registerBlock(IForgeRegistry<Block> registry, Block block, ResourceLocation registryName)
	{
		registry.register(block
				.setRegistryName(registryName)
				.setTranslationKey(registryName.toString())
		);
		return block;
	}
}

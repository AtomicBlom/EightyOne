package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.BlockLibrary;
import com.github.atomicblom.eightyone.ItemLibrary;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.UnbreakableBlock;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import com.github.atomicblom.eightyone.client.MimicBakedModel;
import com.github.atomicblom.eightyone.client.PortalTESR;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistrationEvents
{

	@SubscribeEvent
	public static void onRenderingReady(ModelRegistryEvent evt) {
		StateMapperBase ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				if (iBlockState.getValue(Reference.Blocks.OVERLAY)) {
					return new ModelResourceLocation(iBlockState.getBlock().getRegistryName(), "overlay");
				}
				return new ModelResourceLocation(iBlockState.getBlock().getRegistryName(), "mimic");
			}
		};
		ModelLoader.setCustomStateMapper(BlockLibrary.dungeon_block, ignoreState);
		ModelLoader.setCustomStateMapper(BlockLibrary.secret_block, ignoreState);


		ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				return new ModelResourceLocation(iBlockState.getBlock().getRegistryName(), "mimic");
			}
		};

		for (final UnbreakableBlock mimicBlock : mimicBlocks)
		{
			ModelLoader.setCustomStateMapper(mimicBlock, ignoreState);
		}

		final int DEFAULT_ITEM_SUBTYPE = 0;

		ModelLoader.setCustomModelResourceLocation(ItemLibrary.dungeon_block, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.DUNGEON_BLOCK, "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.secret_block, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.SECRET_BLOCK, "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.secret_pressure_plate, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.SECRET_PRESSURE_PLATE, "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.portal, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.PORTAL, "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.placeholder_loot_chest, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.PLACEHOLDER_LOOT_CHEST, "inventory"));
	}

	private static List<UnbreakableBlock> mimicBlocks = Lists.newArrayList();

	@SubscribeEvent
	public static void onRegisterMimicBlock(RegisterMimicBlockEvent event) {
		mimicBlocks.add(event.getBlock());
	}


	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		final IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();

		registerMimicBakedModel(modelRegistry, BlockLibrary.dungeon_block.getRegistryName());
		registerMimicBakedModel(modelRegistry, BlockLibrary.secret_block.getRegistryName());

		for (final UnbreakableBlock mimicBlock : mimicBlocks)
		{
			registerMimicBakedModel(modelRegistry, mimicBlock.getRegistryName());
		}
	}

	private static void registerMimicBakedModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, ResourceLocation resourceLocation)
	{
		ModelResourceLocation mimicTag = new ModelResourceLocation(resourceLocation, "mimic");

		Object object =  modelRegistry.getObject(mimicTag);
		if (object instanceof IBakedModel) {
			IBakedModel existingModel = (IBakedModel)object;
			MimicBakedModel customModel = new MimicBakedModel(existingModel);
			modelRegistry.putObject(mimicTag, customModel);
		}

		ModelResourceLocation inventoryTag = new ModelResourceLocation(resourceLocation, "inventory");

		object =  modelRegistry.getObject(inventoryTag);
		if (object instanceof IBakedModel) {
			IBakedModel existingModel = (IBakedModel)object;
			MimicBakedModel customModel = new MimicBakedModel(existingModel);
			modelRegistry.putObject(inventoryTag, customModel);
		}
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPortal.class, new PortalTESR());
	}
}

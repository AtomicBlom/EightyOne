package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.Portal;
import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class RegistrationEvents
{
	@SubscribeEvent
	public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		final ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, "portal");
		registry.register(new Portal()
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);

		GameRegistry.registerTileEntity(PortalTileEntity.class, "portal");
	}

	@SubscribeEvent
	public static void onRegisterEnchantments(RegistryEvent.Register<Enchantment> event) {
		EnumEnchantmentType mossyCobblestoneEnchant = EnumHelper.addEnchantmentType("eightyone:mossycobblestone", x -> x instanceof ItemBlock && ((ItemBlock) x).getBlock() == Blocks.MOSSY_COBBLESTONE);
		event.getRegistry().register(new MysticMossyEnchantment(mossyCobblestoneEnchant)
				.setName("mystical")
				.setRegistryName(new ResourceLocation(Reference.MOD_ID, "mystic_cobblestone"))
		);
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		ItemStack heldItemStack = event.getPlayer().getHeldItem(event.getHand());
		Item item = heldItemStack.getItem();
		if (item instanceof MossyBlockItem && heldItemStack.isItemEnchanted()) {
			BlockSnapshot blockSnapshot = event.getBlockSnapshot();
			World world = blockSnapshot.getWorld();
			world.setBlockState(blockSnapshot.getPos(),BlockLibrary.portal.getDefaultState());
		}
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		final ResourceLocation registryName = BlockLibrary.portal.getRegistryName();
		registry.register(new ItemBlock(BlockLibrary.portal)
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);

		registry.register(new MossyBlockItem(Blocks.MOSSY_COBBLESTONE).setRegistryName("minecraft", "mossy_cobblestone"));
	}

	private static class MossyBlockItem extends ItemBlock {

		public MossyBlockItem(Block block) {
			super(block);
		}

		@Override
		public boolean isEnchantable(ItemStack stack) {
			return stack.getCount() == 1;
		}

		@Override
		public int getItemEnchantability() {
			return 1;
		}
	}
}

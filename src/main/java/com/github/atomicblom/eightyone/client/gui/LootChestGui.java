package com.github.atomicblom.eightyone.client.gui;

import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPlaceholderLootChest;
import com.github.atomicblom.eightyone.networking.PacketUpdateLootChest;
import com.github.atomicblom.eightyone.registration.PacketHandler;
import net.minecraft.block.BlockChest;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

public class LootChestGui extends GuiScreen
{
	private final BlockPos location;
	private final World world;
	private GuiButton done;
	private GuiButton cancel;
	private GuiButton chestType;
	private GuiTextField lootTableName;
	private BlockChest.Type type = BlockChest.Type.BASIC;


	public LootChestGui(World world, int x, int y, int z)
	{
		this.world = world;
		location = new BlockPos(x, y, z);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();

		this.drawString(this.fontRenderer, "Loot Table Resource Location", this.width / 2 - 150, 40, 10526880);
		lootTableName.drawTextBox();

		super.drawScreen(mouseX, mouseY, partialTicks);

	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public void initGui() {
		final TileEntity tileEntity = world.getTileEntity(location);
		String lootTableName = "";
		if (tileEntity instanceof TileEntityPlaceholderLootChest) {
			final TileEntityPlaceholderLootChest lootChest = (TileEntityPlaceholderLootChest) tileEntity;
			final ResourceLocation resourceLocation = lootChest.getLootTableName();
			lootTableName = resourceLocation != null ? resourceLocation.toString() : "";
			type = lootChest.getChestType();
		}

		Keyboard.enableRepeatEvents(true);
		this.buttonList.add(this.done = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
		this.buttonList.add(this.cancel = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
		this.buttonList.add(this.chestType = new GuiButton(2, this.width / 2 - 150, 50 + 40, 150, 20, type.name()));
		this.lootTableName = new GuiTextField(2, this.fontRenderer, this.width / 2 - 150, 50, 300, 20);
		this.lootTableName.setMaxStringLength(32500);
		this.lootTableName.setFocused(true);
		this.lootTableName.setText(lootTableName);
	}
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		this.lootTableName.textboxKeyTyped(typedChar, keyCode);

		if (keyCode != 28 && keyCode != 156)
		{
			if (keyCode == 1)
			{
				this.actionPerformed(this.cancel);
			}
		}
		else
		{
			this.actionPerformed(this.done);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button == chestType) {
			type = type == BlockChest.Type.BASIC ? BlockChest.Type.TRAP : BlockChest.Type.BASIC;
			chestType.displayString = type.name();
			return;
		}

		if (button == done) {
			//send confirmation packet to server
			PacketHandler.INSTANCE.sendToServer(new PacketUpdateLootChest(location, new ResourceLocation(lootTableName.getText()), type));
		}

		mc.displayGuiScreen(null);
		if (mc.currentScreen == null)
			mc.setIngameFocus();
	}
}

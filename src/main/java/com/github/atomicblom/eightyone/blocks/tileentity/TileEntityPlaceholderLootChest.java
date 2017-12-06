package com.github.atomicblom.eightyone.blocks.tileentity;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEntityPlaceholderLootChest extends TileEntity
{
	private ResourceLocation lootTableName;
	private BlockChest.Type chestType = BlockChest.Type.BASIC;

	public void setLootTableName(ResourceLocation lootTableName) {
		this.lootTableName = lootTableName;
		markDirty();
		if (world != null) {
			IBlockState state = world.getBlockState(getPos());
			world.notifyBlockUpdate(getPos(), state, state, 3);
		}
	}

	public ResourceLocation getLootTableName() {
		return lootTableName;
	}

	public void setChestType(BlockChest.Type chestType)
	{
		this.chestType = chestType;
		markDirty();
		if (world != null) {
			IBlockState state = world.getBlockState(getPos());
			world.notifyBlockUpdate(getPos(), state, state, 3);
		}
	}

	public BlockChest.Type getChestType()
	{
		return chestType;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		// getUpdateTag() is called whenever the chunkdata is sent to the
		// client. In contrast getUpdatePacket() is called when the tile entity
		// itself wants to sync to the client. In many cases you want to send
		// over the same information in getUpdateTag() as in getUpdatePacket().
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		// Prepare a packet for syncing our TE to the client. Since we only have to sync the stack
		// and that's all we have we just write our entire NBT here. If you have a complex
		// tile entity that doesn't need to have all information on the client you can write
		// a more optimal NBT here.
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		// Here we get the packet from the server and read it into our client side tile entity
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		if (compound.hasKey("LootTable")) {
			lootTableName = new ResourceLocation(compound.getString("LootTable"));
		}
		if (compound.hasKey("ChestType")) {
			chestType = BlockChest.Type.valueOf(compound.getString("ChestType"));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		final NBTTagCompound nbtTagCompound = super.writeToNBT(compound);
		if (lootTableName != null)
		{
			compound.setString("LootTable", lootTableName.toString());
		}
		compound.setString("ChestType", chestType.name());
		return nbtTagCompound;
	}
}

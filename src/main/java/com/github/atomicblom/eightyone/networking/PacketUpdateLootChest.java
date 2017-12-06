package com.github.atomicblom.eightyone.networking;

import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPlaceholderLootChest;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateLootChest implements IMessage
{
    private BlockPos lootChestLocation;
    private ResourceLocation lootTableName;
    private BlockChest.Type chestType;

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        // Encoding the position as a long is more efficient
        lootChestLocation = BlockPos.fromLong(packetBuffer.readLong());
        lootTableName = new ResourceLocation(packetBuffer.readString(255));
        chestType = BlockChest.Type.valueOf(packetBuffer.readString(255));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        // Encoding the position as a long is more efficient
        packetBuffer.writeLong(lootChestLocation.toLong());
        packetBuffer.writeString(lootTableName.toString());
        packetBuffer.writeString(chestType.name());
    }

    public PacketUpdateLootChest() {}

    public PacketUpdateLootChest(BlockPos lootChestLocation, ResourceLocation lootTableName, BlockChest.Type chestType) {
        this.lootChestLocation = lootChestLocation;
        this.lootTableName = lootTableName;
        this.chestType = chestType;
    }

    public static class Handler implements IMessageHandler<PacketUpdateLootChest, IMessage>
    {
        @Override
        public IMessage onMessage(PacketUpdateLootChest message, MessageContext ctx) {
            // Always use a construct like this to actually handle your message. This ensures that
            // your 'handle' code is run on the main Minecraft thread. 'onMessage' itself
            // is called on the networking thread so it is not safe to do a lot of things
            // here.
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateLootChest message, MessageContext ctx) {
            // This code is run on the server side. So you can do server-side calculations here
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            // Check if the block (chunk) is loaded to prevent abuse from a client
            // trying to overload a server by randomly loading chunks
            if (world.isBlockLoaded(message.lootChestLocation)) {
                final TileEntity tileEntity = world.getTileEntity(message.lootChestLocation);
                if (tileEntity instanceof TileEntityPlaceholderLootChest) {
                    final TileEntityPlaceholderLootChest lootChest = (TileEntityPlaceholderLootChest) tileEntity;
                    lootChest.setLootTableName(message.lootTableName);
                    lootChest.setChestType(message.chestType);
                    lootChest.markDirty();
                }
            }
        }
    }
}

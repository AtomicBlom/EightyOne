package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.EightyOneTeleporter;
import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import javax.annotation.Nullable;
import java.util.Random;

public class PortalBlock extends Block implements ITileEntityProvider
{
	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public PortalBlock()
	{
		super(Material.ROCK, MapColor.GRAY);
		setTickRandomly(true);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityPortal))
		{
			return false;
		}
		final TileEntityPortal tileEntityPortal = (TileEntityPortal)tileEntity;

		if (player.isSneaking()) return false;

		if (!tileEntityPortal.checkValidStructure()) {
			return false;
		}

		attemptSendPlayer(player);

		return true;
	}

	private static void attemptSendPlayer(Entity entity) {

		if (entity.isDead || entity.world.isRemote) {
			return;
		}

		if (entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss()) {
			return;
		}

		if (entity.timeUntilPortal > 0) {
			return;
		}

		// set a cooldown before this can run again
		entity.timeUntilPortal = 10;

		final int destination = entity.dimension == Reference.DIMENSION_ID ? Reference.ORIGIN_DIMENSION_ID : Reference.DIMENSION_ID;

		entity.changeDimension(destination, EightyOneTeleporter.getTeleporterForDim(entity.getServer(), destination));

		if (destination == Reference.DIMENSION_ID && entity instanceof EntityPlayerMP) {
			final EntityPlayerMP playerMP = (EntityPlayerMP) entity;
			playerMP.setSpawnChunk(new BlockPos(playerMP), true, Reference.DIMENSION_ID);
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityPortal();
	}

	@Override
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
	{
		Reference.CURRENT_RENDER_LAYER = layer;
		return (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT);
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}
}

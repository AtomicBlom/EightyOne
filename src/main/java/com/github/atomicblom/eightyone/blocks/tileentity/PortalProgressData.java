package com.github.atomicblom.eightyone.blocks.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalProgressData {
    private final TileEntityPortal.BlockType type;
    public final BlockPos pos;

    public final int renderSet;
    public boolean currentlyValid = false;
    public boolean currentlyAir = true;
    public double distanceFromPlayer;
    public Block currentBlock;
    private IBlockState currentBlockState;

    public PortalProgressData(TileEntityPortal.BlockType type, BlockPos pos, int renderSet)
    {
        this.type = type;
        this.pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        this.renderSet = 21 - renderSet;
    }

    public boolean checkBlock(Block expectedBlock) {
        if (currentlyAir) { return false; }
        currentlyValid = false;
        currentlyAir = false;
        if (expectedBlock == null)
        {
            currentlyAir = true;
            return false;
        }

        if (currentBlock == expectedBlock) {
            currentlyValid = true;
        }
        return currentlyValid;
    }

    public void updateCurrentBlock(World world)
    {
        currentBlockState = world.getBlockState(pos);
        currentBlock = currentBlockState.getBlock();
        currentlyAir = currentBlock.isAir(currentBlockState, world, pos);
        if (currentlyAir) {
            currentlyValid = false;
        }
    }

    public TileEntityPortal.BlockType getType() {
        return type;
    }
}

package com.github.atomicblom.eightyone.blocks;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;

public class SecretPressurePlate extends BlockPressurePlate {
    public SecretPressurePlate() {
        super(Material.ROCK, Sensitivity.EVERYTHING);
        this.setHardness(0.5F);
        this.setSoundType(SoundType.STONE);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }
}

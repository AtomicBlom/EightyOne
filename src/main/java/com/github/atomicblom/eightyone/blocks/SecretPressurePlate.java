package com.github.atomicblom.eightyone.blocks;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class SecretPressurePlate extends BlockPressurePlate {
    protected SecretPressurePlate() {
        super(Material.ROCK, Sensitivity.EVERYTHING);
        this.setHardness(0.5F);
        this.setSoundType(SoundType.STONE);
    }
}

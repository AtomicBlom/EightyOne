package com.github.atomicblom.eightyone.blocks.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class SecretMaterial extends Material {
    public SecretMaterial() {
        super(MapColor.AIR);
    }

    @Override
    public boolean blocksLight() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean blocksMovement() {
        return false;
    }
}

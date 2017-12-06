package com.github.atomicblom.eightyone.blocks.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class DungeonMaterial extends Material {

    public DungeonMaterial() {
        super(MapColor.GRAY);
        setNoPushMobility();
        setImmovableMobility();
    }
}

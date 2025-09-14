package com.megacreative;

import org.bukkit.Material;

public class MaterialTest {
    public static void main(String[] args) {
        // Test material matching
        System.out.println("DIAMOND_BLOCK: " + Material.matchMaterial("DIAMOND_BLOCK"));
        System.out.println("COBBLESTONE: " + Material.matchMaterial("COBBLESTONE"));
        System.out.println("PISTON: " + Material.matchMaterial("PISTON"));
    }
}
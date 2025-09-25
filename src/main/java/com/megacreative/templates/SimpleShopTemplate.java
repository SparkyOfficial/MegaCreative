package com.megacreative.templates;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Template factory for creating common script templates
 * Provides pre-built scripts for common use cases like shops, kits, etc.
 */
public class SimpleShopTemplate {
    
    /**
     * Creates a simple shop template
     * @return A CodeScript representing a basic shop system
     */
    public static CodeScript createSimpleShopTemplate() {
        // Root block - triggered when player interacts with shop sign
        CodeBlock rootBlock = new CodeBlock("DIAMOND_BLOCK", "onBlockPlace");
        rootBlock.setParameter("blockType", DataValue.of("OAK_WALL_SIGN"));
        
        // Check if player has enough money
        CodeBlock hasMoneyCondition = new CodeBlock("OAK_PLANKS", "hasItem");
        hasMoneyCondition.setParameter("item", DataValue.of("GOLD_INGOT"));
        hasMoneyCondition.setParameter("amount", DataValue.of("10"));
        rootBlock.setNextBlock(hasMoneyCondition);
        
        // If condition - player has enough money
        CodeBlock ifHasMoney = new CodeBlock("OBSIDIAN", "ifVarEquals");
        ifHasMoney.setParameter("variable", DataValue.of("hasItem"));
        ifHasMoney.setParameter("value", DataValue.of("true"));
        hasMoneyCondition.setNextBlock(ifHasMoney);
        
        // Remove money from player
        CodeBlock removeMoney = new CodeBlock("COBBLESTONE", "removeItems");
        removeMoney.setParameter("item", DataValue.of("GOLD_INGOT"));
        removeMoney.setParameter("amount", DataValue.of("10"));
        ifHasMoney.setNextBlock(removeMoney);
        
        // Give item to player
        CodeBlock giveItem = new CodeBlock("COBBLESTONE", "giveItem");
        giveItem.setParameter("item", DataValue.of("DIAMOND"));
        giveItem.setParameter("amount", DataValue.of("1"));
        removeMoney.setNextBlock(giveItem);
        
        // Send success message
        CodeBlock successMessage = new CodeBlock("COBBLESTONE", "sendMessage");
        successMessage.setParameter("message", DataValue.of("§aYou bought a diamond for 10 gold!"));
        giveItem.setNextBlock(successMessage);
        
        // Else block - player doesn't have enough money
        CodeBlock elseBlock = new CodeBlock("END_STONE", "else");
        ifHasMoney.setNextBlock(elseBlock);
        
        // Send failure message
        CodeBlock failureMessage = new CodeBlock("COBBLESTONE", "sendMessage");
        failureMessage.setParameter("message", DataValue.of("§cYou need 10 gold ingots to buy a diamond!"));
        elseBlock.setNextBlock(failureMessage);
        
        // Create the script
        CodeScript shopTemplate = new CodeScript("Simple Shop", true, rootBlock);
        shopTemplate.setDescription("A basic shop system where players can buy diamonds with gold");
        shopTemplate.setAuthor("System");
        shopTemplate.setId(UUID.randomUUID());
        
        return shopTemplate;
    }
    
    /**
     * Creates a kit starter template
     * @return A CodeScript representing a kit starter system
     */
    public static CodeScript createKitStarterTemplate() {
        // Root block - triggered when player joins for the first time
        CodeBlock rootBlock = new CodeBlock("DIAMOND_BLOCK", "onJoin");
        rootBlock.setParameter("firstJoinOnly", DataValue.of("true"));
        
        // Give starter items
        CodeBlock giveItems = new CodeBlock("COBBLESTONE", "giveItems");
        rootBlock.setNextBlock(giveItems);
        
        // Send welcome message
        CodeBlock welcomeMessage = new CodeBlock("COBBLESTONE", "sendMessage");
        welcomeMessage.setParameter("message", DataValue.of("§aWelcome to the server! Here's your starter kit."));
        giveItems.setNextBlock(welcomeMessage);
        
        // Create the script
        CodeScript kitTemplate = new CodeScript("Kit Starter", true, rootBlock);
        kitTemplate.setDescription("Gives new players a starter kit when they first join");
        kitTemplate.setAuthor("System");
        kitTemplate.setId(UUID.randomUUID());
        
        return kitTemplate;
    }
}
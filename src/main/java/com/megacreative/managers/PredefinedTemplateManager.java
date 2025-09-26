package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.templates.SimpleShopTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages predefined templates that come with the plugin
 * These templates provide examples and starting points for users
 */
public class PredefinedTemplateManager {
    
    private final MegaCreative plugin;
    private final Logger logger;
    private final List<CodeScript> predefinedTemplates = new ArrayList<>();
    
    public PredefinedTemplateManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadPredefinedTemplates();
    }
    
    /**
     * Loads all predefined templates
     */
    private void loadPredefinedTemplates() {
        logger.info("Loading predefined templates...");
        
        try {
            // Load simple shop template
            CodeScript shopTemplate = SimpleShopTemplate.createSimpleShopTemplate();
            predefinedTemplates.add(shopTemplate);
            logger.info("Loaded template: " + shopTemplate.getName());
            
            // Load kit starter template
            CodeScript kitTemplate = SimpleShopTemplate.createKitStarterTemplate();
            predefinedTemplates.add(kitTemplate);
            logger.info("Loaded template: " + kitTemplate.getName());
            
            logger.info("Successfully loaded " + predefinedTemplates.size() + " predefined templates");
        } catch (Exception e) {
            logger.severe("Failed to load predefined templates: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets all predefined templates
     * @return List of predefined templates
     */
    public List<CodeScript> getPredefinedTemplates() {
        return new ArrayList<>(predefinedTemplates);
    }
    
    /**
     * Finds a predefined template by name
     * @param name The name of the template to find
     * @return The template if found, null otherwise
     */
    public CodeScript getPredefinedTemplate(String name) {
        return predefinedTemplates.stream()
                .filter(template -> template.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Adds all predefined templates to the main template manager
     */
    public void registerTemplates() {
        for (CodeScript template : predefinedTemplates) {
            // Check if template already exists to avoid duplicates
            CodeScript existing = plugin.getServiceRegistry().getTemplateManager().getTemplate(template.getName());
            if (existing == null) {
                plugin.getServiceRegistry().getTemplateManager().saveTemplate(template);
                logger.info("Registered predefined template: " + template.getName());
            }
        }
    }
    
    /**
     * Shuts down the predefined template manager
     */
    public void shutdown() {
        predefinedTemplates.clear();
    }
}
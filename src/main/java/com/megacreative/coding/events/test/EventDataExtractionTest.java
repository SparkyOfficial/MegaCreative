package com.megacreative.coding.events.test;

import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Test utility for verifying event data extraction functionality
 * This class can be used to validate that the unified extraction system works correctly
 */
public class EventDataExtractionTest {
    
    private static final Logger log = Logger.getLogger(EventDataExtractionTest.class.getName());
    
    /**
     * Tests the event data extraction system with a sample PlayerJoinEvent
     * @param registry The EventDataExtractorRegistry to test
     * @param event A PlayerJoinEvent to test extraction with
     * @return true if extraction works correctly
     */
    public static boolean testPlayerJoinExtraction(EventDataExtractorRegistry registry, PlayerJoinEvent event) {
        try {
            // Test data extraction
            Map<String, DataValue> extractedData = registry.extractData(event);
            
            // Verify expected variables are present
            String[] expectedVariables = {
                "playerName", "playerUUID", "playerDisplayName",
                "joinX", "joinY", "joinZ", "joinWorld", "joinLocation",
                "firstTime", "joinMessage"
            };
            
            for (String variable : expectedVariables) {
                if (!extractedData.containsKey(variable)) {
                    log.warning("Missing expected variable: " + variable);
                    return false;
                }
            }
            
            // Verify data types
            Player player = event.getPlayer();
            DataValue playerNameValue = extractedData.get("playerName");
            if (!player.getName().equals(playerNameValue.asString())) {
                log.warning("Player name extraction mismatch");
                return false;
            }
            
            log.info("PlayerJoin extraction test passed! Extracted " + extractedData.size() + " variables");
            return true;
            
        } catch (Exception e) {
            log.severe("PlayerJoin extraction test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Logs all available extractors and their provided variables
     * @param registry The registry to inspect
     */
    public static void logExtractorInfo(EventDataExtractorRegistry registry) {
        log.info("=== Event Data Extractor Registry Info ===");
        log.info("Total extractors registered: " + registry.getExtractorCount());
        
        for (Class<?> eventType : registry.getRegisteredEventTypes()) {
            log.info("Event: " + eventType.getSimpleName());
            
            var variables = registry.getProvidedVariables(eventType);
            log.info("  Variables (" + variables.size() + "): " + String.join(", ", variables));
            
            var descriptions = registry.getVariableDescriptions(eventType);
            for (String variable : variables) {
                log.info("    " + variable + ": " + descriptions.get(variable));
            }
        }
        log.info("===========================================");
    }
}
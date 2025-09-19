package com.megacreative.coding;

/**
 * Centralized constants for the MegaCreative plugin.
 * This class contains commonly used string literals to avoid duplication
 * and improve maintainability across the codebase.
 */
public class Constants {
    
    // Common error messages
    public static final String PLUGIN_CANNOT_BE_NULL = "Plugin cannot be null";
    public static final String PLAYER_REQUIRED_FOR_ACTION = "Player required for action: ";
    public static final String UNKNOWN_ACTION_TYPE = "Unknown action type: ";
    public static final String FAILED_TO_EXECUTE_ACTION = "Failed to execute action: ";
    public static final String ACTION_EXECUTED_SUCCESSFULLY = "Action executed successfully: ";
    public static final String SCRIPT_IS_INVALID_OR_DISABLED = "Script is invalid or disabled.";
    public static final String BLOCK_IS_NULL = "Block is null.";
    public static final String START_BLOCK_IS_NULL = "Start block is null.";
    public static final String END_OF_CHAIN_OR_CANCELLED = "End of chain or cancelled.";
    
    // Variable scope identifiers
    public static final String GLOBAL_SCOPE = "global";
    public static final String SCRIPT_UNKNOWN = "script_unknown";
    public static final String UNKNOWN_WORLD = "unknown_world";
    
    // World name patterns
    public static final String DEV_WORLD_PATTERN = "dev";
    public static final String DEV_WORLD_PATTERN_RU = "разработка";
    public static final String CREATIVE_WORLD_PATTERN = "creative";
    
    // Permission strings
    public static final String DEBUG_PERMISSION = "megacreative.debug";
    
    // Test command subcommands
    public static final String TEST_RUN = "run";
    public static final String TEST_LIST = "list";
    public static final String TEST_CREATE = "create";
    public static final String TEST_SAMPLE = "sample";
    public static final String TEST_HELP = "help";
    
    // Test command messages
    public static final String TEST_COMMAND_PLAYERS_ONLY = "§cThis command can only be used by players!";
    public static final String TEST_UNKNOWN_SUBCOMMAND = "§cUnknown subcommand: ";
    public static final String TEST_USAGE_RUN = "§cUsage: /test run <suite> [testcase]";
    public static final String TEST_RUNNER_NOT_AVAILABLE = "§cTest runner is not available!";
    public static final String TEST_RUNNING_CASE = "§eRunning test case: ";
    public static final String TEST_PASSED = "§a✓ Test passed";
    public static final String TEST_FAILED = "§c✗ Test failed";
    public static final String TEST_CASE_NOT_FOUND = "§cTest case not found: ";
    public static final String TEST_ERROR_RUNNING = "§cError running test: ";
    public static final String TEST_RUNNING_SUITE = "§eRunning test suite: ";
    public static final String TEST_ERROR_RUNNING_SUITE = "§cError running test suite: ";
    public static final String TEST_SUITE_RESULTS = "§a=== Test Suite Results ===";
    public static final String TEST_SUITE = "§7Suite: ";
    public static final String TEST_PASSED_COUNT = "§7Passed: ";
    public static final String TEST_FAILED_COUNT = "§7Failed: ";
    public static final String TEST_TOTAL_COUNT = "§7Total: ";
    public static final String TEST_FAILED_TESTS = "§cFailed tests:";
    public static final String TEST_FAILED_TEST_FORMAT = "§c  ✗ ";
    public static final String TEST_AVAILABLE_SUITES = "§e=== Available Test Suites ===";
    
    // Connection debug GUI messages
    public static final String CONNECTION_DEBUG_GUI_TITLE = "§8🔗 Связи блоков";
    public static final String BLOCK_NOT_FOUND = "Блок не найден";
    public static final String NEXT_BLOCK_ARROW = "§a→ Следующий";
    public static final String CHILD_BLOCK_ARROW = "§b↓ Дочерний";
    public static final String BLOCK_UNASSIGNED = "Неназначено";
    
    // Auto connection manager messages
    public static final String BLOCK_REMOVED_MESSAGE = "§cБлок кода удален и отсоединён от цепочки!";
    
    // Generic action messages
    public static final String TELEPORT_SUCCESS = "§aTeleported to: ";
    public static final String INVALID_LOCATION_FORMAT = "§cInvalid location format: ";
    public static final String SET_BLOCK_SUCCESS = "§aSet block at ";
    public static final String BROKE_BLOCK_SUCCESS = "§aBroke block at ";
    
    // Variable manager error messages
    public static final String CANNOT_SET_VARIABLE = "Cannot set variable: script ID is not available";
    public static final String CANNOT_SET_GLOBAL_VARIABLE = "Cannot set global variable: world ID is not available";
    public static final String CANNOT_SET_PLAYER_VARIABLE = "Cannot set player variable: player is not available";
    public static final String VARIABLE_MANAGER_NOT_AVAILABLE = "VariableManager is not available";
    public static final String INVALID_PLAYER_UUID = "Invalid player UUID: ";
    
    // Advanced script optimizer messages
    public static final String DEEPLY_NESTED_LOOP = "DeeplyNestedLoop";
    public static final String DEEPLY_NESTED_LOOP_DESC = "Loop at position ";
    public static final String DEEPLY_NESTED_LOOP_RECOMMENDATION = "Consider flattening nested loops or using more efficient data structures";
    public static final String EXPENSIVE_LOOP_OPERATION = "ExpensiveLoopOperation";
    public static final String EXPENSIVE_LOOP_OPERATION_DESC = "Loop contains expensive operation: ";
    public static final String EXPENSIVE_LOOP_OPERATION_RECOMMENDATION = "Consider moving expensive operations outside the loop or caching results";
    public static final String LONG_CONDITION_CHAIN = "LongConditionChain";
    public static final String LONG_CONDITION_CHAIN_DESC = "Conditional chain of length ";
    public static final String LONG_CONDITION_CHAIN_RECOMMENDATION = "Consider using switch statements or lookup tables for better performance";
    public static final String REDUNDANT_CONDITION = "RedundantCondition";
    public static final String REDUNDANT_CONDITION_DESC = "Redundant condition check detected";
    public static final String REDUNDANT_CONDITION_RECOMMENDATION = "Remove duplicate condition checks to improve performance";
    public static final String UNUSED_VARIABLES = "UnusedVariables";
    public static final String UNUSED_VARIABLES_DESC = "Found ";
    public static final String UNUSED_VARIABLES_RECOMMENDATION = "Remove unused variables to reduce memory usage";
    public static final String EXCESSIVE_VARIABLE_SCOPE = "ExcessiveVariableScope";
    public static final String EXCESSIVE_VARIABLE_SCOPE_DESC = "Script declares ";
    public static final String EXCESSIVE_VARIABLE_SCOPE_RECOMMENDATION = "Consider reducing variable scope or using local variables";
    public static final String LINEAR_STRUCTURE = "LinearStructure";
    public static final String LINEAR_STRUCTURE_DESC = "Script has linear structure with ";
    public static final String LINEAR_STRUCTURE_RECOMMENDATION = "Consider grouping related blocks into functions or modules";
    public static final String REPEATED_ACTIONS = "RepeatedActions";
    public static final String REPEATED_ACTIONS_DESC = "Action '";
    public static final String REPEATED_ACTIONS_RECOMMENDATION = "Consider creating a reusable function for repeated actions";
    
    // Default script engine messages
    public static final String SCRIPT_VALIDATION_FAILED = "Script validation failed: ";
    public static final String ERRORS_FOUND = " errors found. ";
    public static final String FIRST_ERROR = "First error: ";
    public static final String SCRIPT_EXECUTION_ERROR = "Script execution error: ";
    public static final String CRITICAL_SCRIPT_EXECUTION_ERROR = "Critical script execution error: ";
    public static final String CRITICAL_EXECUTION_ERROR = "Critical execution error";
    public static final String BLOCK_EXECUTION_ERROR = "Block execution error: ";
    public static final String CRITICAL_BLOCK_EXECUTION_ERROR = "Critical block execution error: ";
    public static final String BLOCK_CHAIN_EXECUTION_ERROR = "Block chain execution error: ";
    public static final String CRITICAL_BLOCK_CHAIN_EXECUTION_ERROR = "Critical block chain execution error: ";
    public static final String MAX_INSTRUCTIONS_EXCEEDED = "Max instructions per tick exceeded. Possible infinite loop detected.";
    public static final String MAX_RECURSION_EXCEEDED = "Max recursion depth exceeded.";
    public static final String UNKNOWN_BLOCK_ACTION = "Unknown block action ID: ";
    
    // File and folder names
    public static final String VARIABLES_FOLDER = "variables";
    
    // VariableManager constants
    public static final String FAILED_TO_CREATE_VARIABLES_DIR = "Failed to create variables directory: ";
    public static final String NAME_VALUE_SCOPE_CANNOT_BE_NULL = "Name, value, and scope cannot be null";
    public static final String DYNAMIC_PREFIX = "dynamic_";
    public static final String CONTEXT_AND_NAME_CANNOT_BE_NULL = "Context and name cannot be null";
    public static final String LOCAL_PREFIX = "local_";
    public static final String GLOBAL_KEY = "global";
    public static final String PLAYER_ID_CANNOT_BE_NULL = "Player ID and name cannot be null";
    public static final String PLAYER_PREFIX = "player_";
    public static final String SERVER_PREFIX = "server_";
    public static final String PERSISTENT_PREFIX = "persistent_";
    
    // TestCommand constants
    public static final String SAMPLE_SUITE_NAME = "sample";
    public static final String TEST_RESULTS_HEADER = "§e=== Test Suite Results ===";
    public static final String NO_TEST_RESULTS = "§cNo test results to display";
    public static final String SUITE_PREFIX = "§7Suite: ";
    public static final String PASSED_PREFIX = "§7Passed: ";
    public static final String FAILED_PREFIX = "§7Failed: ";
    public static final String TOTAL_PREFIX = "§7Total: ";
    public static final String FAILED_TESTS_HEADER = "§cFailed tests:";
    public static final String FAILED_TEST_FORMAT = "§c  ✗ ";
    public static final String AVAILABLE_SUITES_HEADER = "§e=== Available Test Suites ===";
    public static final String SUITE_ITEM_FORMAT = "§7• §f";
    public static final String SAMPLE_SUITE_CREATED = "§aSample test suite '";
    public static final String SAMPLE_SUITE_CREATED_SUFFIX = "' created successfully!";
    public static final String TEST_COMMAND_HEADER = "§e=== MegaCreative Test Command ===";
    public static final String TEST_COMMAND_HELP_RUN = "§7/test run <suite> [testcase] §f- Run a test suite or specific test case";
    public static final String TEST_COMMAND_HELP_LIST = "§7/test list §f- List all available test suites";
    public static final String TEST_COMMAND_HELP_CREATE = "§7/test create <suite> §f- Create a sample test suite";
    public static final String TEST_COMMAND_HELP_SAMPLE = "§7/test sample §f- Create a sample test suite named 'sample'";
    public static final String TEST_COMMAND_HELP_HELP = "§7/test help §f- Show this help message";
    
    // AdvancedScriptOptimizer constants
    public static final String APPLIED_OPTIMIZATION = "Applied optimization: ";
    public static final String APPLIED_OPTIMIZATION_TO_SCRIPT = " to script: ";
    public static final String UNKNOWN_OPTIMIZATION_TYPE = "Unknown optimization type: ";
    public static final String AVOID_EXPENSIVE_OPERATIONS_IN_LOOPS = "avoid-expensive-operations-in-loops";
    public static final String MINIMIZE_VARIABLE_LOOKUPS = "minimize-variable-lookups";
    public static final String REDUCE_NESTING_DEPTH = "reduce-nesting-depth";
    public static final String SIMPLIFY_CONDITION_CHAINS = "simplify-condition-chains";
    public static final String FOUND_EXPENSIVE_OPERATIONS = "Found ";
    public static final String FOUND_EXPENSIVE_OPERATIONS_SUFFIX = " expensive operations in loop at position ";
    public static final String EXPENSIVE_OPERATION = "  - Expensive operation: ";
    public static final String FREQUENTLY_USED_VARIABLE = "Frequently used variable: ";
    public static final String FREQUENTLY_USED_VARIABLE_SUFFIX = " times)";
    public static final String MAXIMUM_NESTING_DEPTH = "Maximum nesting depth: ";
    public static final String DEEP_NESTING_DETECTED = "Deep nesting detected (";
    public static final String DEEP_NESTING_DETECTED_SUFFIX = " levels). Consider refactoring to reduce complexity.";
    public static final String CONSECUTIVE_CONDITION_BLOCKS = "Consecutive condition blocks found at positions ";
    public static final String CONSECUTIVE_CONDITION_BLOCKS_SUFFIX = " and ";
    public static final String CONDITION_FOLLOWED_BY = "  - ";
    public static final String CONDITION_FOLLOWED_BY_SUFFIX = " followed by ";
    
    // ExecutionContext constants
    public static final String GLOBAL_SCOPE_ID = "global";
    public static final String SCRIPT_ID_PREFIX = "script_";
    public static final String BREAK_FLAG_SET = "Break flag set to: ";
    public static final String BREAK_FLAG_CLEARED = "Break flag cleared";
    public static final String CONTINUE_FLAG_SET = "Continue flag set to: ";
    public static final String CONTINUE_FLAG_CLEARED = "Continue flag cleared";
    
    // ConnectionDebugGUI constants
    public static final String CONNECTION_DEBUG_GUI_TITLE = "§8🔗 Связи блоков";
    public static final String BLOCK_NOT_FOUND = "Блок не найден";
    public static final String BLOCK_UNASSIGNED = "Неназначено";
    public static final String NEXT_BLOCK_ARROW = "§a→ Следующий";
    public static final String CHILD_BLOCK_ARROW = "§b↓ Дочерний";
    public static final String NEXT_SLOT = "next";
    public static final String CHILD_SLOT_PREFIX = "child";
    
    // DefaultScriptEngine constants
    public static final String MAX_RECURSION_EXCEEDED_IN_WHILE_LOOP = "Max recursion depth exceeded in while loop.";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}
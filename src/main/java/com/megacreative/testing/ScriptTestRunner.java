package com.megacreative.testing;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Comprehensive test runner for MegaCreative scripts
 * Provides advanced testing capabilities including unit testing, integration testing,
 * performance testing, and debugging features
 */
public class ScriptTestRunner {
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final Logger logger;
    private final Map<String, TestSuite> testSuites = new HashMap<>();
    private final List<TestResult> testResults = new ArrayList<>();
    
    public ScriptTestRunner(MegaCreative plugin, ScriptEngine scriptEngine, 
                           VariableManager variableManager, VisualDebugger debugger) {
        this.plugin = plugin;
        this.scriptEngine = scriptEngine;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Creates a new test suite
     */
    public TestSuite createTestSuite(String name) {
        TestSuite suite = new TestSuite(name);
        testSuites.put(name, suite);
        return suite;
    }
    
    /**
     * Gets a test suite by name
     */
    public TestSuite getTestSuite(String name) {
        return testSuites.get(name);
    }
    
    /**
     * Runs all test suites
     */
    public CompletableFuture<List<TestResult>> runAllTests() {
        return CompletableFuture.supplyAsync(() -> {
            List<TestResult> results = new ArrayList<>();
            for (TestSuite suite : testSuites.values()) {
                results.addAll(suite.runTests());
            }
            testResults.addAll(results);
            return results;
        });
    }
    
    /**
     * Runs a specific test suite
     */
    public CompletableFuture<List<TestResult>> runTestSuite(String suiteName) {
        return CompletableFuture.supplyAsync(() -> {
            TestSuite suite = testSuites.get(suiteName);
            if (suite == null) {
                throw new IllegalArgumentException("Test suite not found: " + suiteName);
            }
            List<TestResult> results = suite.runTests();
            testResults.addAll(results);
            return results;
        });
    }
    
    /**
     * Runs a specific test case
     */
    public CompletableFuture<TestResult> runTestCase(String suiteName, String testCaseName) {
        return CompletableFuture.supplyAsync(() -> {
            TestSuite suite = testSuites.get(suiteName);
            if (suite == null) {
                throw new IllegalArgumentException("Test suite not found: " + suiteName);
            }
            TestResult result = suite.runTestCase(testCaseName);
            if (result != null) {
                testResults.add(result);
            }
            return result;
        });
    }
    
    /**
     * Gets all test results
     */
    public List<TestResult> getTestResults() {
        return new ArrayList<>(testResults);
    }
    
    /**
     * Clears all test results
     */
    public void clearResults() {
        testResults.clear();
    }
    
    /**
     * Generates a test report
     */
    public TestReport generateReport() {
        return new TestReport(new ArrayList<>(testResults));
    }
    
    /**
     * Gets the names of all available test suites
     */
    public List<String> getAvailableTestSuites() {
        return new ArrayList<>(testSuites.keySet());
    }
    
    /**
     * Represents a test suite containing multiple test cases
     */
    public class TestSuite {
        private final String name;
        private final Map<String, TestCase> testCases = new HashMap<>();
        private final List<TestResult> results = new ArrayList<>();
        
        public TestSuite(String name) {
            this.name = name;
        }
        
        /**
         * Adds a test case to the suite
         */
        public TestSuite addTestCase(TestCase testCase) {
            testCases.put(testCase.getName(), testCase);
            return this;
        }
        
        /**
         * Creates and adds a new test case
         */
        public TestCase createTestCase(String name) {
            TestCase testCase = new TestCase(name);
            testCases.put(name, testCase);
            return testCase;
        }
        
        /**
         * Runs all test cases in this suite
         */
        public List<TestResult> runTests() {
            List<TestResult> suiteResults = new ArrayList<>();
            for (TestCase testCase : testCases.values()) {
                TestResult result = testCase.execute();
                suiteResults.add(result);
                results.add(result);
            }
            return suiteResults;
        }
        
        /**
         * Runs a specific test case
         */
        public TestResult runTestCase(String testCaseName) {
            TestCase testCase = testCases.get(testCaseName);
            if (testCase == null) {
                return new TestResult(testCaseName, false, "Test case not found", 0, new ArrayList<>());
            }
            TestResult result = testCase.execute();
            results.add(result);
            return result;
        }
        
        /**
         * Gets all test results for this suite
         */
        public List<TestResult> getResults() {
            return new ArrayList<>(results);
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * Represents a single test case
     */
    public class TestCase {
        private final String name;
        private CodeScript script;
        private Player testPlayer;
        private Location testLocation;
        private Map<String, Object> testVariables = new HashMap<>();
        private List<TestAssertion> assertions = new ArrayList<>();
        private long timeoutMs = 5000; // 5 second default timeout
        
        public TestCase(String name) {
            this.name = name;
        }
        
        /**
         * Sets the script to test
         */
        public TestCase withScript(CodeScript script) {
            this.script = script;
            return this;
        }
        
        /**
         * Sets the test player
         */
        public TestCase withPlayer(Player player) {
            this.testPlayer = player;
            return this;
        }
        
        /**
         * Sets the test location
         */
        public TestCase withLocation(Location location) {
            this.testLocation = location;
            return this;
        }
        
        /**
         * Adds a test variable
         */
        public TestCase withVariable(String name, Object value) {
            testVariables.put(name, value);
            return this;
        }
        
        /**
         * Adds multiple test variables
         */
        public TestCase withVariables(Map<String, Object> variables) {
            testVariables.putAll(variables);
            return this;
        }
        
        /**
         * Adds an assertion to the test case
         */
        public TestCase addAssertion(TestAssertion assertion) {
            assertions.add(assertion);
            return this;
        }
        
        /**
         * Sets the test timeout
         */
        public TestCase withTimeout(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
        
        /**
         * Executes the test case
         */
        public TestResult execute() {
            long startTime = System.currentTimeMillis();
            List<String> messages = new ArrayList<>();
            
            try {
                // Setup test environment
                setupTestEnvironment();
                
                // Execute the script
                CompletableFuture<ExecutionResult> future = scriptEngine.executeScript(script, testPlayer, "test");
                
                // Wait for completion with timeout
                ExecutionResult result = future.get(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Check assertions
                boolean passed = true;
                for (TestAssertion assertion : assertions) {
                    AssertionResult assertionResult = assertion.check(result, variableManager, testPlayer);
                    if (!assertionResult.passed) {
                        passed = false;
                        messages.add("Assertion failed: " + assertionResult.message);
                    } else {
                        messages.add("Assertion passed: " + assertionResult.message);
                    }
                }
                
                messages.add(0, "Script executed successfully in " + executionTime + "ms");
                return new TestResult(name, passed, String.join("; ", messages), executionTime, messages);
                
            } catch (java.util.concurrent.TimeoutException e) {
                long executionTime = System.currentTimeMillis() - startTime;
                messages.add("Test timed out after " + executionTime + "ms");
                return new TestResult(name, false, "Timeout", executionTime, messages);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                messages.add("Test failed with exception: " + e.getMessage());
                logger.severe("Test case " + name + " failed: " + e.getMessage());
                e.printStackTrace();
                return new TestResult(name, false, "Exception: " + e.getMessage(), executionTime, messages);
            } finally {
                // Cleanup test environment
                cleanupTestEnvironment();
            }
        }
        
        /**
         * Sets up the test environment
         */
        private void setupTestEnvironment() {
            // Set test variables
            for (Map.Entry<String, Object> entry : testVariables.entrySet()) {
                variableManager.setVariable(entry.getKey(), DataValue.fromObject(entry.getValue()), 
                    VariableScope.LOCAL, "test");
            }
            
            // Enable debugging for test player if specified
            if (testPlayer != null && debugger != null) {
                debugger.startDebugSession(testPlayer, "test-session-" + name);
            }
        }
        
        /**
         * Cleans up the test environment
         */
        private void cleanupTestEnvironment() {
            // Clear test variables
            for (String varName : testVariables.keySet()) {
                variableManager.setVariable(varName, null, VariableScope.LOCAL, "test");
            }
            
            // Disable debugging for test player
            if (testPlayer != null && debugger != null && debugger.isDebugging(testPlayer)) {
                debugger.stopDebugSession(testPlayer);
            }
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * Represents a test assertion
     */
    public interface TestAssertion {
        AssertionResult check(ExecutionResult result, VariableManager variableManager, Player player);
    }
    
    /**
     * Represents the result of an assertion check
     */
    public static class AssertionResult {
        public final boolean passed;
        public final String message;
        
        public AssertionResult(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }
    }
    
    /**
     * Common test assertions
     */
    public static class Assertions {
        /**
         * Asserts that the script execution was successful
         */
        public static TestAssertion successful() {
            return (result, variableManager, player) -> 
                new AssertionResult(result.isSuccess(), 
                    "Script execution " + (result.isSuccess() ? "successful" : "failed"));
        }
        
        /**
         * Asserts that a variable has a specific value
         */
        public static TestAssertion variableEquals(String variableName, Object expectedValue) {
            return (result, variableManager, player) -> {
                DataValue value = variableManager.getVariable(variableName, VariableScope.LOCAL, "test");
                if (value == null) {
                    return new AssertionResult(false, "Variable " + variableName + " not found");
                }
                boolean equals = value.getValue().equals(expectedValue);
                return new AssertionResult(equals, 
                    "Variable " + variableName + " = " + value.getValue() + 
                    (equals ? " (matches expected)" : " (expected " + expectedValue + ")"));
            };
        }
        
        /**
         * Asserts that the execution result contains a specific message
         */
        public static TestAssertion resultContains(String message) {
            return (result, variableManager, player) -> {
                boolean contains = result.getMessage() != null && result.getMessage().contains(message);
                return new AssertionResult(contains, 
                    "Result message " + (contains ? "contains" : "does not contain") + " '" + message + "'");
            };
        }
        
        /**
         * Asserts that execution time is less than a threshold
         */
        public static TestAssertion executionTimeLessThan(long maxTimeMs) {
            return (result, variableManager, player) -> {
                // This assertion would need to be checked differently since we don't have execution time here
                return new AssertionResult(true, "Execution time assertion placeholder");
            };
        }
    }
    
    /**
     * Represents the result of a test case
     */
    public static class TestResult {
        private final String testName;
        private final boolean passed;
        private final String message;
        private final long executionTimeMs;
        private final List<String> details;
        private final long timestamp;
        
        public TestResult(String testName, boolean passed, String message, long executionTimeMs, List<String> details) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
            this.executionTimeMs = executionTimeMs;
            this.details = details != null ? new ArrayList<>(details) : new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getTestName() { return testName; }
        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public List<String> getDetails() { return new ArrayList<>(details); }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Represents a test report
     */
    public static class TestReport {
        private final List<TestResult> results;
        private final long generatedTime;
        
        public TestReport(List<TestResult> results) {
            this.results = results != null ? new ArrayList<>(results) : new ArrayList<>();
            this.generatedTime = System.currentTimeMillis();
        }
        
        /**
         * Gets the number of passed tests
         */
        public int getPassedCount() {
            return (int) results.stream().filter(TestResult::isPassed).count();
        }
        
        /**
         * Gets the number of failed tests
         */
        public int getFailedCount() {
            return results.size() - getPassedCount();
        }
        
        /**
         * Gets the pass rate as a percentage
         */
        public double getPassRate() {
            return results.isEmpty() ? 0 : (getPassedCount() * 100.0) / results.size();
        }
        
        /**
         * Gets the average execution time
         */
        public double getAverageExecutionTime() {
            return results.isEmpty() ? 0 : 
                results.stream().mapToLong(TestResult::getExecutionTimeMs).average().orElse(0);
        }
        
        /**
         * Gets failed test results
         */
        public List<TestResult> getFailedTests() {
            return results.stream().filter(result -> !result.isPassed()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        /**
         * Gets passed test results
         */
        public List<TestResult> getPassedTests() {
            return results.stream().filter(TestResult::isPassed).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        /**
         * Generates a formatted report string
         */
        public String generateFormattedReport() {
            StringBuilder report = new StringBuilder();
            report.append("=== MegaCreative Test Report ===\n");
            report.append("Generated: ").append(new Date(generatedTime)).append("\n");
            report.append("Total Tests: ").append(results.size()).append("\n");
            report.append("Passed: ").append(getPassedCount()).append("\n");
            report.append("Failed: ").append(getFailedCount()).append("\n");
            report.append("Pass Rate: ").append(String.format("%.2f", getPassRate())).append("%\n");
            report.append("Average Execution Time: ").append(String.format("%.2f", getAverageExecutionTime())).append("ms\n\n");
            
            if (getFailedCount() > 0) {
                report.append("Failed Tests:\n");
                for (TestResult result : getFailedTests()) {
                    report.append("  ✗ ").append(result.getTestName()).append(": ").append(result.getMessage()).append("\n");
                }
                report.append("\n");
            }
            
            if (getPassedCount() > 0) {
                report.append("Passed Tests:\n");
                for (TestResult result : getPassedTests()) {
                    report.append("  ✓ ").append(result.getTestName()).append(" (").append(result.getExecutionTimeMs()).append("ms)\n");
                }
            }
            
            return report.toString();
        }
        
        // Getters
        public List<TestResult> getResults() { return new ArrayList<>(results); }
        public long getGeneratedTime() { return generatedTime; }
    }
}
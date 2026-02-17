// package com.example.javabackend;

// import org.springframework.web.bind.annotation.*;
// import java.io.*;
// import java.net.URL;
// import java.net.URLClassLoader;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;
// import javax.tools.JavaCompiler;
// import javax.tools.ToolProvider;
// import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
// import org.junit.platform.launcher.Launcher;
// import org.junit.platform.launcher.core.LauncherFactory;
// import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
// import org.junit.platform.launcher.listeners.TestExecutionSummary;
// import org.junit.jupiter.api.Assertions;

// @RestController
// @RequestMapping("/api")
// @CrossOrigin(origins = "*", maxAge = 3600)
// public class JavaCodeController {

//     private static final String CLASS_NAME = "Main";
//     private static final String TEST_CLASS_NAME = "MainTest";

//     // Helper method for interpreted languages (Python, JS)
//     private String runInterpreter(String fileName, String code, String... command) {
//         try {
//             try (FileWriter writer = new FileWriter(fileName)) {
//                 writer.write(code);
//             }
//             Process process = new ProcessBuilder(command).start();
            
//             if (!process.waitFor(60, TimeUnit.SECONDS)) {
//                 process.destroyForcibly();
//                 return "Error: Execution timed out.";
//             }

//             String output = readInputStream(process.getInputStream());
//             String error = readInputStream(process.getErrorStream());

//             if (!error.isEmpty()) {
//                 return "Execution error:\n" + error;
//             }
//             return output;
//         } catch (IOException | InterruptedException e) {
//             return "Server error: " + e.getMessage();
//         } finally {
//             new File(fileName).delete();
//         }
//     }

//     // Helper method for compiled languages (C, C++)
//     private String runCompiledCode(String sourceFile, String code, String compileCmd, String execCmd) {
//         try {
//             // Write the source code to a file
//             try (FileWriter writer = new FileWriter(sourceFile)) {
//                 writer.write(code);
//             }

//             // 1. Run the compilation command
//             Process compileProcess = new ProcessBuilder(compileCmd.split(" ")).start();
//             if (!compileProcess.waitFor(60, TimeUnit.SECONDS) || compileProcess.exitValue() != 0) {
//                 String error = readInputStream(compileProcess.getErrorStream());
//                 return "Compilation error:\n" + error;
//             }

//             // 2. Run the executable
//             Process execProcess = new ProcessBuilder(execCmd).start();
//             if (!execProcess.waitFor(60, TimeUnit.SECONDS)) {
//                 execProcess.destroyForcibly();
//                 return "Error: Execution timed out.";
//             }
            
//             String output = readInputStream(execProcess.getInputStream());
//             String error = readInputStream(execProcess.getErrorStream());

//             if (!error.isEmpty()) {
//                 return "Execution error:\n" + error;
//             }
//             return output;

//         } catch (IOException | InterruptedException e) {
//             return "Server error: " + e.getMessage();
//         } finally {
//             new File(sourceFile).delete();
//             new File(execCmd).delete(); 
//         }
//     }

//     private String readInputStream(InputStream is) throws IOException {
//         return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
//                 .lines().collect(Collectors.joining("\n"));
//     }
    
//     private int extractNumber(String line) {
//         try {
//             String[] parts = line.split("\\s+");
//             for (String part : parts) {
//                 if (part.matches("\\d+")) {
//                     return Integer.parseInt(part);
//                 }
//             }
//         } catch (Exception e) {
//             // Ignore parsing errors
//         }
//         return 0;
//     }
    
// // API endpoint for Java code with test cases
// @PostMapping("/run-java")
// public Map<String, Object> runTestCases(@RequestBody Map<String, Object> requestBody) {
//     String javaCode = (String) requestBody.get("code");
//     java.util.List<Map<String, Object>> testCases = (java.util.List<Map<String, Object>>) requestBody.get("testCases");

//     File javaFile = new File(CLASS_NAME + ".java");
//     File testFile = new File(TEST_CLASS_NAME + ".java");
//     Map<String, Object> response = new HashMap<>();

//     try {
//         // Write user's code
//         try (FileWriter writer = new FileWriter(javaFile)) {
//             writer.write(javaCode);
//         }
        
//         // Generate and write test code
//         String testCode = generateTestClass(testCases);
//         try (FileWriter writer = new FileWriter(testFile)) {
//             writer.write(testCode);
//         }

//         // Build comprehensive classpath for compilation
//         StringBuilder classpathBuilder = new StringBuilder();
//         classpathBuilder.append(System.getProperty("java.class.path"));
        
//         // Add target/classes directory (where our compiled classes are)
//         File targetClasses = new File("target/classes");
//         if (targetClasses.exists()) {
//             classpathBuilder.append(File.pathSeparator).append(targetClasses.getAbsolutePath());
//         }
        
//         // Add Maven dependencies from target/dependency if they exist
//         File targetDependency = new File("target/dependency");
//         if (targetDependency.exists()) {
//             File[] jars = targetDependency.listFiles((dir, name) -> name.endsWith(".jar"));
//             if (jars != null) {
//                 for (File jar : jars) {
//                     classpathBuilder.append(File.pathSeparator).append(jar.getAbsolutePath());
//                 }
//             }
//         }
        
//         String classpath = classpathBuilder.toString();
        
//         // // Use ProcessBuilder to compile with comprehensive classpath
//         // ProcessBuilder compileProcessBuilder = new ProcessBuilder(
//         //     "javac", 
//         //     "-cp", classpath,
//         //     javaFile.getPath(), 
//         //     testFile.getPath()
//         // );

//         ProcessBuilder compileProcessBuilder = new ProcessBuilder(
//             "javac",
//             "-d", "target/classes",
//             "-cp", classpath,
//             javaFile.getPath(),
//             testFile.getPath()
// );
        
//         Process compileProcess = compileProcessBuilder.start();
        
//         // Capture compilation errors
//         String compilationErrors = readInputStream(compileProcess.getErrorStream());
//         String compilationOutput = readInputStream(compileProcess.getInputStream());
        
//         int exitCode = compileProcess.waitFor();
//         if (exitCode != 0) {
//             return Map.of("error", "Compilation failed:\n" + compilationErrors + "\nClasspath used: " + classpath);
//         }

//         // Find the JUnit Platform Console Standalone JAR
//         File junitStandaloneJar = null;
//         File dependencyDir = new File("target/dependency");
//         if (dependencyDir.exists()) {
//             File[] jars = dependencyDir.listFiles((dir, name) -> name.contains("junit-platform-console-standalone"));
//             if (jars != null && jars.length > 0) {
//                 junitStandaloneJar = jars[0];
//             }
//         }
        
        
//         if (junitStandaloneJar == null) {
//             // Provide debug information about what JARs are available
//             StringBuilder debugInfo = new StringBuilder();
//             debugInfo.append("JUnit Platform Console Standalone JAR not found in target/dependency.\n");
//             debugInfo.append("Available JARs in target/dependency:\n");
//             if (dependencyDir.exists()) {
//                 File[] allJars = dependencyDir.listFiles((dir, name) -> name.endsWith(".jar"));
//                 if (allJars != null) {
//                     for (File jar : allJars) {
//                         debugInfo.append("- ").append(jar.getName()).append("\n");
//                     }
//                 } else {
//                     debugInfo.append("No JAR files found\n");
//                 }
//             } else {
//                 debugInfo.append("target/dependency directory does not exist\n");
//             }
//             return Map.of("error", debugInfo.toString());
//         }
        
//         // Corrected classpath for test execution
//         String testClasspath = "target/classes" + File.pathSeparator + junitStandaloneJar.getAbsolutePath();
//         ProcessBuilder testProcessBuilder = new ProcessBuilder(
//             "java",
//             "-cp", testClasspath,
//             "org.junit.platform.console.ConsoleLauncher",
//             "--select-class=" + TEST_CLASS_NAME,
//             "--details=summary"
//         );
        
//         Process testProcess = testProcessBuilder.start();
        
//         String testOutput = readInputStream(testProcess.getInputStream());
//         String testErrors = readInputStream(testProcess.getErrorStream());
        
//         int testExitCode = testProcess.waitFor();
        
//         // Parse the test results from console output
//         int passed = 0, failed = 0, total = 0;
//         boolean success = testExitCode == 0;
        
//         // Simple regex-based parsing of JUnit console output
//         // String[] lines = testOutput.split("\n");
//         // for (String line : lines) {
//         //     line = line.trim();
//         //     if (line.startsWith("[") && line.endsWith("]")) {
//         //         // Use regex to find number followed by description
//         //         java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[\\s*(\\d+)\\s+(\\w+)\\s+\\w+\\s*\\]");
//         //         java.util.regex.Matcher matcher = pattern.matcher(line);
//         //         if (matcher.find()) {
//         //             int num = Integer.parseInt(matcher.group(1));
//         //             String description = matcher.group(2).toLowerCase();
                    
//         //             if (description.contains("found")) {
//         //                 total = num;
//         //             } else if (description.contains("successful")) {
//         //                 passed = num;
//         //             } else if (description.contains("failed")) {
//         //                 failed = num;
//         //             }
//         //         }
//         //     }
//         // }
        
//         String[] lines = testOutput.split("\n");
// for (String line : lines) {
//     String cleanLine = line.replaceAll("[\\[\\]]", "").trim(); // remove [ and ]
//     if (cleanLine.contains("tests found")) {
//         total = extractNumber(cleanLine);
//     } else if (cleanLine.contains("tests successful")) {
//         passed = extractNumber(cleanLine);
//     } else if (cleanLine.contains("tests failed")) {
//         failed = extractNumber(cleanLine);
//     }
// }



//         // Add debug information about parsing
//         if (total == 0) {
//             response.put("debug", Map.of(
//                 "testOutput", testOutput,
//                 "testErrors", testErrors,
//                 "exitCode", testExitCode,
//                 "testClasspath", testClasspath,
//                 "parsingDebug", Map.of(
//                     "total", total,
//                     "passed", passed,
//                     "failed", failed,
//                     "linesProcessed", lines.length
//                 )
//             ));
//         }
        
//         response.put("passed", passed);
//         response.put("failed", failed);
//         response.put("total", total);
//         response.put("success", success);
        
//         if (failed > 0) {
//             response.put("failures", java.util.Arrays.asList("Test execution completed with failures"));
//         }
        
//     } catch (Exception e) {
//         response.put("error", "Server error during execution: " + e.getMessage());
//     } finally {
//         // Clean up files safely
//         try {
//             if (javaFile.exists()) javaFile.delete();
//             if (testFile.exists()) testFile.delete();
            
//             File mainClassFile = new File(CLASS_NAME + ".class");
//             if (mainClassFile.exists()) mainClassFile.delete();
            
//             File testClassFile = new File(TEST_CLASS_NAME + ".class");
//             if (testClassFile.exists()) testClassFile.delete();
//         } catch (Exception cleanupException) {
//             // Log cleanup errors but don't fail the main operation
//             System.err.println("Warning: Failed to clean up some files: " + cleanupException.getMessage());
//         }
//     }

//     return response;
// }
//     private String generateTestClass(java.util.List<Map<String, Object>> testCases) {
//         StringBuilder sb = new StringBuilder();
//         sb.append("import org.junit.jupiter.api.Test;\n");
//         sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
//         sb.append("public class MainTest {\n");

//         for (int i = 0; i < testCases.size(); i++) {
//             Map<String, Object> testCase = testCases.get(i);
//             String input = (String) testCase.get("input");
//             String expected = (String) testCase.get("expected");
//             String methodName = "testCase" + (i + 1);

//             sb.append("    @Test\n");
//             sb.append("    void " + methodName + "() {\n");
//             sb.append("        Main main = new Main();\n");
//             sb.append("        int[] expectedArray = new int[]{" + expected + "};\n");
//             sb.append("        int[] actualArray = main.run(new int[]{" + input + "});\n");
//             sb.append("        assertArrayEquals(expectedArray, actualArray, \"Test Case " + (i + 1) + " failed\");\n");
//             sb.append("    }\n\n");
//         }

//         sb.append("}\n");
//         return sb.toString();
//     }
    
//     @PostMapping("/run-c")
//     public String runC(@RequestBody String code) {
//         String sourceFile = "main.c";
//         String execFile = "a.out";
//         String compileCmd = "gcc " + sourceFile + " -o " + execFile;
//         return runCompiledCode(sourceFile, code, compileCmd, "./" + execFile);
//     }

//     @PostMapping("/run-cpp")
//     public String runCpp(@RequestBody String code) {
//         String sourceFile = "main.cpp";
//         String execFile = "a.out";
//         String compileCmd = "g++ " + sourceFile + " -o " + execFile;
//         return runCompiledCode(sourceFile, code, compileCmd, "./" + execFile);
//     }

//     @PostMapping("/run-python")
//     public String runPython(@RequestBody String code) {
//         String fileName = "main.py";
//         return runInterpreter(fileName, code, "python3", fileName);
//     }

//     @PostMapping("/run-js")
//     public String runJavaScript(@RequestBody String code) {
//         String fileName = "main.js";
//         return runInterpreter(fileName, code, "node", fileName);
//     }
// }




























package com.example.javabackend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class JavaCodeController {

    private static final String CLASS_NAME = "Main";
    private static final String TEST_CLASS_NAME = "MainTest";

    /**
     * NEW: Simplified Java Execution (from src2)
     * Maps to: /api/v2/run-java
     */
    @PostMapping("/v2/run-java")
    public String runJavaCodeV2(@RequestBody String javaCode) {
        // Basic validation
        if (!javaCode.contains("public class Main")) {
            return "Error: Java code must contain a 'public class Main'.";
        }

        File javaFile = new File("Main.java");
        File classFile = new File("Main.class");
        String result;

        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(javaCode);
            writer.flush();

            // 1. Compile the Java file
            Process compileProcess = new ProcessBuilder("javac", "Main.java").start();
            if (!compileProcess.waitFor(10, TimeUnit.SECONDS)) {
                compileProcess.destroy();
                return "Error: Compilation timed out.";
            }

            if (compileProcess.exitValue() != 0) {
                String compileError = readInputStream(compileProcess.getErrorStream());
                return "Compilation error:\n" + compileError;
            }

            // 2. Run the compiled Java class
            Process runProcess = new ProcessBuilder("java", "Main").start();
            if (!runProcess.waitFor(10, TimeUnit.SECONDS)) {
                runProcess.destroy();
                return "Error: Execution timed out.";
            }

            String runOutput = readInputStream(runProcess.getInputStream());
            String runError = readInputStream(runProcess.getErrorStream());

            if (runProcess.exitValue() != 0) {
                return "Runtime error:\n" + runError;
            }

            result = runOutput;

        } catch (IOException | InterruptedException e) {
            result = "Server error: " + e.getMessage();
        } finally {
            // 3. Clean up the files
            if (javaFile.exists()) javaFile.delete();
            if (classFile.exists()) classFile.delete();
        }

        return result;
    }

    // --- EXISTING MULTI-LANGUAGE METHODS ---

    @PostMapping("/run-java")
    public Map<String, Object> runTestCases(@RequestBody Map<String, Object> requestBody) {
        String javaCode = (String) requestBody.get("code");
        java.util.List<Map<String, Object>> testCases = (java.util.List<Map<String, Object>>) requestBody.get("testCases");

        File javaFile = new File(CLASS_NAME + ".java");
        File testFile = new File(TEST_CLASS_NAME + ".java");
        Map<String, Object> response = new HashMap<>();

        try {
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(javaCode);
            }
            
            String testCode = generateTestClass(testCases);
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write(testCode);
            }

            StringBuilder classpathBuilder = new StringBuilder();
            classpathBuilder.append(System.getProperty("java.class.path"));
            
            File targetClasses = new File("target/classes");
            if (targetClasses.exists()) {
                classpathBuilder.append(File.pathSeparator).append(targetClasses.getAbsolutePath());
            }
            
            File targetDependency = new File("target/dependency");
            if (targetDependency.exists()) {
                File[] jars = targetDependency.listFiles((dir, name) -> name.endsWith(".jar"));
                if (jars != null) {
                    for (File jar : jars) {
                        classpathBuilder.append(File.pathSeparator).append(jar.getAbsolutePath());
                    }
                }
            }
            
            String classpath = classpathBuilder.toString();
            
            ProcessBuilder compileProcessBuilder = new ProcessBuilder(
                "javac",
                "-d", "target/classes",
                "-cp", classpath,
                javaFile.getPath(),
                testFile.getPath()
            );
            
            Process compileProcess = compileProcessBuilder.start();
            String compilationErrors = readInputStream(compileProcess.getErrorStream());
            
            int exitCode = compileProcess.waitFor();
            if (exitCode != 0) {
                return Map.of("error", "Compilation failed:\n" + compilationErrors);
            }

            File junitStandaloneJar = null;
            if (targetDependency.exists()) {
                File[] jars = targetDependency.listFiles((dir, name) -> name.contains("junit-platform-console-standalone"));
                if (jars != null && jars.length > 0) junitStandaloneJar = jars[0];
            }
            
            if (junitStandaloneJar == null) {
                return Map.of("error", "JUnit Standalone JAR not found in target/dependency.");
            }
            
            String testClasspath = "target/classes" + File.pathSeparator + junitStandaloneJar.getAbsolutePath();
            ProcessBuilder testProcessBuilder = new ProcessBuilder(
                "java", "-cp", testClasspath, "org.junit.platform.console.ConsoleLauncher",
                "--select-class=" + TEST_CLASS_NAME, "--details=summary"
            );
            
            Process testProcess = testProcessBuilder.start();
            String testOutput = readInputStream(testProcess.getInputStream());
            int testExitCode = testProcess.waitFor();
            
            int passed = 0, failed = 0, total = 0;
            String[] lines = testOutput.split("\n");
            for (String line : lines) {
                String cleanLine = line.replaceAll("[\\[\\]]", "").trim();
                if (cleanLine.contains("tests found")) total = extractNumber(cleanLine);
                else if (cleanLine.contains("tests successful")) passed = extractNumber(cleanLine);
                else if (cleanLine.contains("tests failed")) failed = extractNumber(cleanLine);
            }
            
            response.put("passed", passed);
            response.put("failed", failed);
            response.put("total", total);
            response.put("success", testExitCode == 0);
            
        } catch (Exception e) {
            response.put("error", "Server error: " + e.getMessage());
        } finally {
            if (javaFile.exists()) javaFile.delete();
            if (testFile.exists()) testFile.delete();
        }
        return response;
    }

    private String generateTestClass(java.util.List<Map<String, Object>> testCases) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.junit.jupiter.api.Test;\nimport static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("public class MainTest {\n");
        for (int i = 0; i < testCases.size(); i++) {
            Map<String, Object> testCase = testCases.get(i);
            sb.append("    @Test\n    void testCase" + (i + 1) + "() {\n");
            sb.append("        Main main = new Main();\n");
            sb.append("        int[] expectedArray = new int[]{" + testCase.get("expected") + "};\n");
            sb.append("        int[] actualArray = main.run(new int[]{" + testCase.get("input") + "});\n");
            sb.append("        assertArrayEquals(expectedArray, actualArray);\n    }\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    @PostMapping("/run-python")
    public String runPython(@RequestBody String code) { return runInterpreter("main.py", code, "python3", "main.py"); }

    @PostMapping("/run-js")
    public String runJavaScript(@RequestBody String code) { return runInterpreter("main.js", code, "node", "main.js"); }

    @PostMapping("/run-c")
    public String runC(@RequestBody String code) {
        return runCompiledCode("main.c", code, "gcc main.c -o a.out", "./a.out");
    }

    @PostMapping("/run-cpp")
    public String runCpp(@RequestBody String code) {
        return runCompiledCode("main.cpp", code, "g++ main.cpp -o a.out", "./a.out");
    }

    /**
     * Health check endpoint to keep the service awake.
     * Maps to: GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
    private String runInterpreter(String fileName, String code, String... command) {
        try {
            try (FileWriter writer = new FileWriter(fileName)) { writer.write(code); }
            Process process = new ProcessBuilder(command).start();
            if (!process.waitFor(15, TimeUnit.SECONDS)) return "Error: Timeout";
            String error = readInputStream(process.getErrorStream());
            return error.isEmpty() ? readInputStream(process.getInputStream()) : "Error: " + error;
        } catch (Exception e) { return "Error: " + e.getMessage(); }
        finally { new File(fileName).delete(); }
    }

    private String runCompiledCode(String sourceFile, String code, String compileCmd, String execCmd) {
        try {
            try (FileWriter writer = new FileWriter(sourceFile)) { writer.write(code); }
            Process compile = new ProcessBuilder(compileCmd.split(" ")).start();
            if (compile.waitFor() != 0) return "Compilation Error: " + readInputStream(compile.getErrorStream());
            Process exec = new ProcessBuilder(execCmd).start();
            return readInputStream(exec.getInputStream());
        } catch (Exception e) { return "Error: " + e.getMessage(); }
        finally { new File(sourceFile).delete(); new File(execCmd).delete(); }
    }

    private String readInputStream(InputStream is) throws IOException {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    }

    private int extractNumber(String line) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(line);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }
}
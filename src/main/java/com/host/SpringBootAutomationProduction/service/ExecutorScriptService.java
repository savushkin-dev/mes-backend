package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.util.StringJavaFileObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ExecutorScriptService {


    public Object executeScript(String javaCode, Map<String, String> parameters) {
        String className;
        if(findMainClassName(javaCode).isPresent()){
            className = findMainClassName(javaCode).get();
        } else {
            throw new RuntimeException("Unable to find main class name for " + javaCode);
        }

        String methodName = "main";

        try {
            Class<?> cls = compileAndLoadClass(javaCode, className);

            Method method = cls.getDeclaredMethod(methodName, Map.class, Map.class);

            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Method must be static");
            }

            return method.invoke(null, parameters, ReportGlobalVarsService.getGlobalVarsCache());

        } catch (InvocationTargetException e) {
            // Ошибка выполнения скрипта
            Throwable cause = e.getCause();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);
            log.error("Script execution error", e);
            throw new RuntimeException("Script execution error: " + cause.getMessage() + "\n" + sw.toString());
        } catch (Exception e) {
            log.error("Script execution failed", e);
            throw new RuntimeException("Script execution error: " + e.getMessage(), e);
        }
    }

    private Class<?> compileAndLoadClass(String javaCode, String className) throws Exception {
        Path tempDir = Files.createTempDirectory("java-scripts");
        Path sourcePath = tempDir.resolve(className + ".java");

        try {
            String classpath = System.getProperty("java.class.path");

            Files.write(sourcePath, javaCode.getBytes());

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new RuntimeException("Java compiler not available. Make sure you're running with JDK.");
            }

            // Собираем диагностику компиляции
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            List<String> options = List.of("-classpath", classpath);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    fileManager.getJavaFileObjects(sourcePath)
            );

            boolean success = task.call();
            fileManager.close();

            if (!success) {
                // Форматируем ошибки компиляции
                String errorMessage = formatCompilationErrors(diagnostics, javaCode);
                throw new RuntimeException(errorMessage);
            }

            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{tempDir.toUri().toURL()},
                    this.getClass().getClassLoader()
            );

            return classLoader.loadClass(className);

        } finally {
            cleanup(tempDir, sourcePath, className);
        }
    }

    private String formatCompilationErrors(DiagnosticCollector<JavaFileObject> diagnostics, String javaCode) {
        String[] lines = javaCode.split("\n");
        StringBuilder sb = new StringBuilder();

        List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();

        // Собираем только ошибки
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                errors.add(diagnostic);
            }
        }

        // Сортируем по строке
        errors.sort(Comparator.comparingLong(Diagnostic::getLineNumber));

        sb.append("Compilation failed:\n\n");

        for (int i = 0; i < errors.size(); i++) {
            Diagnostic<? extends JavaFileObject> error = errors.get(i);
            long lineNum = error.getLineNumber();
            long colNum = error.getColumnNumber();

            sb.append(String.format("Error #%d at line %d, column %d:\n", i + 1, lineNum, colNum));
            sb.append("  Message: ").append(error.getMessage(Locale.ENGLISH)).append("\n");

            // Показываем строку с ошибкой
            if (lineNum > 0 && lineNum <= lines.length) {
                int lineIndex = (int)lineNum - 1;

                // Номер строки и код
                sb.append(String.format("\n  %d | %s\n", lineNum, lines[lineIndex]));

                // Указатель на место ошибки
                sb.append("    ");
                for (int j = 0; j < colNum - 1; j++) {
                    sb.append(" ");
                }
                sb.append("^\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private void cleanup(Path tempDir, Path sourcePath, String className) {
        try {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(tempDir.resolve(className + ".class"));
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            log.warn("Failed to delete temporary files: {}", e.getMessage());
        }
    }

    public static Optional<String> findMainClassName(String javaCode) {
        Pattern pattern = Pattern.compile(
                "\\b(?:public\\s+)?class\\s+(\\w+).*?\\bpublic\\s+static\\s+void\\s+main\\s*\\(",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        pattern = Pattern.compile("\\bpublic\\s+class\\s+(\\w+)", Pattern.DOTALL);
        matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        pattern = Pattern.compile("\\bclass\\s+(\\w+)", Pattern.DOTALL);
        matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}
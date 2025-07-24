package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.util.StringJavaFileObject;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.internal.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
            className = findMainClassName(javaCode).get(); //Определяем имя главного класса
        } else {
            throw new RuntimeException("Unable to find main class name for " + javaCode);
        }

        String methodName = "main"; // Универсальное имя метода

        try {
            // 1. Компиляция и загрузка класса
            Class<?> cls = compileAndLoadClass(javaCode, className);

            // 2. Получаем метод с сигнатурой:
            //    `public static Object execute(Map<String, String> params)`
            Method method = cls.getDeclaredMethod(methodName, Map.class);

            // 3. Проверяем, что метод статический
            if (!Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Method must be static");
            }

            // 4. Вызываем метод, передавая parameters
            return method.invoke(null, parameters);

        } catch (Exception e) {
            log.error("Script execution failed", e);
            throw new RuntimeException("Script execution error: " + e.getMessage(), e);
        }
    }

    private Class<?> compileAndLoadClass(String javaCode, String className) throws Exception {
        Path tempDir = Files.createTempDirectory("java-scripts");
        Path sourcePath = tempDir.resolve(className + ".java");

        try {
            // Добавляем classpath основной программы
            String classpath = System.getProperty("java.class.path");

            // Сохраняем исходный код
            Files.write(sourcePath, javaCode.getBytes());

            // Компилируем с указанием classpath
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            List<String> options = List.of("-classpath", classpath);
            compiler.getTask(null, fileManager, null, options, null,
                    fileManager.getJavaFileObjects(sourcePath)).call();

            // Загружаем класс с тем же classloader, что и основной программы
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{tempDir.toUri().toURL()},
                    this.getClass().getClassLoader() // Используем родительский ClassLoader
            );

            return classLoader.loadClass(className);
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(tempDir.resolve(className + ".class"));
            Files.deleteIfExists(tempDir);
        }
    }

    public static Optional<String> findMainClassName(String javaCode) {
        // Паттерн для поиска класса с методом main
        Pattern pattern = Pattern.compile(
                "\\b(?:public\\s+)?class\\s+(\\w+).*?\\bpublic\\s+static\\s+void\\s+main\\s*\\(",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        // Если не нашли main, ищем любой public класс
        pattern = Pattern.compile(
                "\\bpublic\\s+class\\s+(\\w+)",
                Pattern.DOTALL
        );
        matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        // Если нет public класса, ищем любой класс
        pattern = Pattern.compile(
                "\\bclass\\s+(\\w+)",
                Pattern.DOTALL
        );
        matcher = pattern.matcher(javaCode);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }



}

package utils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Utils {
    private Utils() {
    }

    public static List<Class<?>> findClassesByAnnotation(String packageName, Class<?> annotationClass) {
        if (packageName == null || packageName.isBlank() || annotationClass == null) {
            return Collections.emptyList();
        }

        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("annotationClass must be an annotation type");
        }

        Set<Class<?>> matchedClasses = new LinkedHashSet<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    matchedClasses.addAll(findClassesInDirectory(resource, packageName, annotationClass, classLoader));
                } else if ("jar".equals(resource.getProtocol())) {
                    matchedClasses.addAll(findClassesInJar(resource, packagePath, annotationClass, classLoader));
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan package: " + packageName, exception);
        }

        return new ArrayList<>(matchedClasses);
    }

    private static Set<Class<?>> findClassesInDirectory(URL resource, String packageName, Class<?> annotationClass,
            ClassLoader classLoader) {
        Path directoryPath = Paths.get(URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8));
        if (!Files.exists(directoryPath)) {
            return Collections.emptySet();
        }

        Set<Class<?>> matchedClasses = new LinkedHashSet<>();

        try {
            Files.walk(directoryPath)
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .forEach(path -> {
                        String relativeClassName = directoryPath.relativize(path).toString()
                                .replace('/', '.')
                                .replace('\\', '.')
                                .replaceAll("\\.class$", "");
                        String className = packageName + '.' + relativeClassName;
                        addIfAnnotated(matchedClasses, className, annotationClass, classLoader);
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan directory: " + directoryPath, exception);
        }

        return matchedClasses;
    }

    private static Set<Class<?>> findClassesInJar(URL resource, String packagePath, Class<?> annotationClass,
            ClassLoader classLoader) {
        Set<Class<?>> matchedClasses = new LinkedHashSet<>();

        try {
            JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
            try (JarFile jarFile = jarConnection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (!entry.isDirectory() && entryName.startsWith(packagePath) && entryName.endsWith(".class")
                            && !entryName.contains("$")) {
                        String className = entryName.replace('/', '.').replaceAll("\\.class$", "");
                        addIfAnnotated(matchedClasses, className, annotationClass, classLoader);
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan jar for package: " + packagePath, exception);
        }

        return matchedClasses;
    }

    private static void addIfAnnotated(Set<Class<?>> matchedClasses, String className, Class<?> annotationClass,
            ClassLoader classLoader) {
        try {
            Class<?> candidateClass = Class.forName(className, false, classLoader);
            if (candidateClass.isAnnotationPresent(annotationClass.asSubclass(java.lang.annotation.Annotation.class))) {
                matchedClasses.add(candidateClass);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError exception) {
            // Ignore classes that cannot be loaded during scan.
        }
    }
}
package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import annotations.UrlMapping;
import mapping.Mapping;
import mapping.UrlMethode;

public class ScanAnnotation {

    public static List<Method> getMethodesWithAnnotation(Class<? extends Annotation> annotationClass,
            Class<?> tagetClass) {
        List<Method> methods = new ArrayList<>();
        Method[] lsMethods = tagetClass.getMethods();

        for (Method method : lsMethods) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
        return methods;

    }

    public static List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationClass, String packagee)
            throws ClassNotFoundException, URISyntaxException {

        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> class1 : UtilAnalyser.getClasses(packagee)) {
            if (class1.isAnnotationPresent(annotationClass)) {
                classes.add(class1);
            }
        }
        return classes;
    }

    public static List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotationClass)
            throws ClassNotFoundException, URISyntaxException {
        return getClassesWithAnnotation(annotationClass, "");
    }

    public static void generateMap(HashMap<UrlMethode, Mapping> mapping, Class<? extends Annotation> annotationController,
            Class<? extends Annotation> annotationMapping, String packagee) throws Exception {
        for (Class<?> class1 : UtilAnalyser.getClasses(packagee)) {
            if (class1.isAnnotationPresent(annotationController)) {
                for (Method method : class1.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(annotationMapping)) {

                        UrlMapping urlAnnotation = (UrlMapping) method.getAnnotation(annotationMapping);

                        UrlMethode urlMethode = new UrlMethode(urlAnnotation.url(), urlAnnotation.method());
                        if (mapping.containsKey(urlMethode)) {
                            throw new Exception("Il y a DUPLICATION d'url avec le même methode");
                        }
                        mapping.put(urlMethode, new Mapping(class1, method));
                    }
                }

            }
        }
    }

}

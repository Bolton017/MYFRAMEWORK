package util;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class UtilAnalyser {

    public static List<Class<?>> getClasses(String packageName) throws URISyntaxException, ClassNotFoundException {
        List<Class<?>> retour = new ArrayList<>();
        String path = packageName.replace('.', '/');
        File directory = null;
        ClassLoader cl = null;
        try {
            directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).toURI());
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Exception e) {
            return retour;
        }
        if (directory.exists()) {
            System.out.println(directory.getAbsolutePath());
            for (String file : directory.list()) {
                if (file.endsWith(".class")) {
                    String className = packageName + '.' + file.replace(".class", "");
                    if (className.startsWith(".")) {
                        className = className.substring(1);
                    }
                    System.out.println("file : " + file);
                    System.out.println("Chargement : " + className);
                    Class<?> clazz = cl.loadClass(className);
                    retour.add(clazz);
                    // System.out.println(clazz.getName());
                    continue;
                }
                if (!file.contains(".")) {
                    try {
                        retour.addAll(getClasses(packageName + "." + file));

                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } else {
            System.out.println("directory tsy miexiste");
            throw new ClassNotFoundException("directory tsy miexiste");
        }
        return retour;
    }
}
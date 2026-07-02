package controller;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import annotations.Controller;
import annotations.UrlMapping;
import mapping.Mapping;
import mapping.UrlMethode;
import util.ScanAnnotation;

public class FrontControllerListener implements ServletContextListener {

    private static final Class<? extends Annotation> CONTROLLER_ANNOTATION = Controller.class;
    private static final Class<? extends Annotation> URLMAPPING_ANNOTATION = UrlMapping.class;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String packageController = context.getInitParameter("controller-package");
        if (packageController == null || packageController.isEmpty()) {
            packageController = "";
        }

        HashMap<UrlMethode, Mapping> mapping = new HashMap<>();

        try {
            ScanAnnotation.generateMap(mapping, CONTROLLER_ANNOTATION, URLMAPPING_ANNOTATION, packageController);
            ;
            context.setAttribute("mapping", mapping);
        } catch (Exception e) {
            System.out.println("Erreur:");
            e.printStackTrace();
        }
    }

}
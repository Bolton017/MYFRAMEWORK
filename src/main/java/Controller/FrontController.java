package controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Controller;
import annotations.Get;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Mapping; // Import de ta nouvelle classe
import utils.Utils;

public class FrontController extends HttpServlet {
    private List<Class<?>> controllerClasses = Collections.emptyList();
    
    // La table de routage : "URL" -> "Objet Mapping (Classe + Méthode)"
    private Map<String, Mapping> routeMapping = new HashMap<>();

    public FrontController() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();

        String packageName = getInitParameter("controller-package");
        if (packageName == null || packageName.isBlank()) {
            packageName = "controller";
        }

        controllerClasses = Utils.findClassesByAnnotation(packageName, Controller.class);

        // Remplissage de la table de routage avec les objets Mapping
        for (Class<?> clazz : controllerClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get annotation = method.getAnnotation(Get.class);
                    String url = annotation.value();
                    
                    // On crée le mapping en stockant le nom complet de la classe et le nom de la méthode
                    Mapping mapping = new Mapping(clazz.getName(), method.getName());
                    
                    routeMapping.put(url, mapping);
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Récupérer l'URL demandée
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI().substring(contextPath.length());

        // 2. Chercher le Mapping associé à l'URL
        Mapping mapping = routeMapping.get(requestUri);

        if (mapping != null) {
            try {
                // Charger la classe dynamiquement à partir de son nom stocké dans le Mapping
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                
                // Instancier le contrôleur
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                
                // Retrouver la méthode exacte en spécifiant ses paramètres (HttpServletRequest, HttpServletResponse)
                Method methodToInvoke = controllerClass.getDeclaredMethod(
                    mapping.getMethod(), 
                    HttpServletRequest.class, 
                    HttpServletResponse.class
                );
                
                // Exécuter la méthode
                methodToInvoke.invoke(controllerInstance, request, response);
                
            } catch (NoSuchMethodException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "La méthode " + mapping.getMethod() + " avec les bons paramètres est introuvable.");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'exécution du contrôleur.");
            }
        } else {
            // 404 si l'URL n'est pas dans la Map
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucun mapping trouvé pour l'URL : " + requestUri);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }    

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }
}
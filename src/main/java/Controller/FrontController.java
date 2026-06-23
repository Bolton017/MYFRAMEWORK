package controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Controller;
import annotations.Url; // Utilise ta vraie annotation @Url
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Mapping; // Import de ton Mapping depuis le package utils
import utils.Utils;

public class FrontController extends HttpServlet {
    private List<Class<?>> controllerClasses = Collections.emptyList();
    
    // Table de routage : "URL" -> "Objet Mapping (Classe + Méthode)"
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

        // Utilise ta classe Utils pour trouver les @Controller
        controllerClasses = Utils.findClassesByAnnotation(packageName, Controller.class);

        // Remplissage automatique de la table de routage au démarrage
        for (Class<?> clazz : controllerClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                // Utilisation de ton annotation @Url
                if (method.isAnnotationPresent(Url.class)) {
                    Url annotation = method.getAnnotation(Url.class);
                    String url = annotation.value();
                    
                    // Sécurité : Éviter qu'un développeur mette deux fois la même URL
                    if (routeMapping.containsKey(url)) {
                        throw new ServletException("Erreur : L'URL '" + url + 
                            "' est déjà associée à la méthode " + routeMapping.get(url).getMethod() + "()");
                    }
                    
                    // Instanciation de ton model utils.Mapping
                    Mapping mapping = new Mapping(clazz.getName(), method.getName());
                    routeMapping.put(url, mapping);
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Extraction de l'URL propre (ex: /mon-app/biere -> /biere)
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI().substring(contextPath.length());

        // 2. Recherche du Mapping associé à cette URL
        Mapping mapping = routeMapping.get(requestUri);

        if (mapping != null) {
            try {
                // Chargement dynamique de la classe du contrôleur
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                
                // Recherche de la méthode par son nom
                Method methodToInvoke = null;
                for (Method m : controllerClass.getDeclaredMethods()) {
                    if (m.getName().equals(mapping.getMethod())) {
                        methodToInvoke = m;
                        break;
                    }
                }

                if (methodToInvoke != null) {
                    // Préparation dynamique des paramètres à envoyer à la méthode
                    Object[] parameters = new Object[methodToInvoke.getParameterCount()];
                    Class<?>[] paramTypes = methodToInvoke.getParameterTypes();
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i].equals(HttpServletRequest.class)) {
                            parameters[i] = request;
                        } else if (paramTypes[i].equals(HttpServletResponse.class)) {
                            parameters[i] = response;
                        } else {
                            parameters[i] = null; // Emplacement libre pour de futurs ajouts (ex: Session)
                        }
                    }

                    // Exécution de la méthode du contrôleur
                    methodToInvoke.invoke(controllerInstance, parameters);
                } else {
                    throw new NoSuchMethodException();
                }
                
            } catch (NoSuchMethodException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "La méthode " + mapping.getMethod() + " est introuvable.");
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'exécution du contrôleur.");
            }
        } else {
            // Si l'URL n'est pas configurée dans les contrôleurs -> Erreur 404
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
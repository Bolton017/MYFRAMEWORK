package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
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
                    String url = normalizeUrl(annotation.value());
                    if (url.isBlank()) {
                        continue;
                    }
                    
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

<<<<<<< Updated upstream
    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String normalized = url.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    private String toRouteLine(String url, Mapping mapping) {
        String simpleControllerName = mapping.getClassName();
        int lastDot = simpleControllerName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < simpleControllerName.length() - 1) {
            simpleControllerName = simpleControllerName.substring(lastDot + 1);
        }
        return url + "  " + simpleControllerName + "  " + mapping.getMethod() + "()";
    }

    private List<String> buildSupportedRouteLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Mapping> entry : routeMapping.entrySet()) {
            lines.add(toRouteLine(entry.getKey(), entry.getValue()));
        }
        return lines;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Extraction de l'URL propre (ex: /mon-app/dept/list -> /dept/list)
        String contextPath = request.getContextPath();
        String requestUri = normalizeUrl(request.getRequestURI().substring(contextPath.length()));

        // 2. Vérification par boucle si l'URL demandée est supportée
        String matchedUrl = null;
        Mapping mapping = null;
        for (Map.Entry<String, Mapping> entry : routeMapping.entrySet()) {
            if (entry.getKey().equals(requestUri)) {
                matchedUrl = entry.getKey();
                mapping = entry.getValue();
                break;
            }
        }

        // 3. Option debug: ?showMapping=true -> sortie demandée: URL + contrôleur + méthode
        String showMappingParam = request.getParameter("showMapping");
        boolean showMapping = "true".equalsIgnoreCase(showMappingParam);

        if (showMapping && mapping != null) {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("URL supportee:");
            out.println(toRouteLine(matchedUrl, mapping));
            return;
        }
=======
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI().substring(contextPath.length());

        // Sécurité : Si l'URI se termine par un "/" (sauf si c'est la racine exacte), on le nettoie
        if (requestUri.endsWith("/") && requestUri.length() > 1) {
            requestUri = requestUri.substring(0, requestUri.length() - 1);
        }

        Mapping mapping = routeMapping.get(requestUri);
>>>>>>> Stashed changes

        if (mapping != null) {
            try {
                Class<?> controllerClass = Class.forName(mapping.getClassName());
                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
                
                Method methodToInvoke = null;
                for (Method m : controllerClass.getDeclaredMethods()) {
                    if (m.getName().equals(mapping.getMethod())) {
                        methodToInvoke = m;
                        break;
                    }
                }

                if (methodToInvoke != null) {
                    Object[] parameters = new Object[methodToInvoke.getParameterCount()];
                    Class<?>[] paramTypes = methodToInvoke.getParameterTypes();
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (paramTypes[i].equals(HttpServletRequest.class)) {
                            parameters[i] = request;
                        } else if (paramTypes[i].equals(HttpServletResponse.class)) {
                            parameters[i] = response;
                        } else {
                            parameters[i] = null;
                        }
                    }
<<<<<<< Updated upstream

                    // Exécution de la méthode du contrôleur
                    Object actionResult = methodToInvoke.invoke(controllerInstance, parameters);

                    // Si la méthode retourne une valeur (ex: String), on l'affiche en réponse.
                    if (actionResult != null && !response.isCommitted()) {
                        if (response.getContentType() == null) {
                            response.setContentType("text/plain;charset=UTF-8");
                        }
                        response.getWriter().print(String.valueOf(actionResult));
                    }
=======
                    methodToInvoke.invoke(controllerInstance, parameters);
>>>>>>> Stashed changes
                } else {
                    throw new NoSuchMethodException();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'exécution du contrôleur : " + e.getMessage());
            }
        } else {
<<<<<<< Updated upstream
            // Si l'URL n'est pas configurée dans les contrôleurs -> 404 + liste des URLs supportées
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("URL non supportee: " + requestUri);
            out.println("URLs supportees:");
            for (String line : buildSupportedRouteLines()) {
                out.println(line);
            }
=======
            // Message personnalisé pour valider que le Framework intercepte bien la requête
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("<h2>[Mon Framework] Erreur 404 : Aucune méthode trouvée pour l'URL '" + requestUri + "'</h2>");
            response.getWriter().println("<p>Routes disponibles : " + routeMapping.keySet() + "</p>");
>>>>>>> Stashed changes
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
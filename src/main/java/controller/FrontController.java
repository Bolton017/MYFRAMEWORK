package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import http.HttpMethode;
import mapping.Mapping;
import mapping.UrlMethode;

public class FrontController extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        out.println("FrameWork OK");

        ServletContext context = req.getServletContext();
        HashMap<UrlMethode, Mapping>  mapping = (HashMap<UrlMethode, Mapping>) context.getAttribute("mapping");
        out.println(mapping);
        if (mapping == null) {
            out.println("Mapping introuvable");
            return;
        }
        String askUrl = req.getRequestURI();
        String contextPath = req.getContextPath();

        askUrl = askUrl.substring(contextPath.length());
        out.println(askUrl);
        HttpMethode methode = HttpMethode.valueOf(req.getMethod());
        UrlMethode urlMethode = new UrlMethode(askUrl, methode);

        out.println("Recherche :");
        out.println(urlMethode.getUrl());
        out.println(urlMethode.getMethode());
        out.println(urlMethode.hashCode());


        Mapping map = mapping.get(urlMethode);
        if (map != null) {
            Class<?> class1 = map.getControllerClass();
            Method method = map.getMethod();
            out.println("Url existe :");
            out.println(askUrl + " (" + method + ") --> " + map.getClass().getSimpleName() + " | " + method.getName());
            out.println("Execution de la methode demandé.... ");

            try {
                Object obj = class1.getDeclaredConstructor().newInstance();
                Object result = method.invoke(obj);

                if (result != null) {
                    out.println(result.toString());
                } else {
                    out.println("La méthode a bien été executé");
                }
                
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new ServletException("Erreur :" + e);
            }

        }

        else {
            out.println("Url Introuvable, voici ceux qui existe :");
            for (UrlMethode url : mapping.keySet()) {
                Mapping nMap = mapping.get(url);

                out.println(
                        url.getUrl() + " --> " + nMap.getClass().getSimpleName() + " | " + nMap.getMethod().getName());

            }
        }

    }
}
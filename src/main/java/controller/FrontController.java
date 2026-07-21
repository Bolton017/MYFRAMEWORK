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
        out.println("Framework ");

        ServletContext context = req.getServletContext();

        String prefix = context.getInitParameter("view-prefix");
        String sufix = context.getInitParameter("view-suffix");


        @SuppressWarnings("unchecked")
        HashMap<UrlMethode, Mapping> mapping = (HashMap<UrlMethode, Mapping>) context.getAttribute("mapping");
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
            if (method.getReturnType() != ModelAndView.class) {
                throw new ServletException("La methode " + method + " n'as pas de type de retour valide");
            }

            try {
                Object obj = class1.getDeclaredConstructor().newInstance();
                ModelAndView result = (ModelAndView) method.invoke(obj);

                if (result != null) {
                    out.println(result.toString());
                    addArgToRequest(req, result.getData());
                    String path =   "/" + prefix + "/" + result.getView() + "." + sufix; 
                    RequestDispatcher dispat = req.getRequestDispatcher(path);
                    dispat.forward(req, resp);
                } else {
                    throw new ServletException("Le model envoyé est null");

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

    private void addArgToRequest(HttpServletRequest req, Map<String, Object> data) {
        for (String argument : data.keySet()) {
            Object value = data.get(argument);
            req.setAttribute(argument, value);
        }
    }

}

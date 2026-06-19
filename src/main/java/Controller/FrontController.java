package controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import annotations.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Utils;



public class FrontController extends HttpServlet {
    private List<Class<?>> controllerClasses = Collections.emptyList();

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
    }

    protected void processRequest(HttpServletRequest request , HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("FRAMEWORK_OK");
    }

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException {
        processRequest(request, response);
    }    

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }
}

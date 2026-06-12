package controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



public class FrontController extends HttpServlet {
    public FrontController() {
        super();
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
}

package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pahanaedu.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Servlet for handling user logout
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/api/auth/logout"})
public class LogoutServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            boolean logoutSuccessful = authService.logout(request);

            JsonObject jsonResponse = new JsonObject();

            if (logoutSuccessful) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Logout successful");
                LOGGER.info("User successfully logged out");
            } else {
                jsonResponse.addProperty("status", "warning");
                jsonResponse.addProperty("message", "No active session found");
                LOGGER.warning("Logout attempted with no active session");
            }

            out.print(gson.toJson(jsonResponse));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An error occurred during logout");
            out.print(gson.toJson(jsonResponse));
            LOGGER.severe("Logout error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
}

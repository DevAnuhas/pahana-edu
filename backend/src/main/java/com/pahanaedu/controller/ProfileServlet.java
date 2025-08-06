package com.pahanaedu.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pahanaedu.model.User;
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
 * Servlet for retrieving the current user's profile information
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/api/auth/profile"})
public class ProfileServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ProfileServlet.class.getName());
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            User currentUser = authService.getProfile(request);
            JsonObject jsonResponse = new JsonObject();

            if (currentUser != null) {
                jsonResponse.addProperty("status", "success");

                JsonObject userJson = new JsonObject();
                userJson.addProperty("id", currentUser.getId());
                userJson.addProperty("username", currentUser.getUsername());
                userJson.addProperty("fullName", currentUser.getFullName());
                userJson.addProperty("role", currentUser.getRole());
                userJson.addProperty("email", currentUser.getEmail());
                userJson.addProperty("active", currentUser.isActive());

                jsonResponse.add("user", userJson);
                LOGGER.info("Profile information retrieved for user: " + currentUser.getUsername());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Not authenticated");
                LOGGER.warning("Unauthorized attempt to access profile information");
            }

            out.print(gson.toJson(jsonResponse));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "An error occurred while retrieving profile information");
            out.print(gson.toJson(jsonResponse));
            LOGGER.severe("Profile retrieval error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
}

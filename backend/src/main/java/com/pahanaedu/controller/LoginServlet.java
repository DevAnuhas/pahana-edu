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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Servlet for handling user login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/auth/login"})
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private final Gson gson = new Gson();
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }

            JsonObject credentials = gson.fromJson(requestBody.toString(), JsonObject.class);

            if (credentials == null || !credentials.has("username") || !credentials.has("password")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
                return;
            }

            String username = credentials.get("username").getAsString();
            String password = credentials.get("password").getAsString();

            User authenticatedUser = authService.login(username, password);

            if (authenticatedUser != null) {
                authService.storeUserInSession(request, authenticatedUser);

                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Login successful");

                JsonObject userJson = new JsonObject();
                userJson.addProperty("id", authenticatedUser.getId());
                userJson.addProperty("username", authenticatedUser.getUsername());
                userJson.addProperty("fullName", authenticatedUser.getFullName());
                userJson.addProperty("role", authenticatedUser.getRole());
                userJson.addProperty("email", authenticatedUser.getEmail());

                jsonResponse.add("user", userJson);

                out.print(gson.toJson(jsonResponse));
                LOGGER.info("User successfully authenticated: " + username);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
                LOGGER.warning("Failed login attempt for username: " + username);
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred during login");
            LOGGER.severe("Login error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "error");
        jsonResponse.addProperty("message", message);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(jsonResponse));
    }
}

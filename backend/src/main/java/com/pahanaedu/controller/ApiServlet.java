package com.pahanaedu.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base API servlet that confirms the API is running
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api", "/api/"})
public class ApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Create JSON response using Gson
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("message", "API is running");
        jsonResponse.addProperty("version", "1.0");

        PrintWriter out = response.getWriter();
        out.print(new Gson().toJson(jsonResponse));
        out.flush();
    }
}

package com.pahanaedu.service;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.logging.Logger;

/**
 * Service class for handling authentication-related business logic
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final String USER_SESSION_KEY = "authenticated_user";

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) {
        LOGGER.info("Attempting authentication for user: " + username);
        return userDAO.authenticate(username, password);
    }

    public void storeUserInSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_KEY, user);
        LOGGER.info("User stored in session: " + user.getUsername());
    }

    public User getUserFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(USER_SESSION_KEY);
        }
        return null;
    }

    public boolean logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(USER_SESSION_KEY);
            if (user != null) {
                LOGGER.info("Logging out user: " + user.getUsername());
                session.removeAttribute(USER_SESSION_KEY);
                session.invalidate();
                return true;
            }
        }
        return false;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return getUserFromSession(request) != null;
    }

    public User getProfile(HttpServletRequest request) {
        return getUserFromSession(request);
    }
}

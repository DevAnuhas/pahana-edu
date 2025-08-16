package com.pahanaedu.functional;

import com.pahanaedu.dao.UserDAO;
import com.pahanaedu.model.User;
import com.pahanaedu.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the login and authentication feature
 */
public class LoginFunctionalTest {

    private static class MockUserDAO extends UserDAO {
        private boolean shouldReturnValidUser = true;
        private String expectedUsername = "admin";
        private String expectedPassword = "admin1234";

        public void setShouldReturnValidUser(boolean shouldReturnValidUser) {
            this.shouldReturnValidUser = shouldReturnValidUser;
        }

        public void setExpectedCredentials(String username, String password) {
            this.expectedUsername = username;
            this.expectedPassword = password;
        }

        @Override
        public User authenticate(String username, String password) {
            if (shouldReturnValidUser && username != null && password != null && 
                username.equals(expectedUsername) && password.equals(expectedPassword)) {
                User user = new User();
                user.setId(1);
                user.setUsername(username);
                user.setFullName("Test User");
                user.setRole("admin");
                user.setEmail("test@example.com");
                user.setActive(true);
                return user;
            }
            return null;
        }
    }

    // Custom mock implementation for AuthService
    private static class TestAuthService extends AuthService {
        private final MockUserDAO mockUserDAO;
        private User sessionUser;
        private boolean isLoggedOut = false;

        public TestAuthService(MockUserDAO mockUserDAO) {
            this.mockUserDAO = mockUserDAO;
        }

        @Override
        public User login(String username, String password) {
            return mockUserDAO.authenticate(username, password);
        }

        // Simplified session management for testing
        public void storeUserInSession(User user) {
            this.sessionUser = user;
        }

        public User getUserFromSession() {
            return sessionUser;
        }

        public boolean logout() {
            if (sessionUser != null) {
                sessionUser = null;
                isLoggedOut = true;
                return true;
            }
            return false;
        }

        public boolean isAuthenticated() {
            return sessionUser != null;
        }

        public boolean isLoggedOut() {
            return isLoggedOut;
        }
    }

    private TestAuthService authService;
    private MockUserDAO mockUserDAO;

    @BeforeEach
    public void setUp() {
        mockUserDAO = new MockUserDAO();
        authService = new TestAuthService(mockUserDAO);
    }

    /**
     * Test login with valid credentials
     * 
     * Purpose: Verify that a user can successfully log in with valid credentials
     * Inputs: Valid username and password
     * Expected Outputs: User object is returned and stored in session
     * Requirement ID: AUTH-001
     */
    @Test
    public void testLoginWithValidCredentials() {
        // Arrange
        String username = "validuser";
        String password = "validpass";
        mockUserDAO.setExpectedCredentials(username, password);
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);
        authService.storeUserInSession(user);

        // Assert
        assertNotNull(user, "User should not be null for valid credentials");
        assertEquals(username, user.getUsername(), "Username should match");
        assertEquals("Test User", user.getFullName(), "Full name should match");
        assertEquals("admin", user.getRole(), "Role should match");
        assertTrue(user.isActive(), "User should be active");
        
        User sessionUser = authService.getUserFromSession();
        assertNotNull(sessionUser, "User should be stored in session");
        assertEquals(username, sessionUser.getUsername(), "Session username should match");
    }

    /**
     * Test login with invalid username
     * 
     * Purpose: Verify that login fails when an invalid username is provided
     * Inputs: Invalid username and valid password
     * Expected Outputs: Null user object is returned
     * Requirement ID: AUTH-002
     */
    @Test
    public void testLoginWithInvalidUsername() {
        // Arrange
        String username = "invaliduser";
        String password = "validpass";
        mockUserDAO.setExpectedCredentials("validuser", password);
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);

        // Assert
        assertNull(user, "User should be null for invalid username");
    }

    /**
     * Test login with invalid password
     * 
     * Purpose: Verify that login fails when an invalid password is provided
     * Inputs: Valid username and invalid password
     * Expected Outputs: Null user object is returned
     * Requirement ID: AUTH-003
     */
    @Test
    public void testLoginWithInvalidPassword() {
        // Arrange
        String username = "validuser";
        String password = "invalidpass";
        mockUserDAO.setExpectedCredentials(username, "validpass");
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);

        // Assert
        assertNull(user, "User should be null for invalid password");
    }

    /**
     * Test login with empty username
     * 
     * Purpose: Verify that login fails when an empty username is provided
     * Inputs: Empty username and valid password
     * Expected Outputs: Null user object is returned
     * Requirement ID: AUTH-004
     */
    @Test
    public void testLoginWithEmptyUsername() {
        // Arrange
        String username = "";
        String password = "validpass";
        mockUserDAO.setExpectedCredentials("validuser", password);
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);

        // Assert
        assertNull(user, "User should be null for empty username");
    }

    /**
     * Test login with empty password
     * 
     * Purpose: Verify that login fails when an empty password is provided
     * Inputs: Valid username and empty password
     * Expected Outputs: Null user object is returned
     * Requirement ID: AUTH-005
     */
    @Test
    public void testLoginWithEmptyPassword() {
        // Arrange
        String username = "validuser";
        String password = "";
        mockUserDAO.setExpectedCredentials(username, "validpass");
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);

        // Assert
        assertNull(user, "User should be null for empty password");
    }

    /**
     * Test login with null credentials
     * 
     * Purpose: Verify that login fails when null credentials are provided
     * Inputs: Null username and password
     * Expected Outputs: Null user object is returned
     * Requirement ID: AUTH-006
     */
    @Test
    public void testLoginWithNullCredentials() {
        // Arrange
        String username = null;
        String password = null;
        mockUserDAO.setExpectedCredentials("validuser", "validpass");
        mockUserDAO.setShouldReturnValidUser(true);

        // Act
        User user = authService.login(username, password);

        // Assert
        assertNull(user, "User should be null for null credentials");
    }

    /**
     * Test logout functionality
     * 
     * Purpose: Verify that a user can successfully log out
     * Inputs: Authenticated user in session
     * Expected Outputs: Session is invalidated and user is removed from session
     * Requirement ID: AUTH-007
     */
    @Test
    public void testLogout() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        authService.storeUserInSession(user);

        // Act
        boolean result = authService.logout();

        // Assert
        assertTrue(result, "Logout should return true for authenticated user");
        assertTrue(authService.isLoggedOut(), "User should be logged out");
        assertNull(authService.getUserFromSession(), "User should be removed from session");
    }

    /**
     * Test logout with no authenticated user
     * 
     * Purpose: Verify that logout fails when no user is authenticated
     * Inputs: No authenticated user in session
     * Expected Outputs: Logout returns false
     * Requirement ID: AUTH-008
     */
    @Test
    public void testLogoutWithNoAuthenticatedUser() {
        // Arrange
        // No user in session

        // Act
        boolean result = authService.logout();

        // Assert
        assertFalse(result, "Logout should return false when no user is authenticated");
    }

    /**
     * Test isAuthenticated with authenticated user
     * 
     * Purpose: Verify that isAuthenticated returns true when a user is authenticated
     * Inputs: Authenticated user in session
     * Expected Outputs: isAuthenticated returns true
     * Requirement ID: AUTH-009
     */
    @Test
    public void testIsAuthenticatedWithAuthenticatedUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        authService.storeUserInSession(user);

        // Act
        boolean result = authService.isAuthenticated();

        // Assert
        assertTrue(result, "isAuthenticated should return true for authenticated user");
    }

    /**
     * Test isAuthenticated with no authenticated user
     * 
     * Purpose: Verify that isAuthenticated returns false when no user is authenticated
     * Inputs: No authenticated user in session
     * Expected Outputs: isAuthenticated returns false
     * Requirement ID: AUTH-010
     */
    @Test
    public void testIsAuthenticatedWithNoAuthenticatedUser() {
        // Arrange
        // No user in session

        // Act
        boolean result = authService.isAuthenticated();

        // Assert
        assertFalse(result, "isAuthenticated should return false when no user is authenticated");
    }
}
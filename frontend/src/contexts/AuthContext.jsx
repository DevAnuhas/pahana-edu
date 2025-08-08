import React, { createContext, useContext, useState, useEffect } from "react";
import { authAPI } from "../services/api";

const AuthContext = createContext();

export const useAuth = () => {
	const context = useContext(AuthContext);
	if (!context) {
		throw new Error("useAuth must be used within an AuthProvider");
	}
	return context;
};

export const AuthProvider = ({ children }) => {
	const [user, setUser] = useState(null);
	const [loading, setLoading] = useState(true);

	// Check if user is already logged in on app start
	useEffect(() => {
		checkAuthStatus();
	}, []);

	const login = async (credentials) => {
		try {
			const response = await authAPI.login(credentials);
			if (response.status === "success") {
				setUser(response.user);
				return { success: true, user: response.user };
			} else {
				return { success: false, error: response.message };
			}
		} catch (error) {
			return {
				success: false,
				error:
					error.response?.data?.message || "Login failed. Please try again.",
			};
		}
	};

	const checkAuthStatus = async () => {
		try {
			const response = await authAPI.getProfile();
			if (response.status === "success") {
				setUser(response.user);
			}
		} catch (error) {
			console.log("No active session");
			setUser(null);
		} finally {
			setLoading(false);
		}
	};

	const logout = async () => {
		try {
			await authAPI.logout();
		} catch (error) {
			console.error("Logout error:", error);
		} finally {
			setUser(null);
		}
	};

	const value = {
		user,
		login,
		logout,
		loading,
		isAuthenticated: !!user,
	};

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

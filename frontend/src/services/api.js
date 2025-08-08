import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

// Create axios instance with default config
const apiClient = axios.create({
	baseURL: API_BASE_URL,
	headers: {
		"Content-Type": "application/json",
	},
	withCredentials: true, // Important for session-based auth
});

// Auth API functions
export const authAPI = {
	login: async (credentials) => {
		const response = await apiClient.post("/auth/login", credentials);
		return response.data;
	},

	logout: async () => {
		const response = await apiClient.post("/auth/logout");
		return response.data;
	},

	getProfile: async () => {
		const response = await apiClient.get("/auth/profile");
		return response.data;
	},
};

export default apiClient;

import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

// Create axios instance with default config
const apiClient = axios.create({
	baseURL: API_BASE_URL,
	headers: {
		"Content-Type": "application/json",
	},
	withCredentials: true, // Important for session-based auth
	timeout: 10000, // 10 seconds timeout
});

// Add request interceptor for debugging
apiClient.interceptors.request.use(
	(config) => {
		return config;
	},
	(error) => {
		console.error("API Request Error:", error);
		return Promise.reject(error);
	}
);

// Add response interceptor for handling common errors
apiClient.interceptors.response.use(
	(response) => {
		return response;
	},
	(error) => {
		console.error("API Response Error:", error);

		if (error.code === "ECONNABORTED") {
			console.error("Request timeout");
		}

		if (!error.response) {
			console.error("Network error or server not reachable");
		}

		return Promise.reject(error);
	}
);

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

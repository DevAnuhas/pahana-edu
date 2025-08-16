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
		} else {
			console.error("Status:", error.response.status);
			console.error("Status Text:", error.response.statusText);
			console.error("Response Data:", error.response.data);

			// Format backend validation errors
			if (error.response.status === 400 && error.response.data) {
				if (typeof error.response.data === "object") {
					const errorMessages = [];
					for (const [key, value] of Object.entries(error.response.data)) {
						errorMessages.push(`${key}: ${value}`);
					}
					error.message = errorMessages.join(", ");
				} else if (typeof error.response.data === "string") {
					error.message = error.response.data;
				}
			}
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

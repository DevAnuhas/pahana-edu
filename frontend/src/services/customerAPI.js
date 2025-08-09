import apiClient from "./api";
import { withRetry } from "./utils";

const customerAPI = {
	getAllCustomers: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/customers");
			return response.data;
		});
	},

	getCustomerById: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/customers/${id}`);
			return response.data;
		});
	},

	searchCustomers: async (searchTerm) => {
		return withRetry(async () => {
			const response = await apiClient.get(
				`/customers/search?q=${encodeURIComponent(searchTerm)}`
			);
			return response.data;
		});
	},

	createCustomer: async (customerData) => {
		return withRetry(async () => {
			const response = await apiClient.post("/customers", customerData);
			return response.data;
		});
	},

	updateCustomer: async (id, customerData) => {
		return withRetry(async () => {
			const response = await apiClient.put(`/customers/${id}`, customerData);
			return response.data;
		});
	},

	deleteCustomer: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.delete(`/customers/${id}`);
			return response.data;
		});
	},
};

export default customerAPI;

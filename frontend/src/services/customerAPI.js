import apiClient from "./api";

const customerAPI = {
	getAllCustomers: async () => {
		const response = await apiClient.get("/customers");
		return response.data;
	},

	getCustomerById: async (id) => {
		const response = await apiClient.get(`/customers/${id}`);
		return response.data;
	},

	searchCustomers: async (searchTerm) => {
		const response = await apiClient.get(
			`/customers/search?q=${encodeURIComponent(searchTerm)}`
		);
		return response.data;
	},

	createCustomer: async (customerData) => {
		const response = await apiClient.post("/customers", customerData);
		return response.data;
	},

	updateCustomer: async (id, customerData) => {
		const response = await apiClient.put(`/customers/${id}`, customerData);
		return response.data;
	},

	deleteCustomer: async (id) => {
		const response = await apiClient.delete(`/customers/${id}`);
		return response.data;
	},
};

export default customerAPI;

import apiClient from "./api";
import { withRetry } from "./utils";

const billingAPI = {
	getAllInvoices: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/invoices");
			return response.data;
		});
	},

	getInvoiceById: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/invoices/${id}`);
			return response.data;
		});
	},

	getInvoiceByNumber: async (invoiceNumber) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/invoices/number/${invoiceNumber}`);
			return response.data;
		});
	},

	getInvoicesByCustomer: async (customerId) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/invoices?customer=${customerId}`);
			return response.data;
		});
	},

	calculateBill: async (invoiceData) => {
		return withRetry(async () => {
			const response = await apiClient.put("/invoices/calculate", invoiceData);
			return response.data;
		});
	},

	createInvoice: async (invoiceData) => {
		try {
			const response = await fetch(`${apiClient.defaults.baseURL}/invoices`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				credentials: "include",
				body: JSON.stringify(invoiceData),
			});

			const data = await response.json();

			if (!response.ok) {
				console.error("Error creating invoice:", data);
				return {
					status: "error",
					message: data.message || `Server returned status ${response.status}`,
				};
			}

			return data;
		} catch (error) {
			console.error("Error in createInvoice:", error);
			return {
				status: "error",
				message: error.message || "Failed to create invoice",
			};
		}
	},

	deleteInvoice: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.delete(`/invoices/${id}`);
			return response.data;
		});
	},

	printInvoice: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/invoices/print/${id}`);
			return response.data;
		});
	},

	generateInvoiceNumber: () => {
		const date = new Date();
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, "0");
		const random = Math.floor(Math.random() * 10000)
			.toString()
			.padStart(4, "0");
		return `INV-${year}${month}-${random}`;
	},

	generatePrintableBill: async (invoiceData) => {
		try {
			const response = await apiClient.post("/invoices/preview", invoiceData);
			return response.data;
		} catch (error) {
			console.error("Error generating printable bill:", error);
			throw error;
		}
	},
};

export default billingAPI;

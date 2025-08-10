import apiClient from "./api";
import { withRetry } from "./utils";

// Book API functions
const bookAPI = {
	getAllBooks: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/books");
			return response.data;
		});
	},

	getBookById: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/${id}`);
			return response.data;
		});
	},

	getBookByIsbn: async (isbn) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/isbn/${isbn}`);
			return response.data;
		});
	},

	getBooksByCategory: async (categoryId) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/category/${categoryId}`);
			return response.data;
		});
	},

	searchBooks: async (searchTerm) => {
		return withRetry(async () => {
			const response = await apiClient.get(
				`/books/search?q=${encodeURIComponent(searchTerm)}`
			);
			return response.data;
		});
	},

	createBook: async (bookData) => {
		return withRetry(async () => {
			const response = await apiClient.post("/books", bookData);
			return response.data;
		});
	},

	updateBook: async (id, bookData) => {
		return withRetry(async () => {
			const response = await apiClient.put(`/books/${id}`, bookData);
			return response.data;
		});
	},

	updateBookStock: async (id, quantityChange) => {
		return withRetry(async () => {
			const response = await apiClient.patch(`/books/${id}/stock`, {
				quantityChange,
			});
			return response.data;
		});
	},

	deleteBook: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.delete(`/books/${id}`);
			return response.data;
		});
	},

	getAllCategories: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/categories");
			return response.data;
		});
	},

	getAllPublishers: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/publishers");
			return response.data;
		});
	},
};

export default bookAPI;

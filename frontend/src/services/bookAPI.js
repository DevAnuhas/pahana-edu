import apiClient from "./api";

// Helper function for retrying API calls
const withRetry = async (apiCall, maxRetries = 3, delay = 1000) => {
	let lastError;
	for (let attempt = 0; attempt < maxRetries; attempt++) {
		try {
			return await apiCall();
		} catch (error) {
			lastError = error;
			if (attempt < maxRetries - 1) {
				// Wait before next retry with exponential backoff
				await new Promise((resolve) =>
					setTimeout(resolve, delay * Math.pow(2, attempt))
				);
			}
		}
	}
	throw lastError; // If all retries fail, throw the last error
};

// Book API functions
const bookAPI = {
	// Get all books
	getAllBooks: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/books");
			return response.data;
		});
	},

	// Get book by ID
	getBookById: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/${id}`);
			return response.data;
		});
	},

	// Get book by ISBN
	getBookByIsbn: async (isbn) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/isbn/${isbn}`);
			return response.data;
		});
	},

	// Get books by category
	getBooksByCategory: async (categoryId) => {
		return withRetry(async () => {
			const response = await apiClient.get(`/books/category/${categoryId}`);
			return response.data;
		});
	},

	// Search books
	searchBooks: async (searchTerm) => {
		return withRetry(async () => {
			const response = await apiClient.get(
				`/books/search?q=${encodeURIComponent(searchTerm)}`
			);
			return response.data;
		});
	},

	// Create a new book
	createBook: async (bookData) => {
		return withRetry(async () => {
			const response = await apiClient.post("/books", bookData);
			return response.data;
		});
	},

	// Update an existing book
	updateBook: async (id, bookData) => {
		return withRetry(async () => {
			const response = await apiClient.put(`/books/${id}`, bookData);
			return response.data;
		});
	},

	// Update book stock
	updateBookStock: async (id, quantityChange) => {
		return withRetry(async () => {
			const response = await apiClient.patch(`/books/${id}/stock`, {
				quantityChange,
			});
			return response.data;
		});
	},

	// Delete a book
	deleteBook: async (id) => {
		return withRetry(async () => {
			const response = await apiClient.delete(`/books/${id}`);
			return response.data;
		});
	},

	// Get all categories
	getAllCategories: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/categories");
			return response.data;
		});
	},

	// Get all publishers
	getAllPublishers: async () => {
		return withRetry(async () => {
			const response = await apiClient.get("/publishers");
			return response.data;
		});
	},
};

export default bookAPI;

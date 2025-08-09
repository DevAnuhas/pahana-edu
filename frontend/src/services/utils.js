/**
 * Retry a function call multiple times with exponential backoff
 * @param {Function} apiCall - The function to retry
 * @param {number} maxRetries - Maximum number of retry attempts
 * @param {number} delay - Base delay in milliseconds between retries
 * @returns {Promise<any>} - Result of the function call
 */
export const withRetry = async (apiCall, maxRetries = 3, delay = 1000) => {
	let lastError;
	for (let attempt = 0; attempt < maxRetries; attempt++) {
		try {
			return await apiCall();
		} catch (error) {
			lastError = error;
			console.error(
				`API call failed (attempt ${attempt + 1}/${maxRetries}):`,
				error.message
			);

			// Don't retry if it's a 4xx error (client error)
			if (
				error.response &&
				error.response.status >= 400 &&
				error.response.status < 500
			) {
				console.error("Client error, not retrying:", error.response.data);
				throw error;
			}

			if (attempt < maxRetries - 1) {
				// Wait before next retry with exponential backoff
				const backoffTime = delay * Math.pow(2, attempt);
				console.log(`Retrying in ${backoffTime}ms...`);
				await new Promise((resolve) => setTimeout(resolve, backoffTime));
			}
		}
	}
	throw lastError; // If all retries fail, throw the last error
};

/**
 * Format a date to a locale date string
 * @param {string|Date} date - Date to format
 * @returns {string} - Formatted date string
 */
export const formatDate = (date) => {
	if (!date) return "";
	return new Date(date).toLocaleDateString();
};

/**
 * Format a number as currency
 * @param {number} amount - Amount to format
 * @param {string} currency - Currency code
 * @returns {string} - Formatted currency string
 */
export const formatCurrency = (amount, currency = "Rs.") => {
	if (amount === undefined || amount === null) return `${currency} 0.00`;
	return `${currency} ${parseFloat(amount).toFixed(2)}`;
};

/**
 * Generate a unique ID
 * @returns {string} - Unique ID
 */
export const generateId = () => {
	return Date.now().toString(36) + Math.random().toString(36).substr(2);
};

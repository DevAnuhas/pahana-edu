import { useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
	Form,
	FormItem,
	FormLabel,
	FormControl,
	FormMessage,
} from "@/components/ui/form";

const LoginPage = () => {
	const [formData, setFormData] = useState({
		username: "",
		password: "",
	});
	const [errors, setErrors] = useState({});
	const [isLoading, setIsLoading] = useState(false);
	const { login } = useAuth();
	const navigate = useNavigate();

	const handleInputChange = (e) => {
		const { name, value } = e.target;
		setFormData((prev) => ({
			...prev,
			[name]: value,
		}));
		// Clear error when user starts typing
		if (errors[name]) {
			setErrors((prev) => ({
				...prev,
				[name]: "",
			}));
		}
	};

	const validateForm = () => {
		const newErrors = {};

		if (!formData.username.trim()) {
			newErrors.username = "Username is required";
		}

		if (!formData.password) {
			newErrors.password = "Password is required";
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleSubmit = async (e) => {
		e.preventDefault();

		if (!validateForm()) {
			return;
		}

		setIsLoading(true);

		try {
			const credentials = {
				username: formData.username,
				password: formData.password,
			};

			const result = await login(credentials);

			if (result.success) {
				navigate("/admin/dashboard");
			} else {
				setErrors({ general: result.error });
			}
		} catch (error) {
			console.error("Login error:", error);
			setErrors({ general: "An unexpected error occurred. Please try again." });
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="flex min-h-screen items-center justify-center bg-gray-50">
			<div className="w-full max-w-md space-y-8 rounded-lg bg-white p-8 shadow-lg">
				<div className="text-center">
					<h2 className="text-3xl font-bold tracking-tight text-gray-900">
						Pahana Edu
					</h2>
					<p className="mt-2 text-sm text-gray-600">Sign in to your account</p>
				</div>

				<Form onSubmit={handleSubmit} className="mt-8 space-y-6">
					{errors.general && (
						<div className="rounded-md bg-red-50 p-4">
							<div className="text-sm text-red-800">{errors.general}</div>
						</div>
					)}

					<FormItem>
						<FormLabel htmlFor="username">Username</FormLabel>
						<FormControl>
							<Input
								id="username"
								name="username"
								type="text"
								autoComplete="username"
								required
								value={formData.username}
								onChange={handleInputChange}
								className={errors.username ? "border-red-500" : ""}
								placeholder="Enter your username"
							/>
						</FormControl>
						{errors.username && <FormMessage>{errors.username}</FormMessage>}
					</FormItem>

					<FormItem>
						<FormLabel htmlFor="password">Password</FormLabel>
						<FormControl>
							<Input
								id="password"
								name="password"
								type="password"
								autoComplete="current-password"
								required
								value={formData.password}
								onChange={handleInputChange}
								className={errors.password ? "border-red-500" : ""}
								placeholder="Enter your password"
							/>
						</FormControl>
						{errors.password && <FormMessage>{errors.password}</FormMessage>}
					</FormItem>

					<Button type="submit" className="w-full" disabled={isLoading}>
						{isLoading ? "Signing in..." : "Sign in"}
					</Button>
				</Form>

				<div className="text-center text-sm text-gray-600">
					<p>Demo credentials: admin / admin1234</p>
				</div>
			</div>
		</div>
	);
};

export default LoginPage;

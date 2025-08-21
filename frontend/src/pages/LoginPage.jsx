import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
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
	const [isLoading, setIsLoading] = useState(false);
	const [generalError, setGeneralError] = useState("");

	const loginSchema = z.object({
		username: z.string().min(1, "Username is required"),
		password: z.string().min(1, "Password is required"),
	});

	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm({
		resolver: zodResolver(loginSchema),
		mode: "onChange",
	});
	const { login } = useAuth();
	const navigate = useNavigate();

	const onSubmit = async (values) => {
		setGeneralError("");
		setIsLoading(true);
		try {
			const credentials = {
				username: values.username,
				password: values.password,
			};

			const result = await login(credentials);

			if (result.success) {
				navigate("/admin/dashboard");
			} else {
				setGeneralError(result.error || "Invalid credentials");
			}
		} catch (error) {
			console.error("Login error:", error);
			setGeneralError("An unexpected error occurred. Please try again.");
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

				<Form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
					{generalError && (
						<div className="rounded-md bg-red-50 p-4">
							<div className="text-sm text-red-800">{generalError}</div>
						</div>
					)}

					<FormItem>
						<FormLabel htmlFor="username">Username</FormLabel>
						<FormControl>
							<Input
								id="username"
								{...register("username")}
								type="text"
								autoComplete="username"
								placeholder="Enter your username"
								className={errors.username ? "border-red-500" : ""}
							/>
						</FormControl>
						{errors.username && (
							<FormMessage>{errors.username.message}</FormMessage>
						)}
					</FormItem>

					<FormItem>
						<FormLabel htmlFor="password">Password</FormLabel>
						<FormControl>
							<Input
								id="password"
								{...register("password")}
								type="password"
								autoComplete="current-password"
								placeholder="Enter your password"
								className={errors.password ? "border-red-500" : ""}
							/>
						</FormControl>
						{errors.password && (
							<FormMessage>{errors.password.message}</FormMessage>
						)}
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

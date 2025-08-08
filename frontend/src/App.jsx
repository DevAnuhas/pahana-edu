import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";

const AuthRedirect = () => {
	const { isAuthenticated, loading } = useAuth();

	if (loading) {
		return (
			<div className="flex min-h-screen items-center justify-center">
				<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
			</div>
		);
	}

	return <Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />;
};

function App() {
	return (
		<AuthProvider>
			<Router>
				<Routes>
					<Route path="/" element={<AuthRedirect />} />

					<Route path="/login" element={<LoginPage />} />

					<Route
						path="/dashboard"
						element={
							<ProtectedRoute>
								<Dashboard />
							</ProtectedRoute>
						}
					/>
					{/* <Route path="*" element={<Navigate to="/404" />} /> */}
				</Routes>
			</Router>
		</AuthProvider>
	);
}

export default App;

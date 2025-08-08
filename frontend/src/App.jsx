import React from "react";
import {
	BrowserRouter as Router,
	Routes,
	Route,
	Navigate,
} from "react-router-dom";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import ProtectedLayout from "./layouts/ProtectedLayout";
import AdminLayout from "./layouts/AdminLayout";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";
import CustomerManagement from "./pages/CustomerManagement";

const AuthRedirect = () => {
	const { isAuthenticated, loading } = useAuth();

	if (loading) {
		return (
			<div className="flex min-h-screen items-center justify-center">
				<div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
			</div>
		);
	}

	return (
		<Navigate to={isAuthenticated ? "/admin/dashboard" : "/login"} replace />
	);
};

function App() {
	return (
		<AuthProvider>
			<Router>
				<Routes>
					<Route path="/" element={<AuthRedirect />} />

					<Route path="/login" element={<LoginPage />} />

					<Route element={<ProtectedLayout />}>
						<Route path="/admin" element={<AdminLayout />}>
							<Route path="dashboard" element={<Dashboard />} />
							<Route path="customers" element={<CustomerManagement />} />
						</Route>
					</Route>

					{/* <Route path="*" element={<Navigate to="/404" />} /> */}
				</Routes>
			</Router>
		</AuthProvider>
	);
}

export default App;

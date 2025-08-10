import React, { useState, useEffect } from "react";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
	Users,
	Package,
	Receipt,
	DollarSign,
	ShoppingCart,
	Loader2,
	HelpCircle,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import customerAPI from "@/services/customerAPI";
import bookAPI from "@/services/bookAPI";
import billingAPI from "@/services/billingAPI";
import { formatCurrency } from "@/services/utils";
import { showToast } from "@/lib/toast";

export default function Dashboard() {
	const [isLoading, setIsLoading] = useState(true);
	const [dashboardData, setDashboardData] = useState({
		customerCount: 0,
		bookCount: 0,
		monthlySales: 0,
		orderCount: 0,
		recentInvoices: [],
	});
	const navigate = useNavigate();

	useEffect(() => {
		fetchDashboardData();
	}, []);

	const fetchDashboardData = async () => {
		setIsLoading(true);
		try {
			const customers = await customerAPI.getAllCustomers();
			const customerCount = customers ? customers.length : 0;

			const books = await bookAPI.getAllBooks();
			const bookCount = books ? books.length : 0;

			const invoices = await billingAPI.getAllInvoices();

			let totalSales = 0;
			const recentInvoices = [];

			if (invoices && invoices.length > 0) {
				invoices.sort(
					(a, b) => new Date(b.invoiceDate) - new Date(a.invoiceDate)
				);

				totalSales = invoices.reduce((sum, invoice) => {
					return sum + parseFloat(invoice.totalAmount || 0);
				}, 0);

				const recent = invoices.slice(0, 4).map((invoice) => ({
					id: invoice.invoiceNumber,
					customer: invoice.customerName || "Walk-in Customer",
					amount: formatCurrency(invoice.totalAmount),
					status: "Completed",
				}));

				recentInvoices.push(...recent);
			}

			setDashboardData({
				customerCount,
				bookCount,
				monthlySales: totalSales,
				orderCount: invoices ? invoices.length : 0,
				recentInvoices,
			});
		} catch (error) {
			console.error("Error fetching dashboard data:", error);
			showToast.error("Failed to load dashboard data");
		} finally {
			setIsLoading(false);
		}
	};

	const handleQuickAction = (action) => {
		switch (action) {
			case "new-bill":
				navigate("/admin/billing");
				break;
			case "new-customer":
				navigate("/admin/customers");
				break;
			case "new-book":
				navigate("/admin/items");
				break;
			case "help":
				navigate("/admin/help");
				break;
			default:
				break;
		}
	};

	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
				<p className="text-muted-foreground">
					Welcome to Pahana Edu Bookshop Management System
				</p>
			</div>

			{isLoading ? (
				<div className="flex items-center justify-center h-64">
					<Loader2 className="h-8 w-8 animate-spin text-primary" />
					<span className="ml-2">Loading dashboard data...</span>
				</div>
			) : (
				<>
					{/* Stats Cards */}
					<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Customers
								</CardTitle>
								<Users className="h-4 w-4 text-muted-foreground" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{dashboardData.customerCount}
								</div>
								<p className="text-xs text-muted-foreground">
									Active registered customers
								</p>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Books in Stock
								</CardTitle>
								<Package className="h-4 w-4 text-muted-foreground" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{dashboardData.bookCount}
								</div>
								<p className="text-xs text-muted-foreground">
									Total books in inventory
								</p>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Sales
								</CardTitle>
								<DollarSign className="h-4 w-4 text-muted-foreground" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{formatCurrency(dashboardData.monthlySales)}
								</div>
								<p className="text-xs text-muted-foreground">
									Cumulative sales amount
								</p>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Orders
								</CardTitle>
								<ShoppingCart className="h-4 w-4 text-muted-foreground" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{dashboardData.orderCount}
								</div>
								<p className="text-xs text-muted-foreground">
									Completed transactions
								</p>
							</CardContent>
						</Card>
					</div>

					{/* Recent Activity */}
					<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
						<Card className="col-span-4">
							<CardHeader>
								<CardTitle>Recent Orders</CardTitle>
								<CardDescription>Latest billing transactions</CardDescription>
							</CardHeader>
							<CardContent>
								<div className="space-y-4">
									{dashboardData.recentInvoices.length === 0 ? (
										<p className="text-sm text-muted-foreground">
											No recent orders found
										</p>
									) : (
										dashboardData.recentInvoices.map((order) => (
											<div
												key={order.id}
												className="flex items-center justify-between"
											>
												<div className="space-y-1">
													<p className="text-sm font-medium">{order.id}</p>
													<p className="text-sm text-muted-foreground">
														{order.customer}
													</p>
												</div>
												<div className="text-right">
													<p className="text-sm font-medium">{order.amount}</p>
													<Badge
														variant={
															order.status === "Completed"
																? "default"
																: order.status === "Pending"
																? "secondary"
																: "outline"
														}
													>
														{order.status}
													</Badge>
												</div>
											</div>
										))
									)}
								</div>
							</CardContent>
						</Card>

						<Card className="col-span-3">
							<CardHeader>
								<CardTitle>Quick Actions</CardTitle>
								<CardDescription>Common tasks and shortcuts</CardDescription>
							</CardHeader>
							<CardContent className="space-y-4">
								<div className="grid gap-4">
									<Card
										className="p-3 hover:bg-muted/50 cursor-pointer transition-colors"
										onClick={() => handleQuickAction("new-bill")}
									>
										<div className="flex items-center gap-2">
											<Receipt className="h-4 w-4" />
											<span className="text-sm font-medium">
												Create New Bill
											</span>
										</div>
									</Card>
									<Card
										className="p-3 hover:bg-muted/50 cursor-pointer transition-colors"
										onClick={() => handleQuickAction("new-customer")}
									>
										<div className="flex items-center gap-2">
											<Users className="h-4 w-4" />
											<span className="text-sm font-medium">
												Add New Customer
											</span>
										</div>
									</Card>
									<Card
										className="p-3 hover:bg-muted/50 cursor-pointer transition-colors"
										onClick={() => handleQuickAction("new-book")}
									>
										<div className="flex items-center gap-2">
											<Package className="h-4 w-4" />
											<span className="text-sm font-medium">Add New Book</span>
										</div>
									</Card>
									<Card
										className="p-3 hover:bg-muted/50 cursor-pointer transition-colors"
										onClick={() => handleQuickAction("help")}
									>
										<div className="flex items-center gap-2">
											<HelpCircle className="h-4 w-4" />
											<span className="text-sm font-medium">
												Help & Support
											</span>
										</div>
									</Card>
								</div>
							</CardContent>
						</Card>
					</div>
				</>
			)}
		</div>
	);
}

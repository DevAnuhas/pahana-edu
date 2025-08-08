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
	TrendingUp,
	DollarSign,
	ShoppingCart,
} from "lucide-react";

export default function Dashboard() {
	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
				<p className="text-muted-foreground">
					Welcome to Pahana Edu Bookshop Management System
				</p>
			</div>

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
						<div className="text-2xl font-bold">1,234</div>
						<p className="text-xs text-muted-foreground">
							+12% from last month
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
						<div className="text-2xl font-bold">5,678</div>
						<p className="text-xs text-muted-foreground">+8% from last month</p>
					</CardContent>
				</Card>
				<Card>
					<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
						<CardTitle className="text-sm font-medium">Monthly Sales</CardTitle>
						<DollarSign className="h-4 w-4 text-muted-foreground" />
					</CardHeader>
					<CardContent>
						<div className="text-2xl font-bold">Rs. 45,231</div>
						<p className="text-xs text-muted-foreground">
							+20% from last month
						</p>
					</CardContent>
				</Card>
				<Card>
					<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
						<CardTitle className="text-sm font-medium">Total Orders</CardTitle>
						<ShoppingCart className="h-4 w-4 text-muted-foreground" />
					</CardHeader>
					<CardContent>
						<div className="text-2xl font-bold">892</div>
						<p className="text-xs text-muted-foreground">
							+15% from last month
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
							{[
								{
									id: "INV-001",
									customer: "John Doe",
									amount: "Rs. 1,250",
									status: "Completed",
								},
								{
									id: "INV-002",
									customer: "Jane Smith",
									amount: "Rs. 890",
									status: "Pending",
								},
								{
									id: "INV-003",
									customer: "Mike Johnson",
									amount: "Rs. 2,100",
									status: "Completed",
								},
								{
									id: "INV-004",
									customer: "Sarah Wilson",
									amount: "Rs. 750",
									status: "Processing",
								},
							].map((order) => (
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
							))}
						</div>
					</CardContent>
				</Card>

				<Card className="col-span-3">
					<CardHeader>
						<CardTitle>Quick Actions</CardTitle>
						<CardDescription>Common tasks and shortcuts</CardDescription>
					</CardHeader>
					<CardContent className="space-y-4">
						<div className="grid gap-2">
							<Card className="p-3 hover:bg-muted/50 cursor-pointer transition-colors">
								<div className="flex items-center gap-2">
									<Receipt className="h-4 w-4" />
									<span className="text-sm font-medium">Create New Bill</span>
								</div>
							</Card>
							<Card className="p-3 hover:bg-muted/50 cursor-pointer transition-colors">
								<div className="flex items-center gap-2">
									<Users className="h-4 w-4" />
									<span className="text-sm font-medium">Add New Customer</span>
								</div>
							</Card>
							<Card className="p-3 hover:bg-muted/50 cursor-pointer transition-colors">
								<div className="flex items-center gap-2">
									<Package className="h-4 w-4" />
									<span className="text-sm font-medium">Add New Book</span>
								</div>
							</Card>
							<Card className="p-3 hover:bg-muted/50 cursor-pointer transition-colors">
								<div className="flex items-center gap-2">
									<TrendingUp className="h-4 w-4" />
									<span className="text-sm font-medium">View Reports</span>
								</div>
							</Card>
						</div>
					</CardContent>
				</Card>
			</div>
		</div>
	);
}

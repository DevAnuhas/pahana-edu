import { Outlet, useLocation, Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { SidebarProvider } from "@/components/ui/sidebar";
import { AppSidebar } from "@/components/app-sidebar";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import {
	Breadcrumb,
	BreadcrumbItem,
	BreadcrumbLink,
	BreadcrumbList,
	BreadcrumbPage,
	BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Button } from "@/components/ui/button";
import { LogOut } from "lucide-react";
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@/components/ui/tooltip";
import {
	AlertDialog,
	AlertDialogAction,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
	AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

export default function AdminLayout() {
	const { user, logout } = useAuth();
	const location = useLocation();

	const pageNameMapping = {
		dashboard: "Dashboard",
		customers: "Customer Management",
		items: "Item Management",
		billing: "Billing System",
		bills: "Bill Management",
		help: "Help Section",
		settings: "Settings",
	};

	const getCurrentPage = () => {
		const path = location.pathname.split("/").filter(Boolean);
		if (path.length > 1) {
			const pageName = path[1];
			return (
				pageNameMapping[pageName] ||
				pageName.charAt(0).toUpperCase() + pageName.slice(1).replace(/-/g, " ")
			);
		}
		return "Dashboard";
	};

	return (
		<SidebarProvider>
			<AppSidebar />
			<SidebarInset>
				<header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
					<SidebarTrigger className="-ml-1" />
					<Separator orientation="vertical" className="mr-2 h-4" />
					<Breadcrumb>
						<BreadcrumbList>
							<BreadcrumbItem className="hidden md:block">
								<BreadcrumbLink asChild>
									<Link to="/admin/dashboard">Pahana Edu Bookshop</Link>
								</BreadcrumbLink>
							</BreadcrumbItem>
							<BreadcrumbSeparator className="hidden md:block" />
							<BreadcrumbItem>
								<TooltipProvider>
									<Tooltip>
										<TooltipTrigger asChild>
											<BreadcrumbPage>{getCurrentPage()}</BreadcrumbPage>
										</TooltipTrigger>
										<TooltipContent>
											<p>You are here: {getCurrentPage()}</p>
										</TooltipContent>
									</Tooltip>
								</TooltipProvider>
							</BreadcrumbItem>
						</BreadcrumbList>
					</Breadcrumb>
					<div className="ml-auto items-center flex gap-4">
						<div className="flex items-center gap-4">
							<div className="flex items-end justify-center flex-col sm:flex-row gap-2">
								<span className="text-sm text-gray-700">
									Welcome, {user?.fullName || user?.username}
								</span>
								<div className="text-xs text-amber-950 bg-amber-200 py-0.5 px-2 font-bold rounded-full ">
									{user?.role}
								</div>
							</div>

							<AlertDialog>
								<AlertDialogTrigger>
									<Button variant="outline">
										<LogOut className="mr-2" />
										Logout
									</Button>
								</AlertDialogTrigger>
								<AlertDialogContent>
									<AlertDialogHeader>
										<AlertDialogTitle>
											Are you sure you want to logout?
										</AlertDialogTitle>
									</AlertDialogHeader>
									<AlertDialogFooter>
										<AlertDialogCancel>Cancel</AlertDialogCancel>
										<AlertDialogAction onClick={logout}>
											Logout
										</AlertDialogAction>
									</AlertDialogFooter>
								</AlertDialogContent>
							</AlertDialog>
						</div>
					</div>
				</header>
				<div className="flex flex-1 flex-col gap-4 p-4">
					<Outlet className="mr-2" />
				</div>
			</SidebarInset>
		</SidebarProvider>
	);
}

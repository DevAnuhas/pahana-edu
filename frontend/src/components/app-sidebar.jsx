import {
	BookOpen,
	Users,
	Package,
	Receipt,
	FileText,
	HelpCircle,
	BarChart3,
	Settings,
} from "lucide-react";
import { Link } from "react-router-dom";

import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroup,
	SidebarGroupContent,
	SidebarGroupLabel,
	SidebarHeader,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
} from "@/components/ui/sidebar";

const menuItems = [
	{
		title: "Dashboard",
		url: "/admin/dashboard",
		icon: BarChart3,
	},
	{
		title: "Customer Management",
		url: "/admin/customers",
		icon: Users,
	},
	{
		title: "Item Management",
		url: "/admin/items",
		icon: Package,
	},
	{
		title: "Billing System",
		url: "/admin/billing",
		icon: Receipt,
	},
	{
		title: "Bill Management",
		url: "/admin/bills",
		icon: FileText,
	},
	{
		title: "Help Section",
		url: "/admin/help",
		icon: HelpCircle,
	},
];

export function AppSidebar() {
	const location = window.location.pathname;

	return (
		<Sidebar>
			<SidebarHeader>
				<div className="flex items-center gap-2 px-2 py-2">
					<BookOpen className="h-6 w-6" />
					<div className="flex flex-col">
						<span className="font-semibold text-sm">Pahana Edu</span>
						<span className="text-xs text-muted-foreground">
							Bookshop System
						</span>
					</div>
				</div>
			</SidebarHeader>
			<SidebarContent>
				<SidebarGroup>
					<SidebarGroupContent>
						<SidebarMenu className="space-y-2">
							{menuItems.map((item) => (
								<SidebarMenuItem key={item.title}>
									<SidebarMenuButton asChild isActive={location === item.url}>
										<Link to={item.url}>
											<item.icon />
											<span>{item.title}</span>
										</Link>
									</SidebarMenuButton>
								</SidebarMenuItem>
							))}
						</SidebarMenu>
					</SidebarGroupContent>
				</SidebarGroup>
			</SidebarContent>
			<SidebarFooter>
				<SidebarMenu>
					<SidebarMenuItem>
						<SidebarMenuButton>
							<Link to="/admin/settings">
								<Settings />
								<span>Settings</span>
							</Link>
						</SidebarMenuButton>
					</SidebarMenuItem>
				</SidebarMenu>
			</SidebarFooter>
		</Sidebar>
	);
}

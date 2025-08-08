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
import { Link, useNavigate } from "react-router";

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
		url: "/dashboard",
		icon: BarChart3,
	},
	{
		title: "Customer Management",
		url: "/customers",
		icon: Users,
	},
	{
		title: "Item Management",
		url: "/items",
		icon: Package,
	},
	{
		title: "Billing System",
		url: "/billing",
		icon: Receipt,
	},
	{
		title: "Bill Management",
		url: "/bills",
		icon: FileText,
	},
	{
		title: "Help Section",
		url: "/help",
		icon: HelpCircle,
	},
];

export function AppSidebar() {
	const navigate = useNavigate();

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
									<SidebarMenuButton asChild isActive={navigate === item.url}>
										<Link href={item.url}>
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
							<Settings />
							<span>Settings</span>
						</SidebarMenuButton>
					</SidebarMenuItem>
				</SidebarMenu>
			</SidebarFooter>
		</Sidebar>
	);
}

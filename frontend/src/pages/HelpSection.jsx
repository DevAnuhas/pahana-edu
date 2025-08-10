import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Accordion,
	AccordionContent,
	AccordionItem,
	AccordionTrigger,
} from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import {
	BookOpen,
	Users,
	Package,
	Receipt,
	FileText,
	HelpCircle,
	Phone,
	Mail,
	MapPin,
} from "lucide-react";

const HelpSection = () => {
	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Help Section</h1>
				<p className="text-muted-foreground">
					System usage guidelines and frequently asked questions
				</p>
			</div>

			{/* Quick Start Guide */}
			<Card>
				<CardHeader>
					<CardTitle>Quick Start Guide</CardTitle>
					<CardDescription>
						Get started with the Pahana Edu Bookshop Management System
					</CardDescription>
				</CardHeader>
				<CardContent>
					<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
						<div className="flex items-start space-x-3">
							<div className="flex-shrink-0">
								<div className="flex items-center justify-center w-8 h-8 bg-primary text-primary-foreground rounded-full text-sm font-medium">
									1
								</div>
							</div>
							<div>
								<h4 className="font-medium">Dashboard Overview</h4>
								<p className="text-sm text-muted-foreground">
									View system statistics, recent orders, and quick actions from
									the main dashboard.
								</p>
							</div>
						</div>
						<div className="flex items-start space-x-3">
							<div className="flex-shrink-0">
								<div className="flex items-center justify-center w-8 h-8 bg-primary text-primary-foreground rounded-full text-sm font-medium">
									2
								</div>
							</div>
							<div>
								<h4 className="font-medium">Manage Customers</h4>
								<p className="text-sm text-muted-foreground">
									Add, edit, and manage customer accounts with unique account
									numbers.
								</p>
							</div>
						</div>
						<div className="flex items-start space-x-3">
							<div className="flex-shrink-0">
								<div className="flex items-center justify-center w-8 h-8 bg-primary text-primary-foreground rounded-full text-sm font-medium">
									3
								</div>
							</div>
							<div>
								<h4 className="font-medium">Create Bills</h4>
								<p className="text-sm text-muted-foreground">
									Generate invoices, calculate totals, and process customer
									payments.
								</p>
							</div>
						</div>
					</div>
				</CardContent>
			</Card>

			{/* System Features */}
			<Card>
				<CardHeader>
					<CardTitle>System Features</CardTitle>
					<CardDescription>
						Overview of all available features in the system
					</CardDescription>
				</CardHeader>
				<CardContent>
					<div className="grid gap-4 md:grid-cols-2">
						<div className="space-y-4">
							<div className="flex items-center space-x-3">
								<Users className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Customer Management</h4>
									<p className="text-sm text-muted-foreground">
										Register new customers, edit information, and manage account
										details
									</p>
								</div>
							</div>
							<div className="flex items-center space-x-3">
								<Package className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Item Management</h4>
									<p className="text-sm text-muted-foreground">
										Add books, manage inventory, track stock levels, and update
										pricing
									</p>
								</div>
							</div>
							<div className="flex items-center space-x-3">
								<Receipt className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Billing System</h4>
									<p className="text-sm text-muted-foreground">
										Create invoices, calculate totals with discounts and tax
									</p>
								</div>
							</div>
						</div>
						<div className="space-y-4">
							<div className="flex items-center space-x-3">
								<FileText className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Bill Management</h4>
									<p className="text-sm text-muted-foreground">
										View all invoices, track payments, and generate reports
									</p>
								</div>
							</div>
							<div className="flex items-center space-x-3">
								<BookOpen className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Reports & Analytics</h4>
									<p className="text-sm text-muted-foreground">
										Generate sales reports and view business analytics
									</p>
								</div>
							</div>
							<div className="flex items-center space-x-3">
								<HelpCircle className="h-5 w-5 text-primary" />
								<div>
									<h4 className="font-medium">Help & Support</h4>
									<p className="text-sm text-muted-foreground">
										Access documentation, tutorials, and system guidelines
									</p>
								</div>
							</div>
						</div>
					</div>
				</CardContent>
			</Card>

			{/* FAQ Section */}
			<Card>
				<CardHeader>
					<CardTitle>Frequently Asked Questions</CardTitle>
					<CardDescription>
						Common questions and answers about using the system
					</CardDescription>
				</CardHeader>
				<CardContent>
					<Accordion type="single" collapsible className="w-full">
						<AccordionItem value="item-1">
							<AccordionTrigger>How do I add a new customer?</AccordionTrigger>
							<AccordionContent>
								<div className="space-y-2">
									<p>To add a new customer:</p>
									<ol className="list-decimal list-inside space-y-1 text-sm">
										<li>Navigate to the Customer Management page</li>
										<li>Click the "Add Customer" button</li>
										<li>
											Fill in the required information (Account Number, Name,
											Address, Telephone)
										</li>
										<li>Click "Add Customer" to save the information</li>
									</ol>
									<p className="text-sm text-muted-foreground">
										Note: Each customer must have a unique account number.
									</p>
								</div>
							</AccordionContent>
						</AccordionItem>

						<AccordionItem value="item-2">
							<AccordionTrigger>
								How do I create a bill for a customer?
							</AccordionTrigger>
							<AccordionContent>
								<div className="space-y-2">
									<p>To create a new bill:</p>
									<ol className="list-decimal list-inside space-y-1 text-sm">
										<li>Go to the Billing System page</li>
										<li>Select the customer from the dropdown</li>
										<li>
											Add items by selecting books, quantity, and discount
										</li>
										<li>Review the bill summary with totals</li>
										<li>
											Click "Save Bill" to store or "Print" to generate invoice
										</li>
									</ol>
								</div>
							</AccordionContent>
						</AccordionItem>

						<AccordionItem value="item-3">
							<AccordionTrigger>
								How do I manage book inventory?
							</AccordionTrigger>
							<AccordionContent>
								<div className="space-y-2">
									<p>To manage books and inventory:</p>
									<ol className="list-decimal list-inside space-y-1 text-sm">
										<li>Access the Item Management page</li>
										<li>View current stock levels and book information</li>
										<li>Add new books using the "Add Book" button</li>
										<li>Edit existing book details using the edit button</li>
										<li>
											Monitor stock status (In Stock, Low Stock, Out of Stock)
										</li>
									</ol>
								</div>
							</AccordionContent>
						</AccordionItem>

						<AccordionItem value="item-4">
							<AccordionTrigger>
								How do I view and manage existing bills?
							</AccordionTrigger>
							<AccordionContent>
								<div className="space-y-2">
									<p>To manage existing bills:</p>
									<ol className="list-decimal list-inside space-y-1 text-sm">
										<li>Navigate to the Bill Management page</li>
										<li>Use the search function to find specific bills</li>
										<li>Filter bills by status (Paid, Pending)</li>
										<li>
											View detailed bill information by clicking the eye icon
										</li>
										<li>
											Print or download bills using the respective buttons
										</li>
									</ol>
								</div>
							</AccordionContent>
						</AccordionItem>

						<AccordionItem value="item-5">
							<AccordionTrigger>
								What payment methods are supported?
							</AccordionTrigger>
							<AccordionContent>
								<p>The system supports the following payment methods:</p>
								<div className="flex gap-2 mt-2">
									<Badge variant="outline">Cash</Badge>
									<Badge variant="outline">Card</Badge>
									<Badge variant="outline">Online</Badge>
								</div>
								<p className="text-sm text-muted-foreground mt-2">
									Payment method can be selected during bill creation and is
									recorded for tracking purposes.
								</p>
							</AccordionContent>
						</AccordionItem>

						<AccordionItem value="item-6">
							<AccordionTrigger>
								How are discounts and taxes calculated?
							</AccordionTrigger>
							<AccordionContent>
								<div className="space-y-2">
									<p>The system calculates totals as follows:</p>
									<ul className="list-disc list-inside space-y-1 text-sm">
										<li>
											<strong>Subtotal:</strong> Sum of all item prices ×
											quantities
										</li>
										<li>
											<strong>Discount:</strong> Applied per item as a
											percentage
										</li>
										<li>
											<strong>Tax:</strong> 5% applied to (Subtotal - Discount)
										</li>
										<li>
											<strong>Total:</strong> Subtotal - Discount + Tax
										</li>
									</ul>
								</div>
							</AccordionContent>
						</AccordionItem>
					</Accordion>
				</CardContent>
			</Card>

			{/* Contact Information */}
			<Card>
				<CardHeader>
					<CardTitle>Contact Support</CardTitle>
					<CardDescription>
						Need additional help? Contact our support team
					</CardDescription>
				</CardHeader>
				<CardContent>
					<div className="grid gap-4 md:grid-cols-3">
						<div className="flex items-center space-x-3">
							<Phone className="h-5 w-5 text-primary" />
							<div>
								<h4 className="font-medium">Phone Support</h4>
								<p className="text-sm text-muted-foreground">+94 11 234 5678</p>
								<p className="text-xs text-muted-foreground">
									Mon-Fri, 9AM-5PM
								</p>
							</div>
						</div>
						<div className="flex items-center space-x-3">
							<Mail className="h-5 w-5 text-primary" />
							<div>
								<h4 className="font-medium">Email Support</h4>
								<p className="text-sm text-muted-foreground">
									support@pahanaedu.com
								</p>
								<p className="text-xs text-muted-foreground">24/7 Response</p>
							</div>
						</div>
						<div className="flex items-center space-x-3">
							<MapPin className="h-5 w-5 text-primary" />
							<div>
								<h4 className="font-medium">Visit Us</h4>
								<p className="text-sm text-muted-foreground">123 Main Street</p>
								<p className="text-sm text-muted-foreground">
									Colombo 01, Sri Lanka
								</p>
							</div>
						</div>
					</div>
				</CardContent>
			</Card>
		</div>
	);
};

export default HelpSection;

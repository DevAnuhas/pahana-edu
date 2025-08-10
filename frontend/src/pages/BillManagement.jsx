import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Search, PrinterIcon, Loader2 } from "lucide-react";
import billingAPI from "@/services/billingAPI";
import { formatCurrency } from "@/services/utils";
import { showToast } from "@/lib/toast";
import PrintBillDialog from "@/components/print-bill-dialog";

const BillManagement = () => {
	// State variables
	const [bills, setBills] = useState([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [statusFilter, setStatusFilter] = useState("all");
	const [isLoading, setIsLoading] = useState(true);
	const [printingBillId, setPrintingBillId] = useState(null); // Track which bill is printing
	const [printContent, setPrintContent] = useState("");
	const [printDialogOpen, setPrintDialogOpen] = useState(false);
	const [selectedPrintBill, setSelectedPrintBill] = useState(null);

	useEffect(() => {
		fetchBills();
	}, []);

	const fetchBills = async () => {
		setIsLoading(true);
		try {
			const response = await billingAPI.getAllInvoices();

			// Process invoices to match our UI data model
			const processedBills = response.map((invoice) => ({
				id: invoice.id,
				invoiceNumber: invoice.invoiceNumber,
				customerName: invoice.customerName || "Walk-in Customer",
				customerAccount: invoice.customerId
					? `ACC-${invoice.customerId.toString().padStart(3, "0")}`
					: "WALK-IN",
				date: new Date(invoice.invoiceDate).toLocaleDateString(),
				items: invoice.items ? invoice.items.length : 0,
				subtotal: invoice.subtotal,
				discount: invoice.discountAmount,
				tax: invoice.taxAmount,
				total: invoice.totalAmount,
				status: "Paid", // Assuming all invoices are paid
				paymentMethod: invoice.paymentMethod,
				rawInvoice: invoice, // Keep the raw invoice data for details
			}));

			setBills(processedBills);
		} catch (error) {
			console.error("Error fetching bills:", error);
			showToast.error("Failed to load bills");
		} finally {
			setIsLoading(false);
		}
	};

	const filteredBills = bills.filter((bill) => {
		const matchesSearch =
			bill.invoiceNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
			bill.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
			bill.customerAccount.toLowerCase().includes(searchTerm.toLowerCase());

		const matchesStatus =
			statusFilter === "all" ||
			bill.status.toLowerCase() === statusFilter.toLowerCase();

		return matchesSearch && matchesStatus;
	});

	const handlePrintBill = async (bill) => {
		setSelectedPrintBill(bill);
		setPrintingBillId(bill.id);
		try {
			const response = await billingAPI.printInvoice(bill.id);

			if (response && response.status === "success") {
				setPrintContent(response.printableInvoice);
				setPrintDialogOpen(true);
			} else {
				showToast.error("Failed to generate printable bill");
			}
		} catch (error) {
			console.error("Error printing bill:", error);
			showToast.error("Failed to print bill");
		} finally {
			setPrintingBillId(null);
		}
	};

	const getTotalRevenue = () => {
		return bills.reduce((sum, bill) => sum + parseFloat(bill.total), 0);
	};

	const getPaidBills = () => {
		return bills.filter((bill) => bill.status === "Paid").length;
	};

	const getPendingBills = () => {
		return bills.filter((bill) => bill.status === "Pending").length;
	};

	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Bill Management</h1>
				<p className="text-muted-foreground">
					View and manage all customer bills and invoices
				</p>
			</div>

			{isLoading ? (
				<div className="flex items-center justify-center h-64">
					<Loader2 className="h-8 w-8 animate-spin text-primary" />
					<span className="ml-2">Loading bills...</span>
				</div>
			) : (
				<>
					{/* Summary Cards */}
					<div className="grid gap-4 md:grid-cols-4">
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Bills
								</CardTitle>
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">{bills.length}</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Paid Bills
								</CardTitle>
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold text-green-600">
									{getPaidBills()}
								</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Pending Bills
								</CardTitle>
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold text-yellow-600">
									{getPendingBills()}
								</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Revenue
								</CardTitle>
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{formatCurrency(getTotalRevenue())}
								</div>
							</CardContent>
						</Card>
					</div>

					{/* Bills Table */}
					<Card>
						<CardHeader>
							<CardTitle>All Bills</CardTitle>
							<CardDescription>
								Complete list of customer bills and invoices
							</CardDescription>
							<div className="flex items-center space-x-2">
								<Search className="h-4 w-4 text-muted-foreground" />
								<Input
									placeholder="Search bills..."
									value={searchTerm}
									onChange={(e) => setSearchTerm(e.target.value)}
									className="max-w-sm"
								/>
								<Select value={statusFilter} onValueChange={setStatusFilter}>
									<SelectTrigger className="w-[180px]">
										<SelectValue placeholder="Filter by status" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="all">All Status</SelectItem>
										<SelectItem value="paid">Paid</SelectItem>
										<SelectItem value="pending">Pending</SelectItem>
									</SelectContent>
								</Select>
							</div>
						</CardHeader>
						<CardContent>
							{filteredBills.length === 0 ? (
								<div className="text-center py-8 text-muted-foreground">
									No bills found matching your criteria
								</div>
							) : (
								<Table>
									<TableHeader>
										<TableRow>
											<TableHead>Invoice No.</TableHead>
											<TableHead>Customer</TableHead>
											<TableHead>Date</TableHead>
											<TableHead>Total</TableHead>
											<TableHead>Status</TableHead>
											<TableHead>Payment</TableHead>
											<TableHead>Actions</TableHead>
										</TableRow>
									</TableHeader>
									<TableBody>
										{filteredBills.map((bill) => (
											<TableRow key={bill.id}>
												<TableCell className="font-mono">
													{bill.invoiceNumber}
												</TableCell>
												<TableCell>
													<div>
														<div className="font-medium">
															{bill.customerName}
														</div>
														<div className="text-sm text-muted-foreground">
															{bill.customerAccount}
														</div>
													</div>
												</TableCell>
												<TableCell>{bill.date}</TableCell>
												<TableCell className="font-medium">
													{formatCurrency(bill.total)}
												</TableCell>
												<TableCell>
													<Badge
														variant={
															bill.status === "Paid" ? "default" : "secondary"
														}
													>
														{bill.status}
													</Badge>
												</TableCell>
												<TableCell>
													<Badge variant="outline">{bill.paymentMethod}</Badge>
												</TableCell>
												<TableCell>
													<Button
														variant="outline"
														size="sm"
														onClick={() => handlePrintBill(bill)}
														disabled={printingBillId === bill.id}
													>
														{printingBillId === bill.id ? (
															<>
																<Loader2 className="h-4 w-4 mr-1 animate-spin" />
																Print
															</>
														) : (
															<>
																<PrinterIcon className="h-4 w-4 mr-1" />
																Print
															</>
														)}
													</Button>
												</TableCell>
											</TableRow>
										))}
									</TableBody>
								</Table>
							)}
						</CardContent>
					</Card>

					{/* Print Dialog */}
					<PrintBillDialog
						open={printDialogOpen}
						onOpenChange={setPrintDialogOpen}
						bill={selectedPrintBill}
						printContent={printContent}
						isPrinting={!!printingBillId}
						onPrint={() => {
							// Custom print handler if needed
							setPrintDialogOpen(false);
							showToast.success("Bill sent to printer");
						}}
					/>
				</>
			)}
		</div>
	);
};

export default BillManagement;

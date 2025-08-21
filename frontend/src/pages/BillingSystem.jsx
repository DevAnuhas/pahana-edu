import React, { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
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
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Plus, Trash2, PrinterIcon, Save, Loader2 } from "lucide-react";
import PrintBillDialog from "@/components/print-bill-dialog";
import customerAPI from "@/services/customerAPI";
import bookAPI from "@/services/bookAPI";
import billingAPI from "@/services/billingAPI";
import { showToast } from "@/lib/toast";
import { formatCurrency } from "@/services/utils";
import { useAuth } from "@/contexts/AuthContext";

const BillingSystem = () => {
	const [customers, setCustomers] = useState([]);
	const [books, setBooks] = useState([]);
	const [billItems, setBillItems] = useState([]);
	const [isLoading, setIsLoading] = useState(false);
	const [isSaving, setIsSaving] = useState(false);
	const [isPrinting, setIsPrinting] = useState(false);
	const [printDialogOpen, setPrintDialogOpen] = useState(false);
	const [printContent, setPrintContent] = useState("");

	const { user } = useAuth();

	// react-hook-form + zod schema for billing fields
	const billFormSchema = z.object({
		selectedCustomer: z.string().min(1, "Please select a customer"),
		selectedBook: z.string().optional(),
		quantity: z.preprocess(
			(val) => (val === "" ? undefined : Number(val)),
			z.number().int().min(1, "Quantity must be at least 1")
		),
		discount: z.preprocess(
			(val) => (val === "" ? undefined : Number(val)),
			z
				.number()
				.min(0, "Discount must be >= 0")
				.max(100, "Discount must be <= 100")
		),
		applyTax: z.boolean().optional(),
	});

	const {
		register,
		control,
		handleSubmit,
		setValue,
		getValues,
		watch,
		trigger,
	} = useForm({
		resolver: zodResolver(billFormSchema),
		mode: "onChange",
		defaultValues: {
			selectedCustomer: "",
			selectedBook: "",
			quantity: 1,
			discount: 0,
			applyTax: true,
		},
	});

	watch("applyTax", true); // keep watch so applyTax checkbox updates live

	const subtotal = billItems.reduce(
		(sum, item) => sum + item.price * item.quantity,
		0
	);

	const totalDiscount = billItems.reduce(
		(sum, item) => sum + (item.price * item.quantity * item.discount) / 100,
		0
	);

	const tax = watch("applyTax") ? (subtotal - totalDiscount) * 0.05 : 0; // 5% tax
	const grandTotal = subtotal - totalDiscount + tax;

	// Generate invoice number
	const [invoiceNumber, setInvoiceNumber] = useState("");

	useEffect(() => {
		setInvoiceNumber(billingAPI.generateInvoiceNumber());
	}, []);

	useEffect(() => {
		fetchData();
	}, []);

	const fetchData = async () => {
		setIsLoading(true);
		try {
			let customersData = [];
			let booksData = [];

			try {
				const customersResponse = await customerAPI.getAllCustomers();
				customersData = customersResponse || [];
			} catch (error) {
				console.error("Error fetching customers:", error);
				showToast.error("Failed to load customers data");
			}

			try {
				const booksResponse = await bookAPI.getAllBooks();
				booksData = booksResponse || [];
			} catch (error) {
				console.error("Error fetching books:", error);
				showToast.error("Failed to load books data");
			}

			setCustomers(customersData);
			setBooks(booksData);
		} catch (error) {
			console.error("Error in fetchData:", error);
		} finally {
			setIsLoading(false);
		}
	};

	const addItemToBill = () => {
		const values = getValues();
		const selectedBook = values.selectedBook;
		const quantity = Number(values.quantity) || 1;
		const discount = Number(values.discount) || 0;

		if (!selectedBook) {
			showToast.error("Please select a book to add");
			return;
		}

		const book = books.find((b) => b.id.toString() === selectedBook);
		if (!book) return;

		if (book.stockQuantity < quantity) {
			showToast.error(
				`Insufficient stock. Only ${book.stockQuantity} available.`
			);
			return;
		}

		const itemTotal =
			book.price * quantity - (book.price * quantity * discount) / 100;

		const newItem = {
			id: Date.now(),
			bookId: book.id,
			title: book.title,
			price: parseFloat(book.price),
			quantity: parseInt(quantity),
			discount: parseFloat(discount),
			total: itemTotal,
		};

		setBillItems([...billItems, newItem]);
		setValue("selectedBook", "");
		setValue("quantity", 1);
		setValue("discount", 0);
	};

	const removeItem = (id) => {
		setBillItems(billItems.filter((item) => item.id !== id));
	};

	const handleSaveBill = async (formValues) => {
		const selectedCustomer =
			formValues.selectedCustomer || getValues("selectedCustomer");
		if (!selectedCustomer || billItems.length === 0) {
			showToast.error("Please select a customer and add items to the bill");
			return;
		}

		try {
			const booksResponse = await bookAPI.getAllBooks();
			const currentBooks = booksResponse || [];

			const stockIssues = [];

			for (const item of billItems) {
				const currentBook = currentBooks.find((b) => b.id === item.bookId);
				if (!currentBook) {
					stockIssues.push(
						`Book ID ${item.bookId} (${item.title}) no longer exists`
					);
					continue;
				}

				if (currentBook.stockQuantity < item.quantity) {
					stockIssues.push(
						`${item.title}: Only ${currentBook.stockQuantity} available, but ${item.quantity} requested`
					);
				}
			}

			if (stockIssues.length > 0) {
				showToast.error("Stock availability issues detected");
				console.error("Stock issues:", stockIssues);
				alert(
					`Cannot create invoice due to stock issues:\n\n${stockIssues.join(
						"\n"
					)}`
				);
				return;
			}
		} catch (error) {
			console.error("Error checking stock:", error);
		}

		setIsPrinting(true);
		try {
			// Prepare invoice data
			const invoiceItems = billItems.map((item) => ({
				bookId: item.bookId,
				quantity: item.quantity,
				unitPrice: parseFloat(item.price),
				discountPercent: parseFloat(item.discount),
				bookTitle: item.title,
			}));

			const invoiceData = {
				invoiceNumber: invoiceNumber,
				customerId: parseInt(selectedCustomer),
				customerName: customers.find(
					(c) => c.id.toString() === selectedCustomer
				)?.name,
				items: invoiceItems,
				subtotal: parseFloat(subtotal),
				discountAmount: parseFloat(totalDiscount),
				taxAmount: parseFloat(tax),
				totalAmount: parseFloat(grandTotal),
				paymentMethod: "CASH", // Default for now
				cashierName: user?.fullName || user?.username,
				invoiceDate: new Date().toLocaleString("en-US", {
					year: "numeric",
					month: "2-digit",
					day: "2-digit",
					hour: "2-digit",
					minute: "2-digit",
					second: "2-digit",
					hour12: true,
				}),
				applyTax: !!getValues("applyTax"),
				notes: "Created via Billing System",
			};

			// Generate printable bill first
			const previewResponse = await billingAPI.generatePrintableBill(
				invoiceData
			);
			if (previewResponse && previewResponse.status === "success") {
				setPrintContent(previewResponse.printableInvoice);

				setIsSaving(true);
				setIsPrinting(false);

				const response = await billingAPI.createInvoice(invoiceData);

				if (response && response.status === "success") {
					showToast.success("Invoice created successfully");
					// Show the print dialog automatically after saving
					setPrintDialogOpen(true);

					// Reset the form
					// reset the customer field in the form
					setValue("selectedCustomer", "");
					setBillItems([]);
					setInvoiceNumber(billingAPI.generateInvoiceNumber());
				} else {
					showToast.error(
						"Failed to create invoice: " +
							(response?.message || "Unknown error")
					);
					console.error("Failed response:", response);
				}
			} else {
				showToast.error(
					"Failed to generate bill preview: " +
						(previewResponse?.message || "Unknown error")
				);
				console.error("Failed preview response:", previewResponse);
			}
		} catch (error) {
			console.error("Error in bill process:", error);

			if (error.response) {
				console.error("Response status:", error.response.status);
				console.error("Response data:", error.response.data);
				showToast.error(
					`Failed to process invoice: Server error (${
						error.response.status
					}) - ${error.response.data?.message || ""}`
				);
			} else if (error.message) {
				console.error("Error message:", error.message);
				showToast.error(`Failed to process invoice: ${error.message}`);
			} else {
				showToast.error("Failed to process invoice: Unknown error");
			}
		} finally {
			setIsSaving(false);
			setIsPrinting(false);
		}
	};

	const handlePrintBill = async (formValues) => {
		const selectedCustomer =
			formValues.selectedCustomer || getValues("selectedCustomer");
		if (!selectedCustomer || billItems.length === 0) {
			showToast.error("Please select a customer and add items to the bill");
			return;
		}

		setIsPrinting(true);
		try {
			// Prepare invoice data for preview
			const invoiceItems = billItems.map((item) => ({
				bookId: item.bookId,
				quantity: item.quantity,
				unitPrice: parseFloat(item.price),
				discountPercent: parseFloat(item.discount),
				bookTitle: item.title,
			}));

			const invoiceData = {
				invoiceNumber: invoiceNumber,
				customerId: parseInt(selectedCustomer),
				customerName: customers.find(
					(c) => c.id.toString() === selectedCustomer
				)?.name,
				items: invoiceItems,
				subtotal: parseFloat(subtotal),
				discountAmount: parseFloat(totalDiscount),
				taxAmount: parseFloat(tax),
				totalAmount: parseFloat(grandTotal),
				paymentMethod: "CASH",
				cashierName: user?.fullName || user?.username,
				invoiceDate: new Date().toLocaleString("en-US", {
					year: "numeric",
					month: "2-digit",
					day: "2-digit",
					hour: "2-digit",
					minute: "2-digit",
					second: "2-digit",
					hour12: true,
				}),
				applyTax: !!getValues("applyTax"),
				notes: "Created via Billing System",
			};

			const response = await billingAPI.generatePrintableBill(invoiceData);

			if (response && response.status === "success") {
				setPrintContent(response.printableInvoice);
				setPrintDialogOpen(true);
			} else {
				showToast.error(
					"Failed to generate printable bill: " +
						(response?.message || "Unknown error")
				);
				console.error("Failed response:", response);
			}
		} catch (error) {
			console.error("Error generating printable bill:", error);

			if (error.response) {
				showToast.error(
					`Failed to generate bill: Server error (${error.response.status})`
				);
			} else if (error.message) {
				showToast.error(`Failed to generate bill: ${error.message}`);
			} else {
				showToast.error("Failed to generate printable bill");
			}
		} finally {
			setIsPrinting(false);
		}
	};

	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Billing System</h1>
				<p className="text-muted-foreground">
					Create and process customer bills and invoices
				</p>
			</div>

			{isLoading ? (
				<div className="flex items-center justify-center h-64">
					<Loader2 className="h-8 w-8 animate-spin text-primary" />
					<span className="ml-2">Loading data...</span>
				</div>
			) : (
				<>
					<div className="grid gap-6 lg:grid-cols-2">
						{/* Bill Creation */}
						<Card>
							<CardHeader>
								<CardTitle>Create New Bill</CardTitle>
								<CardDescription>Select customer and add items</CardDescription>
							</CardHeader>
							<CardContent className="space-y-4">
								<div className="space-y-2">
									<Label htmlFor="customer">Select Customer</Label>
									<Controller
										control={control}
										name="selectedCustomer"
										render={({ field }) => (
											<Select
												value={field.value}
												onValueChange={field.onChange}
											>
												<SelectTrigger>
													<SelectValue placeholder="Choose a customer" />
												</SelectTrigger>
												<SelectContent>
													{customers.map((customer) => (
														<SelectItem
															key={customer.id}
															value={customer.id.toString()}
														>
															{customer.accountNumber} - {customer.name}
														</SelectItem>
													))}
												</SelectContent>
											</Select>
										)}
									/>
								</div>

								<Separator />

								<div className="space-y-4">
									<h4 className="font-medium">Add Items</h4>

									<div className="grid gap-4">
										<div className="space-y-2">
											<Label htmlFor="book">Select Book</Label>
											<Controller
												control={control}
												name="selectedBook"
												render={({ field }) => (
													<Select
														value={field.value}
														onValueChange={field.onChange}
													>
														<SelectTrigger>
															<SelectValue placeholder="Choose a book" />
														</SelectTrigger>
														<SelectContent>
															{books.map((book) => (
																<SelectItem
																	key={book.id}
																	value={book.id.toString()}
																	disabled={book.stockQuantity < 1}
																>
																	{book.title} - {formatCurrency(book.price)}{" "}
																	{book.stockQuantity < 1 && "(Out of Stock)"}
																</SelectItem>
															))}
														</SelectContent>
													</Select>
												)}
											/>
										</div>

										<div className="grid grid-cols-3 gap-2">
											<div className="space-y-2">
												<Label htmlFor="quantity">Quantity</Label>
												<Input
													id="quantity"
													type="number"
													min="1"
													{...register("quantity")}
												/>
											</div>
											<div className="space-y-2">
												<Label htmlFor="discount">Discount (%)</Label>
												<Input
													id="discount"
													type="number"
													min="0"
													max="100"
													{...register("discount")}
												/>
											</div>
											<div className="space-y-2">
												<Label>&nbsp;</Label>
												<Button
													onClick={async () => {
														const ok = await trigger([
															"selectedBook",
															"quantity",
															"discount",
														]);
														if (ok) addItemToBill();
													}}
													className="w-full"
												>
													<Plus className="mr-2 h-4 w-4" />
													Add
												</Button>
											</div>
										</div>
									</div>
								</div>
							</CardContent>
						</Card>

						{/* Bill Summary */}
						<Card>
							<CardHeader>
								<CardTitle>Bill Summary</CardTitle>
								<CardDescription>Invoice: {invoiceNumber}</CardDescription>
							</CardHeader>
							<CardContent>
								{getValues("selectedCustomer") && (
									<div className="mb-4 p-3 bg-muted rounded-lg">
										<p className="font-medium">
											Customer:{" "}
											{
												customers.find(
													(c) =>
														c.id.toString() === getValues("selectedCustomer")
												)?.name
											}
										</p>
										<p className="text-sm text-muted-foreground">
											Account:{" "}
											{
												customers.find(
													(c) =>
														c.id.toString() === getValues("selectedCustomer")
												)?.accountNumber
											}
										</p>
									</div>
								)}

								<div className="space-y-4">
									<div className="space-y-2">
										<div className="flex justify-between text-sm">
											<span>Subtotal:</span>
											<span>{formatCurrency(subtotal)}</span>
										</div>
										<div className="flex justify-between text-sm">
											<span>Discount:</span>
											<span>- {formatCurrency(totalDiscount)}</span>
										</div>
										<div className="flex justify-between text-sm items-center">
											<div className="flex items-center gap-2">
												<span>Tax (5%):</span>
												<input
													type="checkbox"
													id="applyTax"
													{...register("applyTax")}
													className="rounded border-gray-300"
												/>
												<Label htmlFor="applyTax" className="text-xs">
													Apply Tax
												</Label>
											</div>
											<span>{formatCurrency(tax)}</span>
										</div>
										<Separator />
										<div className="flex justify-between font-medium">
											<span>Total:</span>
											<span>{formatCurrency(grandTotal)}</span>
										</div>
									</div>

									<div className="flex gap-2">
										<Button
											onClick={handleSubmit(handleSaveBill)}
											className="flex-1"
											disabled={isSaving || billItems.length === 0}
										>
											{isSaving ? (
												<>
													<Loader2 className="mr-2 h-4 w-4 animate-spin" />
													Saving...
												</>
											) : (
												<>
													<Save className="mr-2 h-4 w-4" />
													Save Bill
												</>
											)}
										</Button>
										<Button
											onClick={handleSubmit(handlePrintBill)}
											variant="outline"
											className="flex-1"
											disabled={isPrinting || billItems.length === 0}
										>
											{isPrinting ? (
												<>
													<Loader2 className="mr-2 h-4 w-4 animate-spin" />
													Preparing...
												</>
											) : (
												<>
													<PrinterIcon className="mr-2 h-4 w-4" />
													Print
												</>
											)}
										</Button>
									</div>
								</div>
							</CardContent>
						</Card>
					</div>

					{/* Bill Items */}
					{billItems.length > 0 && (
						<Card>
							<CardHeader>
								<CardTitle>Bill Items</CardTitle>
								<CardDescription>Items added to current bill</CardDescription>
							</CardHeader>
							<CardContent>
								<Table>
									<TableHeader>
										<TableRow>
											<TableHead>Item</TableHead>
											<TableHead>Price</TableHead>
											<TableHead>Qty</TableHead>
											<TableHead>Discount</TableHead>
											<TableHead>Total</TableHead>
											<TableHead>Actions</TableHead>
										</TableRow>
									</TableHeader>
									<TableBody>
										{billItems.map((item) => (
											<TableRow key={item.id}>
												<TableCell className="font-medium">
													{item.title}
												</TableCell>
												<TableCell>{formatCurrency(item.price)}</TableCell>
												<TableCell>{item.quantity}</TableCell>
												<TableCell>{item.discount}%</TableCell>
												<TableCell>{formatCurrency(item.total)}</TableCell>
												<TableCell>
													<Button
														variant="outline"
														size="sm"
														onClick={() => removeItem(item.id)}
													>
														<Trash2 className="h-4 w-4" />
													</Button>
												</TableCell>
											</TableRow>
										))}
									</TableBody>
								</Table>
							</CardContent>
						</Card>
					)}

					{/* Print Dialog */}
					<PrintBillDialog
						open={printDialogOpen}
						onOpenChange={setPrintDialogOpen}
						printContent={printContent}
						isPrinting={isPrinting}
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

export default BillingSystem;

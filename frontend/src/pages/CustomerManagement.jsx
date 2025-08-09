import React, { useState, useEffect } from "react";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
	AlertDialog,
	AlertDialogAction,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetFooter,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Search, Plus, Edit, Trash2, Loader2, Phone, Mail } from "lucide-react";
import customerAPI from "@/services/customerAPI";
import { showToast } from "@/lib/toast";

export default function CustomerManagement() {
	const [customers, setCustomers] = useState([]);
	const [filteredCustomers, setFilteredCustomers] = useState([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [isSheetOpen, setIsSheetOpen] = useState(false);
	const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
	const [selectedCustomer, setSelectedCustomer] = useState(null);
	const [isLoading, setIsLoading] = useState(false);
	const [isSaving, setIsSaving] = useState(false);
	const [isDeleting, setIsDeleting] = useState(false);

	const [formData, setFormData] = useState({
		accountNumber: "",
		name: "",
		address: "",
		telephone: "",
		email: "",
		registrationDate: new Date().toISOString().split("T")[0],
	});

	const [formErrors, setFormErrors] = useState({});

	useEffect(() => {
		fetchCustomers();
	}, []);

	useEffect(() => {
		if (searchTerm.trim() === "") {
			setFilteredCustomers(customers);
		} else {
			const filtered = customers.filter(
				(customer) =>
					customer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
					customer.accountNumber.toString().includes(searchTerm) ||
					customer.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
					customer.telephone.includes(searchTerm)
			);
			setFilteredCustomers(filtered);
		}
	}, [searchTerm, customers]);

	const fetchCustomers = async () => {
		setIsLoading(true);
		try {
			const response = await customerAPI.getAllCustomers();
			setCustomers(response);
			setFilteredCustomers(response);
		} catch (error) {
			console.error("Error fetching customers:", error);
			showToast.error("Failed to load customers");
		} finally {
			setIsLoading(false);
		}
	};

	const handleSearchChange = (e) => {
		setSearchTerm(e.target.value);
	};

	const handleInputChange = (e) => {
		const { name, value } = e.target;
		setFormData({
			...formData,
			[name]: value,
		});

		if (formErrors[name]) {
			setFormErrors({
				...formErrors,
				[name]: "",
			});
		}
	};

	const validateForm = () => {
		const errors = {};
		if (!formData.accountNumber)
			errors.accountNumber = "Account number is required";
		if (!formData.name) errors.name = "Name is required";
		if (!formData.address) errors.address = "Address is required";
		if (!formData.telephone) errors.telephone = "Telephone is required";
		if (formData.email && !/\S+@\S+\.\S+/.test(formData.email))
			errors.email = "Invalid email format";
		if (!formData.registrationDate)
			errors.registrationDate = "Registration date is required";

		setFormErrors(errors);
		return Object.keys(errors).length === 0;
	};

	const openAddSheet = () => {
		setSelectedCustomer(null);
		setFormData({
			accountNumber: "",
			name: "",
			address: "",
			telephone: "",
			email: "",
			registrationDate: new Date().toISOString().split("T")[0],
		});
		setFormErrors({});
		setIsSheetOpen(true);
	};

	const openEditSheet = (customer) => {
		setSelectedCustomer(customer);
		setFormData({
			accountNumber: customer.accountNumber,
			name: customer.name,
			address: customer.address,
			telephone: customer.telephone,
			email: customer.email || "",
			registrationDate: customer.registrationDate
				? new Date(customer.registrationDate).toISOString().split("T")[0]
				: new Date().toISOString().split("T")[0],
		});
		setFormErrors({});
		setIsSheetOpen(true);
	};

	const openDeleteDialog = (customer) => {
		setSelectedCustomer(customer);
		setIsDeleteDialogOpen(true);
	};

	const handleSave = async () => {
		if (!validateForm()) return;

		setIsSaving(true);
		try {
			if (selectedCustomer) {
				const updatedCustomer = {
					...selectedCustomer,
					...formData,
				};
				await customerAPI.updateCustomer(selectedCustomer.id, updatedCustomer);
				showToast.success("Customer updated successfully");
			} else {
				await customerAPI.createCustomer(formData);
				showToast.success("Customer created successfully");
			}

			fetchCustomers();
			setIsSheetOpen(false);
		} catch (error) {
			console.error("Error saving customer:", error);
			showToast.error(
				selectedCustomer
					? "Failed to update customer"
					: "Failed to create customer"
			);
		} finally {
			setIsSaving(false);
		}
	};

	const handleDelete = async () => {
		if (!selectedCustomer) return;

		setIsDeleting(true);
		try {
			await customerAPI.deleteCustomer(selectedCustomer.id);
			showToast.success("Customer deleted successfully");

			// Refresh customer list
			fetchCustomers();
			setIsDeleteDialogOpen(false);
		} catch (error) {
			console.error("Error deleting customer:", error);
			showToast.error("Failed to delete customer");
		} finally {
			setIsDeleting(false);
		}
	};

	return (
		<div className="flex flex-col space-y-6 h-full">
			<div className="flex justify-between items-center mb-6">
				<div>
					<h1 className="text-3xl font-bold tracking-tight">
						Customer Management
					</h1>
					<p className="text-muted-foreground">
						Manage customer accounts and information
					</p>
				</div>
				<Button onClick={openAddSheet} className="flex items-center gap-1">
					<Plus className="h-4 w-4" /> Add Customer
				</Button>
			</div>

			{isLoading ? (
				<div className="flex items-center justify-center h-64">
					<Loader2 className="h-8 w-8 animate-spin text-primary" />
					<span className="ml-2">Loading customers...</span>
				</div>
			) : (
				<Card>
					<CardHeader>
						<CardTitle>Customer List</CardTitle>
						<CardDescription>
							View and manage all customer accounts
						</CardDescription>
						<div className="flex items-center space-x-2 mt-4">
							<Search className="h-4 w-4 text-muted-foreground" />
							<Input
								placeholder="Search customers..."
								value={searchTerm}
								onChange={handleSearchChange}
								className="max-w-sm"
							/>
						</div>
					</CardHeader>
					<CardContent>
						{filteredCustomers.length > 0 ? (
							<Table>
								<TableHeader>
									<TableRow>
										<TableHead>Account Number</TableHead>
										<TableHead>Name</TableHead>
										<TableHead>Contact</TableHead>
										<TableHead>Address</TableHead>
										<TableHead>Registration Date</TableHead>
										<TableHead>Actions</TableHead>
									</TableRow>
								</TableHeader>
								<TableBody>
									{filteredCustomers.map((customer) => (
										<TableRow key={customer.id}>
											<TableCell className="font-medium">
												{customer.accountNumber}
											</TableCell>
											<TableCell>{customer.name}</TableCell>
											<TableCell>
												<div className="space-y-1">
													<div className="flex items-center gap-1 text-sm">
														<Phone className="h-3 w-3" />
														{customer.telephone}
													</div>
													{customer.email && (
														<div className="flex items-center gap-1 text-sm text-muted-foreground">
															<Mail className="h-3 w-3" />
															{customer.email}
														</div>
													)}
												</div>
											</TableCell>
											<TableCell className="max-w-[200px] truncate">
												{customer.address}
											</TableCell>
											<TableCell>
												{customer.registrationDate
													? new Date(
															customer.registrationDate
													  ).toLocaleDateString()
													: "N/A"}
											</TableCell>
											<TableCell>
												<div className="flex items-center gap-2">
													<Button
														variant="outline"
														size="sm"
														onClick={() => openEditSheet(customer)}
													>
														<Edit className="h-4 w-4" />
													</Button>
													<Button
														variant="outline"
														size="sm"
														onClick={() => openDeleteDialog(customer)}
													>
														<Trash2 className="h-4 w-4" />
													</Button>
												</div>
											</TableCell>
										</TableRow>
									))}
								</TableBody>
							</Table>
						) : (
							<div className="flex justify-center items-center h-32 bg-gray-50 rounded-lg">
								<p className="text-gray-500">
									{searchTerm
										? "No customers match your search"
										: "No customers found. Add one to get started."}
								</p>
							</div>
						)}
					</CardContent>
				</Card>
			)}

			{/* Add/Edit Customer Sheet */}
			<Sheet open={isSheetOpen} onOpenChange={setIsSheetOpen}>
				<SheetContent className="sm:max-w-md">
					<SheetHeader>
						<SheetTitle>
							{selectedCustomer ? "Edit Customer" : "Add New Customer"}
						</SheetTitle>
						<SheetDescription>
							{selectedCustomer
								? "Update customer information in the form below"
								: "Fill in the details to create a new customer"}
						</SheetDescription>
					</SheetHeader>
					<div className="py-6 px-4 space-y-4">
						<div className="space-y-2">
							<label className="text-sm font-medium">Account Number</label>
							<Input
								name="accountNumber"
								value={formData.accountNumber}
								onChange={handleInputChange}
								placeholder="Enter account number"
								className={formErrors.accountNumber ? "border-red-500" : ""}
								disabled={selectedCustomer}
							/>
							{formErrors.accountNumber && (
								<p className="text-red-500 text-xs mt-1">
									{formErrors.accountNumber}
								</p>
							)}
						</div>

						<div className="space-y-2">
							<label className="text-sm font-medium">Name</label>
							<Input
								name="name"
								value={formData.name}
								onChange={handleInputChange}
								placeholder="Enter customer name"
								className={formErrors.name ? "border-red-500" : ""}
							/>
							{formErrors.name && (
								<p className="text-red-500 text-xs mt-1">{formErrors.name}</p>
							)}
						</div>

						<div className="space-y-2">
							<label className="text-sm font-medium">Address</label>
							<Input
								name="address"
								value={formData.address}
								onChange={handleInputChange}
								placeholder="Enter address"
								className={formErrors.address ? "border-red-500" : ""}
							/>
							{formErrors.address && (
								<p className="text-red-500 text-xs mt-1">
									{formErrors.address}
								</p>
							)}
						</div>

						<div className="space-y-2">
							<label className="text-sm font-medium">Telephone</label>
							<Input
								name="telephone"
								value={formData.telephone}
								onChange={handleInputChange}
								placeholder="Enter telephone number"
								className={formErrors.telephone ? "border-red-500" : ""}
							/>
							{formErrors.telephone && (
								<p className="text-red-500 text-xs mt-1">
									{formErrors.telephone}
								</p>
							)}
						</div>

						<div className="space-y-2">
							<label className="text-sm font-medium">Email (Optional)</label>
							<Input
								name="email"
								value={formData.email}
								onChange={handleInputChange}
								placeholder="Enter email address"
								className={formErrors.email ? "border-red-500" : ""}
							/>
							{formErrors.email && (
								<p className="text-red-500 text-xs mt-1">{formErrors.email}</p>
							)}
						</div>

						<div className="space-y-2">
							<label className="text-sm font-medium">Registration Date</label>
							<Input
								type="date"
								name="registrationDate"
								value={formData.registrationDate}
								onChange={handleInputChange}
								className={formErrors.registrationDate ? "border-red-500" : ""}
							/>
							{formErrors.registrationDate && (
								<p className="text-red-500 text-xs mt-1">
									{formErrors.registrationDate}
								</p>
							)}
						</div>
					</div>
					<SheetFooter className="sm:justify-end">
						<Button
							variant="outline"
							onClick={() => setIsSheetOpen(false)}
							disabled={isSaving}
						>
							Cancel
						</Button>
						<Button onClick={handleSave} disabled={isSaving}>
							{isSaving ? (
								<>
									<Loader2 className="h-4 w-4 mr-2 animate-spin" />
									Saving...
								</>
							) : (
								"Save Customer"
							)}
						</Button>
					</SheetFooter>
				</SheetContent>
			</Sheet>

			{/* Delete Confirmation Dialog */}
			<AlertDialog
				open={isDeleteDialogOpen}
				onOpenChange={setIsDeleteDialogOpen}
			>
				<AlertDialogContent>
					<AlertDialogHeader>
						<AlertDialogTitle>Are you sure?</AlertDialogTitle>
						<AlertDialogDescription>
							This will permanently delete the customer{" "}
							<span className="font-semibold">{selectedCustomer?.name}</span>.
							This action cannot be undone.
						</AlertDialogDescription>
					</AlertDialogHeader>
					<AlertDialogFooter>
						<AlertDialogCancel disabled={isDeleting}>Cancel</AlertDialogCancel>
						<AlertDialogAction
							className="bg-destructive hover:bg-destructive/90"
							onClick={handleDelete}
							disabled={isDeleting}
						>
							{isDeleting ? (
								<>
									<Loader2 className="h-4 w-4 mr-2 animate-spin" />
									Deleting...
								</>
							) : (
								"Delete"
							)}
						</AlertDialogAction>
					</AlertDialogFooter>
				</AlertDialogContent>
			</AlertDialog>
		</div>
	);
}

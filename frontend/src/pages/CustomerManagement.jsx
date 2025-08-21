import React, { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
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
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Search, Plus, Edit, Trash2, Loader2, Phone, Mail } from "lucide-react";
import customerAPI from "@/services/customerAPI";
import { showToast } from "@/lib/toast";

export default function CustomerManagement() {
	const [customers, setCustomers] = useState([]);
	const [filteredCustomers, setFilteredCustomers] = useState([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
	const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
	const [selectedCustomer, setSelectedCustomer] = useState(null);
	const [isLoading, setIsLoading] = useState(false);
	const [isSaving, setIsSaving] = useState(false);
	const [isDeleting, setIsDeleting] = useState(false);

	// Zod schema + react-hook-form
	const customerSchema = z.object({
		accountNumber: z.string().min(1, "Account number is required"),
		name: z.string().min(1, "Name is required"),
		address: z.string().min(1, "Address is required"),
		telephone: z.string().min(1, "Telephone is required"),
		email: z
			.string()
			.email("Invalid email format")
			.optional()
			.or(z.literal("")),
		registrationDate: z.string().min(1, "Registration date is required"),
	});

	const today = new Date().toISOString().split("T")[0];

	const {
		register,
		handleSubmit,
		reset,
		formState: { errors },
	} = useForm({
		resolver: zodResolver(customerSchema),
		mode: "onChange",
		defaultValues: {
			accountNumber: "",
			name: "",
			address: "",
			telephone: "",
			email: "",
			registrationDate: today,
		},
	});

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

	// manual input handlers & validation removed - using react-hook-form + zod

	const openAddDialog = () => {
		setSelectedCustomer(null);
		reset({
			accountNumber: "",
			name: "",
			address: "",
			telephone: "",
			email: "",
			registrationDate: today,
		});
		setIsAddDialogOpen(true);
	};

	const openEditDialog = (customer) => {
		setSelectedCustomer(customer);
		reset({
			accountNumber: customer.accountNumber,
			name: customer.name,
			address: customer.address,
			telephone: customer.telephone,
			email: customer.email || "",
			registrationDate: customer.registrationDate
				? new Date(customer.registrationDate).toISOString().split("T")[0]
				: today,
		});
		setIsAddDialogOpen(true);
	};

	const openDeleteDialog = (customer) => {
		setSelectedCustomer(customer);
		setIsDeleteDialogOpen(true);
	};

	const onSubmit = async (data) => {
		setIsSaving(true);
		try {
			if (selectedCustomer) {
				const updatedCustomer = {
					...selectedCustomer,
					...data,
				};
				await customerAPI.updateCustomer(selectedCustomer.id, updatedCustomer);
				showToast.success("Customer updated successfully");
			} else {
				await customerAPI.createCustomer(data);
				showToast.success("Customer created successfully");
			}

			fetchCustomers();
			setIsAddDialogOpen(false);
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
				<Button onClick={openAddDialog} className="flex items-center gap-1">
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
														onClick={() => openEditDialog(customer)}
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

			{/* Add/Edit Customer Dialog */}
			<Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
				<DialogContent className="sm:max-w-[550px]">
					<DialogHeader>
						<DialogTitle>
							{selectedCustomer ? "Edit Customer" : "Add New Customer"}
						</DialogTitle>
						<DialogDescription>
							{selectedCustomer
								? "Update customer information in the form below"
								: "Fill in the details to create a new customer"}
						</DialogDescription>
					</DialogHeader>
					<div className="grid gap-4 py-4">
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="accountNumber">Account Number</Label>
							<Input
								id="accountNumber"
								{...register("accountNumber")}
								placeholder="Enter account number"
								className="col-span-3"
								disabled={!!selectedCustomer}
							/>
							{errors.accountNumber && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.accountNumber.message}
								</p>
							)}
						</div>

						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="name">Name</Label>
							<Input
								id="name"
								{...register("name")}
								placeholder="Enter customer name"
								className="col-span-3"
							/>
							{errors.name && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.name.message}
								</p>
							)}
						</div>

						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="address">Address</Label>
							<Input
								id="address"
								{...register("address")}
								placeholder="Enter address"
								className="col-span-3"
							/>
							{errors.address && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.address.message}
								</p>
							)}
						</div>

						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="telephone">Telephone</Label>
							<Input
								id="telephone"
								{...register("telephone")}
								placeholder="Enter telephone number"
								className="col-span-3"
							/>
							{errors.telephone && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.telephone.message}
								</p>
							)}
						</div>

						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="email">Email (Optional)</Label>
							<Input
								id="email"
								{...register("email")}
								placeholder="Enter email address"
								className="col-span-3"
							/>
							{errors.email && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.email.message}
								</p>
							)}
						</div>

						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="registrationDate">Registration Date</Label>
							<Input
								id="registrationDate"
								type="date"
								{...register("registrationDate")}
								className="col-span-3"
							/>
							{errors.registrationDate && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.registrationDate.message}
								</p>
							)}
						</div>
					</div>
					<DialogFooter>
						<Button
							variant="outline"
							onClick={() => setIsAddDialogOpen(false)}
							disabled={isSaving}
						>
							Cancel
						</Button>
						<Button onClick={handleSubmit(onSubmit)} disabled={isSaving}>
							{isSaving ? (
								<>
									<Loader2 className="h-4 w-4 mr-2 animate-spin" />
									Saving...
								</>
							) : (
								"Save Customer"
							)}
						</Button>
					</DialogFooter>
				</DialogContent>
			</Dialog>

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

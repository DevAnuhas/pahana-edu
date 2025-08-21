import React, { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from "@/components/ui/dialog";
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
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Search, Plus, Edit, Trash2, BookText, Loader2 } from "lucide-react";
import bookAPI from "@/services/bookAPI";
import { showToast } from "@/lib/toast";

const ItemManagement = () => {
	const [books, setBooks] = useState([]);
	const [filteredBooks, setFilteredBooks] = useState([]);
	const [categories, setCategories] = useState([]);
	const [publishers, setPublishers] = useState([]);
	const [searchTerm, setSearchTerm] = useState("");
	const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
	const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
	const [selectedBook, setSelectedBook] = useState(null);
	const [isLoading, setIsLoading] = useState(false);
	const [isSaving, setIsSaving] = useState(false);
	const [isDeleting, setIsDeleting] = useState(false);
	const [loadingErrors, setLoadingErrors] = useState({
		books: false,
		categories: false,
		publishers: false,
	});
	const [retryCount, setRetryCount] = useState(0);

	const bookSchema = z.object({
		isbn: z.string().regex(/^\d{13}$/, "ISBN must be 13 digits"),
		title: z.string().min(1, "Title is required"),
		author: z.string().min(1, "Author is required"),
		categoryId: z.string().min(1, "Category is required"),
		publisherId: z.string().min(1, "Publisher is required"),
		price: z.preprocess(
			(val) => (val === "" ? undefined : Number(val)),
			z.number({ invalid_type_error: "Price must be a number" })
		),
		stockQuantity: z.preprocess(
			(val) => (val === "" ? undefined : Number(val)),
			z.number({ invalid_type_error: "Stock quantity must be a number" }).int()
		),
		publicationYear: z.preprocess(
			(val) => (val === "" ? undefined : Number(val)),
			z.number().int().optional()
		),
		description: z.string().optional(),
	});

	const {
		register,
		control,
		handleSubmit,
		reset,
		formState: { errors },
	} = useForm({
		resolver: zodResolver(bookSchema),
		mode: "onChange",
		defaultValues: {
			isbn: "",
			title: "",
			author: "",
			categoryId: "",
			publisherId: "",
			publicationYear: "",
			price: "",
			stockQuantity: "",
			description: "",
		},
	});

	useEffect(() => {
		fetchData();
	}, [retryCount]);

	useEffect(() => {
		if (searchTerm.trim() === "") {
			setFilteredBooks(books);
		} else {
			const filtered = books.filter(
				(book) =>
					book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
					book.author.toLowerCase().includes(searchTerm.toLowerCase()) ||
					book.isbn.includes(searchTerm) ||
					(book.categoryName &&
						book.categoryName.toLowerCase().includes(searchTerm.toLowerCase()))
			);
			setFilteredBooks(filtered);
		}
	}, [searchTerm, books]);

	const fetchData = async () => {
		setIsLoading(true);
		setLoadingErrors({
			books: false,
			categories: false,
			publishers: false,
		});

		try {
			const booksResponse = await bookAPI.getAllBooks();
			setBooks(booksResponse);
			setFilteredBooks(booksResponse);
		} catch (error) {
			console.error("Error fetching books:", error);
			setLoadingErrors((prev) => ({ ...prev, books: true }));
			showToast.error("Failed to load books data");
		}

		try {
			const categoriesResponse = await bookAPI.getAllCategories();
			setCategories(categoriesResponse);
		} catch (error) {
			console.error("Error fetching categories:", error);
			setLoadingErrors((prev) => ({ ...prev, categories: true }));
			showToast.error("Failed to load categories data");
		}

		try {
			const publishersResponse = await bookAPI.getAllPublishers();
			setPublishers(publishersResponse);
		} catch (error) {
			console.error("Error fetching publishers:", error);
			setLoadingErrors((prev) => ({ ...prev, publishers: true }));
			showToast.error("Failed to load publishers data");
		}

		setIsLoading(false);
	};

	const handleSearchChange = (e) => {
		setSearchTerm(e.target.value);
	};

	const handleRetry = () => {
		setRetryCount((prev) => prev + 1);
	};

	// removed manual form state and validation - using react-hook-form + zod instead

	const openAddDialog = () => {
		setSelectedBook(null);
		reset({
			isbn: "",
			title: "",
			author: "",
			categoryId: "",
			publisherId: "",
			publicationYear: "",
			price: "",
			stockQuantity: "",
			description: "",
		});
		setIsAddDialogOpen(true);
	};

	const openEditDialog = (book) => {
		setSelectedBook(book);
		// reset form values from the selected book (convert ids to strings for Select)
		reset({
			isbn: book.isbn || "",
			title: book.title || "",
			author: book.author || "",
			categoryId: book.categoryId ? String(book.categoryId) : "",
			publisherId: book.publisherId ? String(book.publisherId) : "",
			publicationYear: book.publicationYear ? String(book.publicationYear) : "",
			price:
				book.price !== undefined && book.price !== null
					? String(book.price)
					: "",
			stockQuantity:
				book.stockQuantity !== undefined && book.stockQuantity !== null
					? String(book.stockQuantity)
					: "",
			description: book.description || "",
		});
		setIsAddDialogOpen(true);
	};

	const openDeleteDialog = (book) => {
		setSelectedBook(book);
		setIsDeleteDialogOpen(true);
	};

	const onSubmit = async (data) => {
		// data is validated and preprocessed by zod/resolver
		const bookData = {
			...data,
			publicationYear: data.publicationYear ?? null,
		};

		setIsSaving(true);
		try {
			if (selectedBook) {
				await bookAPI.updateBook(selectedBook.id, bookData);
				showToast.success("Book updated successfully");
			} else {
				await bookAPI.createBook(bookData);
				showToast.success("Book created successfully");
			}

			fetchData();
			setIsAddDialogOpen(false);
		} catch (error) {
			console.error("Error saving book:", error);
			showToast.error(
				selectedBook ? "Failed to update book" : "Failed to create book"
			);
		} finally {
			setIsSaving(false);
		}
	};

	const handleDelete = async () => {
		if (!selectedBook) return;

		setIsDeleting(true);
		try {
			await bookAPI.deleteBook(selectedBook.id);
			showToast.success("Book deleted successfully");

			fetchData();
			setIsDeleteDialogOpen(false);
		} catch (error) {
			console.error("Error deleting book:", error);
			showToast.error("Failed to delete book");
		} finally {
			setIsDeleting(false);
		}
	};

	const getStockStatus = (stock) => {
		if (stock === 0) return { label: "Out of Stock", variant: "destructive" };
		if (stock < 10) return { label: "Low Stock", variant: "secondary" };
		return { label: "In Stock", variant: "default" };
	};

	return (
		<div className="space-y-6">
			<div className="flex items-center justify-between">
				<div>
					<h1 className="text-3xl font-bold tracking-tight">Item Management</h1>
					<p className="text-muted-foreground">
						Manage books and inventory in the bookshop
					</p>
				</div>
				<Button onClick={openAddDialog} className="flex items-center gap-1">
					<Plus className="h-4 w-4" /> Add Book
				</Button>
			</div>

			{isLoading ? (
				<div className="flex flex-col items-center justify-center h-64">
					<Loader2 className="h-8 w-8 animate-spin text-primary" />
					<span className="ml-2 mt-4">Loading books...</span>
				</div>
			) : books.length === 0 &&
			  Object.values(loadingErrors).some((error) => error) ? (
				<div className="flex flex-col items-center justify-center h-64 gap-4">
					<div className="text-destructive">Failed to load some data</div>
					<Button
						onClick={handleRetry}
						variant="outline"
						className="flex items-center gap-2"
					>
						<Loader2 className="h-4 w-4" />
						Retry Loading Data
					</Button>
				</div>
			) : (
				<>
					<div className="grid gap-4 md:grid-cols-4">
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Total Books
								</CardTitle>
								<BookText className="h-4 w-4 text-muted-foreground" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">{books.length}</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">In Stock</CardTitle>
								<BookText className="h-4 w-4 text-green-600" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{books.filter((b) => b.stockQuantity > 10).length}
								</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">Low Stock</CardTitle>
								<BookText className="h-4 w-4 text-yellow-600" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{
										books.filter(
											(b) => b.stockQuantity > 0 && b.stockQuantity <= 10
										).length
									}
								</div>
							</CardContent>
						</Card>
						<Card>
							<CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
								<CardTitle className="text-sm font-medium">
									Out of Stock
								</CardTitle>
								<BookText className="h-4 w-4 text-red-600" />
							</CardHeader>
							<CardContent>
								<div className="text-2xl font-bold">
									{books.filter((b) => b.stockQuantity === 0).length}
								</div>
							</CardContent>
						</Card>
					</div>

					<Card>
						<CardHeader>
							<CardTitle>Book Inventory</CardTitle>
							<CardDescription>
								View and manage all books in inventory
							</CardDescription>
							<div className="flex items-center space-x-2 mt-4">
								<Search className="h-4 w-4 text-muted-foreground" />
								<Input
									placeholder="Search books..."
									value={searchTerm}
									onChange={handleSearchChange}
									className="max-w-sm"
								/>
							</div>
						</CardHeader>
						<CardContent>
							{filteredBooks.length > 0 ? (
								<Table>
									<TableHeader>
										<TableRow>
											<TableHead>ISBN</TableHead>
											<TableHead>Title</TableHead>
											<TableHead>Author</TableHead>
											<TableHead>Category</TableHead>
											<TableHead>Price (Rs.)</TableHead>
											<TableHead>Stock</TableHead>
											<TableHead>Status</TableHead>
											<TableHead>Actions</TableHead>
										</TableRow>
									</TableHeader>
									<TableBody>
										{filteredBooks.map((book) => {
											const stockStatus = getStockStatus(book.stockQuantity);
											return (
												<TableRow key={book.id}>
													<TableCell className="font-mono text-sm">
														{book.isbn}
													</TableCell>
													<TableCell className="font-medium">
														{book.title}
													</TableCell>
													<TableCell>{book.author}</TableCell>
													<TableCell>
														<Badge variant="outline">{book.categoryName}</Badge>
													</TableCell>
													<TableCell>
														{parseFloat(book.price).toFixed(2)}
													</TableCell>
													<TableCell>{book.stockQuantity}</TableCell>
													<TableCell>
														<Badge variant={stockStatus.variant}>
															{stockStatus.label}
														</Badge>
													</TableCell>
													<TableCell>
														<div className="flex items-center gap-2">
															<Button
																variant="outline"
																size="sm"
																onClick={() => openEditDialog(book)}
															>
																<Edit className="h-4 w-4" />
															</Button>
															<Button
																variant="outline"
																size="sm"
																onClick={() => openDeleteDialog(book)}
															>
																<Trash2 className="h-4 w-4" />
															</Button>
														</div>
													</TableCell>
												</TableRow>
											);
										})}
									</TableBody>
								</Table>
							) : (
								<div className="flex justify-center items-center h-32 bg-gray-50 rounded-lg">
									<p className="text-gray-500">
										{searchTerm
											? "No books match your search"
											: "No books found. Add one to get started."}
									</p>
								</div>
							)}
						</CardContent>
					</Card>
				</>
			)}

			{/* Add/Edit Book Dialog */}
			<Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
				<DialogContent className="sm:max-w-[550px]">
					<DialogHeader>
						<DialogTitle>
							{selectedBook ? "Edit Book" : "Add New Book"}
						</DialogTitle>
						<DialogDescription>
							{selectedBook
								? "Update book information in the form below"
								: "Enter book details to add to inventory"}
						</DialogDescription>
					</DialogHeader>
					<div className="grid gap-4 py-4">
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="isbn" className="text-right">
								ISBN
							</Label>
							<Input
								id="isbn"
								{...register("isbn")}
								placeholder="9780123456789"
								className="col-span-3"
								disabled={selectedBook}
							/>
							{errors.isbn && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.isbn.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="title" className="text-right">
								Title
							</Label>
							<Input
								id="title"
								{...register("title")}
								placeholder="Book Title"
								className="col-span-3"
							/>
							{errors.title && (
								<p className="text-red-500 text-xs col-span-3 col-start-2">
									{errors.title.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="author" className="text-right">
								Author
							</Label>
							<Input
								id="author"
								{...register("author")}
								placeholder="Author Name"
								className="col-span-3"
							/>
							{errors.author && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.author.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="category" className="text-right">
								Category
							</Label>
							<div className="col-span-3">
								<Controller
									control={control}
									name="categoryId"
									render={({ field }) => (
										<Select value={field.value} onValueChange={field.onChange}>
											<SelectTrigger>
												<SelectValue placeholder="Select category" />
											</SelectTrigger>
											<SelectContent>
												{categories.map((category) => (
													<SelectItem
														key={category.id}
														value={category.id.toString()}
													>
														{category.name}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
									)}
								/>
								{errors.categoryId && (
									<p className="text-red-500 text-xs mt-1">
										{errors.categoryId.message}
									</p>
								)}
							</div>
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="publisher" className="text-right">
								Publisher
							</Label>
							<div className="col-span-3">
								<Controller
									control={control}
									name="publisherId"
									render={({ field }) => (
										<Select value={field.value} onValueChange={field.onChange}>
											<SelectTrigger>
												<SelectValue placeholder="Select publisher" />
											</SelectTrigger>
											<SelectContent>
												{publishers.map((publisher) => (
													<SelectItem
														key={publisher.id}
														value={publisher.id.toString()}
													>
														{publisher.name}
													</SelectItem>
												))}
											</SelectContent>
										</Select>
									)}
								/>
								{errors.publisherId && (
									<p className="text-red-500 text-xs mt-1">
										{errors.publisherId.message}
									</p>
								)}
							</div>
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="price" className="text-right">
								Price (Rs.)
							</Label>
							<Input
								id="price"
								type="number"
								step="0.01"
								{...register("price")}
								placeholder="2500.00"
								className="col-span-3"
							/>
							{errors.price && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.price.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="stock" className="text-right">
								Stock Qty
							</Label>
							<Input
								id="stock"
								type="number"
								{...register("stockQuantity")}
								placeholder="50"
								className="col-span-3"
							/>
							{errors.stockQuantity && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.stockQuantity.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-center gap-4">
							<Label htmlFor="year" className="text-right">
								Pub. Year
							</Label>
							<Input
								id="year"
								type="number"
								{...register("publicationYear")}
								placeholder="2024"
								className="col-span-3"
							/>
							{errors.publicationYear && (
								<p className="text-red-500 text-xs mt-1 col-span-3 col-start-2">
									{errors.publicationYear.message}
								</p>
							)}
						</div>
						<div className="grid grid-cols-4 items-start gap-4">
							<Label htmlFor="description" className="text-right pt-2">
								Description
							</Label>
							<Textarea
								id="description"
								{...register("description")}
								placeholder="Book description"
								className="col-span-3"
								rows={3}
							/>
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
								"Save Book"
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
							This will permanently delete the book{" "}
							<span className="font-semibold">{selectedBook?.title}</span>. This
							action cannot be undone.
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
};

export default ItemManagement;

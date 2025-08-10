import React from "react";
import { Button } from "@/components/ui/button";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import { PrinterIcon, Loader2 } from "lucide-react";
import { showToast } from "@/lib/toast";
import billingAPI from "@/services/billingAPI";

/**
 * PrintBillDialog - A unified component for printing bills/invoices
 *
 * @param {Object} props - Component props
 * @param {boolean} props.open - Whether the dialog is open
 * @param {Function} props.onOpenChange - Function to call when the open state changes
 * @param {Object} props.bill - The bill/invoice to print (can be either processed or raw invoice data)
 * @param {string} props.printContent - Pre-generated print content (if available)
 * @param {boolean} props.isPrinting - Whether the bill is currently being printed
 * @param {Function} props.onPrint - Function to call when the print button is clicked
 */
const PrintBillDialog = ({
	open,
	onOpenChange,
	bill,
	printContent: initialPrintContent = "",
	isPrinting: externalIsPrinting = false,
	onPrint,
}) => {
	const [printContent, setPrintContent] = React.useState(initialPrintContent);
	const [isPrinting, setIsPrinting] = React.useState(externalIsPrinting);

	React.useEffect(() => {
		if (initialPrintContent) {
			setPrintContent(initialPrintContent);
		}
	}, [initialPrintContent]);

	React.useEffect(() => {
		setIsPrinting(externalIsPrinting);
	}, [externalIsPrinting]);

	const handlePrintBill = async () => {
		if (onPrint) {
			onPrint();
			return;
		}

		setIsPrinting(true);
		try {
			if (!bill || !bill.id) {
				showToast.error("Cannot print: Invalid bill data");
				return;
			}

			if (printContent) {
				showToast.success("Bill sent to printer");
				onOpenChange(false);
				return;
			}

			const response = await billingAPI.printInvoice(bill.id);

			if (response && response.status === "success") {
				setPrintContent(response.printableInvoice);
				showToast.success("Bill sent to printer");

				setTimeout(() => {
					onOpenChange(false);
				}, 800);
			} else {
				showToast.error("Failed to generate printable bill");
			}
		} catch (error) {
			console.error("Error printing bill:", error);
			showToast.error("Failed to print bill");
		} finally {
			setIsPrinting(false);
		}
	};

	const getTitle = () => {
		if (!bill) return "Print Bill";
		return `Print Bill - ${bill.invoiceNumber || "New Invoice"}`;
	};

	return (
		<Dialog open={open} onOpenChange={onOpenChange}>
			<DialogContent className="max-w-[600px]">
				<DialogHeader>
					<DialogTitle>{getTitle()}</DialogTitle>
					<DialogDescription>Preview of printable invoice</DialogDescription>
				</DialogHeader>

				<div className="font-mono text-sm whitespace-pre p-4 bg-gray-50 border h-fit max-h-[600px] overflow-y-auto">
					{printContent}
				</div>

				<div className="flex justify-end mt-4">
					<Button
						className="bg-primary hover:bg-primary/90"
						onClick={handlePrintBill}
						disabled={isPrinting}
					>
						{isPrinting ? (
							<>
								<Loader2 className="h-4 w-4 mr-2 animate-spin" />
								Printing...
							</>
						) : (
							<>
								<PrinterIcon className="h-4 w-4 mr-1" />
								Print
							</>
						)}
					</Button>
				</div>
			</DialogContent>
		</Dialog>
	);
};

export default PrintBillDialog;

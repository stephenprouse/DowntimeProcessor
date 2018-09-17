package us.prouse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class DocumentParser {

	public static void main(String[] args) throws IOException {
		
		String inFilePath = "D:\\MEDITECH\\Downtime\\";
		String outFilePath = "D:\\MEDITECH\\Downtime\\Patients\\";
		
		//String inFilePath = "C:\\MEDITECH\\Downtime\\";
		//String outFilePath = "C:\\MEDITECH\\Downtime\\Patients\\";

		
		File directory = new File(inFilePath);
		File[] fileArray = directory.listFiles();
		
		if (fileArray != null) {
			for (File file : fileArray) {
				if (file.getName().endsWith(".pdf")) {
					System.out.println(file.getAbsolutePath());
					ReadDowntimeFiles(file.getAbsolutePath(), outFilePath);
					file.delete();
				}
			}
			
			File patientDirectory = new File(outFilePath);
			File[] patientFiles = patientDirectory.listFiles();
			
			DeleteOldFiles(7, patientFiles);
		} else {
			System.out.println("Directory is null or empty");
		}		
	}

	static void ReadDowntimeFiles(String inputFile, String outFilePath) throws IOException {
		String endOfRecordFlag = "** END OF RECORD **";
		int patient = 0;
		int startPage = 1;

		PdfReader reader = new PdfReader(inputFile);
		System.out.println("Page count: " + reader.getNumberOfPages());

		for (int page = 1; page <= reader.getNumberOfPages(); page++) {

			String currentPageText = PdfTextExtractor.getTextFromPage(reader, page);
			if (currentPageText.contains(endOfRecordFlag)) {
				// System.out.println(currentPageText);

				String patientAccount = currentPageText.substring(currentPageText.length() - 12,
						currentPageText.length());

				patient++;

				String outputFile = outFilePath + patientAccount
						+ "_SUMMARY.pdf";

				ExtractPages(inputFile, outputFile, startPage, page);

				startPage = page + 1;
			}
		}
		reader.close();
		System.out.println("number of patients: " + String.valueOf(patient));
	}

	static void ExtractPages(String sourcePdfPath, String outputPdfPath, int startPage, int endPage) {
		PdfReader reader = null;
		Document sourceDocument = null;
		PdfCopy pdfCopyProvider = null;
		PdfImportedPage importedPage = null;

		try {
			// Intialize a new PdfReader instance with the contents of the
			// source Pdf file:
			reader = new PdfReader(sourcePdfPath);

			// For simplicity, I am assuming all the pages share the same size
			// and rotation as the first page:
			sourceDocument = new Document(reader.getPageSizeWithRotation(startPage));

			// Initialize an instance of the PdfCopyClass with the source
			// document and an output file stream:
			pdfCopyProvider = new PdfCopy(sourceDocument, new FileOutputStream(outputPdfPath));
			pdfCopyProvider.setFullCompression();

			sourceDocument.open();
			
			// Walk the specified range and add the page copies to the output
			// file:
			for (int i = startPage; i <= endPage; i++) {
				importedPage = pdfCopyProvider.getImportedPage(reader, i);
				pdfCopyProvider.addPage(importedPage);
			}

			sourceDocument.close();
			reader.close();
			pdfCopyProvider.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	static void DeleteOldFiles(int days, File[] fileArray) {

		for (File file : fileArray) {
			long diff = new Date().getTime() - file.lastModified();
			if (file.getName().contains(".pdf")) {
				if (TimeUnit.MILLISECONDS.toDays(diff) >= days) {
					file.delete();
					System.out.println("Name: " + file.getName() + " DELETED");
				}
				if (file.getName().contains("_SUMMARY.pdf") && TimeUnit.MILLISECONDS.toDays(diff) >= 1) {
					file.delete();
					System.out.println("Name: " + file.getName() + " DELETED");
				}	
			}
		}
	}
}

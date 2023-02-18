package com.schedule.printing.util;



import java.io.InputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrintQuality;


public class Printing {
	

	public void setPrinterService(String printerName,InputStream dataPrint) {			

        try {
        	PrintService myPrintService = findPrintService(printerName); //"My  Windowsprinter Name"
        	Doc pdfDoc = new SimpleDoc(dataPrint, DocFlavor.INPUT_STREAM.AUTOSENSE, null);       	
        	final HashPrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
            attrs.add(PrintQuality.HIGH);
            attrs.add(MediaSizeName.ISO_A4);
        	DocPrintJob printJob = myPrintService.createPrintJob();
        	printJob.print(pdfDoc, attrs);
		} catch (PrintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private PrintService findPrintService(String printerName) {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        return null;
    }
}

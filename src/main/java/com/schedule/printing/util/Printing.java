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
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrintQuality;


public class Printing {
	

	public void setPrinterService(String printerName,InputStream dataPrint,String namefile) {			

        try {
        	
        	PrintService myPrintService = findPrintService(printerName); //"My  Windowsprinter Name"
        	Doc pdfDoc = new SimpleDoc(dataPrint, DocFlavor.INPUT_STREAM.AUTOSENSE, null);       	
        	HashPrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        	attrs.add(new JobName(namefile, null));
        	attrs.add(new Copies(1));
            attrs.add(PrintQuality.HIGH);
            attrs.add(MediaSizeName.ISO_A4);
            attrs.add(Chromaticity.MONOCHROME);
        	DocPrintJob printJob = myPrintService.createPrintJob();
        	printJob.print(pdfDoc, attrs);
		} catch (PrintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private PrintService findPrintService(String printerName) {
		DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(flavor, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
        }
        return null;
    }
}

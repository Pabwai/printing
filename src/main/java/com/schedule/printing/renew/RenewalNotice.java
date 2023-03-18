package com.schedule.printing.renew;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.TextField;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import com.schedule.printing.util.AddCertificate;
import com.schedule.printing.util.Printing;

public class RenewalNotice {	
	
	private String fontbase;	//full path font
	private String logoTMSTH;	//full path font
	public void setFilePDF(String data) {	 
		
		try {
			
			    
			JSONObject jsonObject = parseJSONFile(data);
			
			JSONObject object = (JSONObject) jsonObject.get("path");
			String pathfile = object.getString("pathfile");
			String namefile = object.getString("namefile");	
			String printername = object.getString("printername");
			fontbase = object.getString("fontbase");			
			
			String fileout = pathfile + namefile;	
			
			
			JSONArray arr = (JSONArray)jsonObject.get("schedule");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Document document = new Document();	
			document.setPageSize(PageSize.A4);
			PdfCopy copy = new PdfCopy(document, bos);
			document.open();   
			for (int i = 0; i < arr.length(); i++) {
				
				JSONObject jdata  = (JSONObject)arr.get(i);				
				
				JSONArray setting = (JSONArray)jdata.get("setting");

				JSONObject deatil = (JSONObject)jdata.get("detail");
				
				Map<String, String> map  =  new HashMap<String, String>() ;
			    
			    Iterator<String> keysItr = deatil.keys();
			    while(keysItr.hasNext() ) {
			        String key = keysItr.next();
			        String value = deatil.getString(key);
			        logoTMSTH = deatil.getString("img_logoqrcode");
			        map.put(key, value);
			    }
			    
			    setForm(copy,setting,map);
			    
			    
			    map.clear();
			}
		   		  
		    document.close();
		    copy.close();
		    
		    if(!object.getString("printername").equals("")){
		    	Printing printPDF = new Printing();
			    printPDF.setPrinterService(printername,new ByteArrayInputStream(bos.toByteArray()),namefile);
		    }
		    
		    
		    if(object.getString("createpdf").equals("YES")) {
		    	AddCertificate cer = new AddCertificate();

			    if(object.getString("scheduleSet").equals("YES")) {
			    	
			    	File filePDF = new File(fileout);
					if(filePDF.getAbsoluteFile().exists()) {

						if(object.getString("addcer").equals("YES"))cer.AddCert(bos);
						File file = File.createTempFile("temp", ".pdf");
						FileOutputStream fileOutputStream = new FileOutputStream(file);						
						bos.writeTo(fileOutputStream);
						bos.close();
						fileOutputStream.close();					
					    mergePdfs(file.getAbsolutePath(),fileout,pathfile,namefile);										
					}else {
						if(object.getString("addcer").equals("YES"))cer.AddCert(bos);
						FileOutputStream fileOutputStream = new FileOutputStream(fileout);
						bos.writeTo(fileOutputStream);
				    	bos.close();
				    	fileOutputStream.close();
					}
			    }else {
			    	if(object.getString("addcer").equals("YES"))cer.AddCert(bos);
			    	FileOutputStream fileOutputStream = new FileOutputStream(fileout);
			    	bos.writeTo(fileOutputStream);
			    	bos.close();
			    	fileOutputStream.close();
			    }
		    }

		    System.out.println("AddTextOnPDF Complete");	    
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	public void mergePdfs(String tempfile ,String fileout,String pathfile,String namefile ){
        try {
        	 File temp1 = new File(tempfile);
        	 //File temp2 = new File(fileout);
             //File mergePdfs = File.createTempFile("temp", ".pdf",new File("D:\\workspec\\data\\temp"));
        	 File mergePdfs = File.createTempFile("temp", ".pdf");
        	 String[] filemar = {tempfile,fileout};
        	 Document pDFCombineUsingJava = new Document();
             PdfCopy copy = new PdfCopy(pDFCombineUsingJava , new FileOutputStream(mergePdfs));
             pDFCombineUsingJava.open();
             PdfReader ReadInputPDF = null;

             for (int i = 0; i < filemar.length; i++) {
            	 ReadInputPDF = new PdfReader(filemar[i]);
                 copy.addDocument(ReadInputPDF);
                 copy.freeReader(ReadInputPDF);
                 ReadInputPDF.close();           	 
            	 
             }     
             copy.close();
             pDFCombineUsingJava.close();    
             //Thread.sleep(1000);
             //temp1.deleteOnExit();
             //temp2.deleteOnExit();
             if (!mergePdfs.exists()) throw new java.io.IOException("Not found file exists........");
            
             Path to = Paths.get(fileout);
             Path cyp = Paths.get(mergePdfs.getAbsolutePath()); 
             Files.copy(cyp, to, StandardCopyOption.REPLACE_EXISTING);
             mergePdfs.deleteOnExit();
             temp1.deleteOnExit();
             //System.out.println("Complete");
           }
           catch (Exception i)
           {
               System.out.println(i);
           }
   }
	
	
	
	protected void  setForm(PdfCopy copy, JSONArray scheduleMap, Map<String, String> detailMap) throws FileNotFoundException, IOException, DocumentException, WriterException {
    	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();	
		
		for(int i = 0; i < scheduleMap.length() ; i++) {
			JSONObject setDetail = new JSONObject();
    		setDetail = (JSONObject)scheduleMap.get(i);
    		
    		String templete = setDetail.get("formPage").toString();
    		File ftmp = new File(templete);
    		if(ftmp.exists() && !ftmp.isDirectory()) { 
	    		   
	    		
		    	PdfReader reader = new PdfReader(new FileInputStream(templete));
		
		    	PdfStamper stamper = new PdfStamper(reader, baos);
		    	
			    AcroFields fields = stamper.getAcroFields();		        	
				fields.setGenerateAppearances(true);
		        stamper.setFormFlattening(true);
		            
				setNameField(fields, stamper, detailMap);	
				Object[] keys = fields.getFields().keySet().toArray();
				for(int f = 0;f < keys.length;f++) {
					fields.removeField((String)keys[f]);
				}
			    stamper.close();		  
		    	
		    	int copyPage =  Integer.parseInt(setDetail.get("pageCopy").toString());
		    	
		    	for(int j = 0; j < copyPage ; j++) {	    		  		
		    		 
			    	reader = new PdfReader(baos.toByteArray());
			   	 	copy.addPage(copy.getImportedPage(reader,1)); // Choose page 
			   	 	copy.freeReader(reader);
			   	 	reader.close();        
		        }     
	        }	   
		}
    	baos.close(); 
	}
	
	protected void setNameField(AcroFields fields, PdfStamper stamper, Map<String, String> data) throws IOException, DocumentException, WriterException {
        // Set font size.
		
	    BaseFont font = BaseFont.createFont(fontbase, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		
		Map<String, AcroFields.Item> map = new HashMap<String, AcroFields.Item>();
		map = fields.getFields();
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			String fieldName = iterator.next();
//			String fieldValue = fields.getField(fieldName);
//			int fieldType = fields.getFieldType(fieldName);	
//			System.out.println(fieldName + "(" + fieldType + ") = " + fieldValue );
//			fields.setField(fieldName,fieldName);
			//System.out.println(fields.getFieldItem(fieldName).getWidget(0) +" :");  
    		
			for (Entry<String, String> dataValue : data.entrySet()) {	    		
				
				if (dataValue.getKey().equals(fieldName)) {
					String[] sentences = fieldName.split("\\_");
					
					if(sentences[0].equals("gen")) {
						String name = "";
						if(sentences[1].length()>=7) name = sentences[1].substring(0,7);
						else if(sentences[1].length()<=6) name = sentences[1].substring(0,6);
						
						if(name.equals("barcode"))setBarcode(fields,stamper ,fieldName,data.get(fieldName)); //"barcode"
						else if(name.equals("qrcode"))setQRCode(fields, stamper, fieldName,data.get(fieldName)); // qrcode
					}
					else if(sentences[0].equals("img")) {
						setImgField(fields, stamper, fieldName,data.get(fieldName));
					}else if (sentences[0].equals("txt")) {
						
//
//						PdfDictionary widgetDict = fields.getFieldItem(fieldName).getMerged(0);
//						PdfNumber alignment = widgetDict.getAsNumber(PdfName.FF);
//						System.out.println(fieldName);  
//						System.out.println(alignment); 
//	
//						
//						fields.setFieldProperty(fieldName, "bgcolor", Color.TRANSLUCENT, null);
//		    	    	//fields.setFieldProperty(fieldName, "bordercolor", Color.TRANSLUCENT, null);
//						fields.setFieldProperty(fieldName, "textfont", font, null);    	
//		    	    	//fields.setFieldProperty(fieldName, "textsize", 12f, null);
//		    	    	fields.setFieldProperty(fieldName, "fflags", PdfFormField.FLAGS_INVISIBLE, null);	

						
						PdfDictionary widgetDict = fields.getFieldItem(fieldName).getWidget(0);
						TextField textField = new TextField(null, null, null);
						fields.decodeGenericDictionary(widgetDict, textField);
						float fontsize = textField.getFontSize();  // Font Size
						int txtalign = textField.getAlignment();   // Align Text
						//font = textField.getFont(); // Font Base
						Rectangle rect = fields.getFieldPositions(fieldName).get(0).position;		    	    
						//System.out.println(textField.getFont());
						PdfContentByte over = stamper.getOverContent(1);
						over.beginText();
						over.setFontAndSize(font,fontsize);// set font and size
						over.setColorFill(BaseColor.BLACK);// set color text
						
						String test = "";
						if(data.get(fieldName).equals(""))test = fieldName;
						else test =  data.get(fieldName);
						if(txtalign==0) {						
							over.showTextAligned(PdfContentByte.ALIGN_LEFT, test, rect.getLeft(), rect.getBottom(), 0);	
						}else if(txtalign==2) {	
							over.showTextAligned(PdfContentByte.ALIGN_RIGHT, test, rect.getRight(), rect.getBottom(), 0);				
						}else if(txtalign==1) {	
							over.showTextAligned(PdfContentByte.ALIGN_CENTER, test, rect.getLeft()+((rect.getWidth()/2)), rect.getBottom(), 0);			
						}					
						over.endText();	
						
					}

				} 	
							
			}
	    		
		}
		
	
     }
	
	
	
	protected void  setImgField(AcroFields fields, PdfStamper stamper ,String field,String value) {	
	
    	try {    		
    		
    		Rectangle rect = fields.getFieldPositions(field).get(0).position;
    	    float left   = rect.getLeft();
    	    float width  = rect.getWidth();
    	    float height = rect.getHeight();
    		
    		Image img = Image.getInstance(value);  		
    		img.scaleAbsolute(width,height);

    		
    		img.setAbsolutePosition(left, rect.getBottom());
    		PdfContentByte canvas = stamper.getOverContent(1);
    		canvas.addImage(img);    		
    		
		} catch (BadElementException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

     }
	
	
    
	protected void setBarcode(AcroFields fields,PdfStamper stamper, String field,String value ) {  
		value = value.replace(" ", "\n");
        Barcode128  barcode = new Barcode128();
        //barcode.setBaseline(-1); //text to top
        //final BaseFont font = BaseFont.createFont(fontbase1, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
       // barcode.setFont(font);  //null removes the printed text under the barcode
        barcode.setBarHeight(12f); // great! but what about width???
        //barcode.setSize(9f);
        barcode.setX(1f); 
        //barcode.setFont(null);
        barcode.setCodeType(Barcode.CODE128);
        barcode.setCode(value);     

	    try {
	    	Rectangle rect = fields.getFieldPositions(field).get(0).position;
    	    float left   = rect.getLeft();
    	    float width  = rect.getWidth();
    	    float height = rect.getHeight();
    	    Image img = Image.getInstance(barcode.createAwtImage(Color.BLACK, Color.WHITE), null, true);
    		 		
    		img.scaleAbsolute(width,height);

    		
    		img.setAbsolutePosition(left, rect.getBottom());
    		PdfContentByte canvas = stamper.getOverContent(1);
    		canvas.addImage(img);    
		} catch (BadElementException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
               
    }
    
//	protected void setQRCode(AcroFields fields, PdfStamper stamper, String field,String value ) {  
//	    
//    	Map<EncodeHintType, Object> hashMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class); 
//    	hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
//    	hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");	
//    	hashMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */
//
//		try {				
//
//			QRCodeWriter barcodeWriter = new QRCodeWriter();
//    	    BitMatrix bitMatrix =  barcodeWriter.encode(value, BarcodeFormat.QR_CODE, 200, 200,hashMap);
//    	   
//			int matrixWidth = bitMatrix.getWidth();			
//			BufferedImage qrImg  = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
//			qrImg.createGraphics();
//
//			Graphics2D graphics = (Graphics2D) qrImg.getGraphics();
//			//graphics.setComposite(AlphaComposite.Clear);
//			graphics.setColor(Color.WHITE);
//			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
//			// Paint and save the image using the ByteMatrix
//			graphics.setColor(Color.BLACK);
//
//			for (int i = 0; i < matrixWidth; i++) {
//				for (int j = 0; j < matrixWidth; j++) {
//					if (bitMatrix.get(i, j)) {
//						graphics.fillRect(i, j, 1, 1);
//					}
//				}
//			}
//			
//			//graphics.setComposite(AlphaComposite.SrcOver);
//			graphics.drawImage(qrImg, 0, 0, matrixWidth, matrixWidth, null);
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ImageIO.write(qrImg, "gif", bos);
//			
//			Rectangle rect = fields.getFieldPositions(field).get(0).position;
//    	    float left   = rect.getLeft();
//    	    float width  = rect.getWidth();
//    	    float height = rect.getHeight();
//    		
//    	   
//    	    
//    		Image img = Image.getInstance(bos.toByteArray());  		
//    		img.scaleAbsolute(width,height);
//    		img.setAbsolutePosition(left, rect.getBottom());
//    		PdfContentByte canvas = stamper.getOverContent(1);
//    		canvas.addImage(img);    
//    		bos.close();
//			hashMap.clear();
//
//		} catch (BadElementException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DocumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (WriterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	   
//               
//    }
	protected void setQRCode(AcroFields fields, PdfStamper stamper, String field,String value ) {  
		value = value.replace(" ", "\n");
    	Map<EncodeHintType, Object> hashMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class); 
    	hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    	hashMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");	
    	hashMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */

		try {				

			QRCodeWriter barcodeWriter = new QRCodeWriter();
    	    BitMatrix bitMatrix =  barcodeWriter.encode(value, BarcodeFormat.QR_CODE, 400, 400,hashMap);
    	   
			int matrixWidth = bitMatrix.getWidth();			
			BufferedImage qrImg  = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			qrImg.createGraphics();

			Graphics2D graphics = (Graphics2D) qrImg.getGraphics();
			//graphics.setComposite(AlphaComposite.Clear);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			// Paint and save the image using the ByteMatrix
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (bitMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			//graphics.setComposite(AlphaComposite.SrcOver);
			graphics.drawImage(qrImg, 0, 0, matrixWidth, matrixWidth, null);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
		
			
			BufferedImage logoImage = ImageIO.read( new File(logoTMSTH));
	        int finalImageHeight = qrImg.getHeight() - logoImage.getHeight();
	        int finalImageWidth = qrImg.getWidth() - logoImage.getWidth();
            graphics.drawImage(qrImg, 0, 0, null);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            graphics.drawImage(logoImage, (int) Math.round(finalImageWidth / 2), (int) Math.round(finalImageHeight / 2), null);

	        ImageIO.write(qrImg, "png", bos);
			
			
			Rectangle rect = fields.getFieldPositions(field).get(0).position;
    	    float left   = rect.getLeft();
    	    float width  = rect.getWidth();
    	    float height = rect.getHeight();
    		
    		Image img = Image.getInstance(bos.toByteArray());  		
    		img.scaleAbsolute(width,height);
    			
    		
    		img.setAbsolutePosition(left, rect.getBottom());
    		PdfContentByte canvas = stamper.getOverContent(1);
    		canvas.addImage(img);    
    		bos.close();
			hashMap.clear();

		} catch (BadElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	   
               
    }
	
	
    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)),StandardCharsets.UTF_8);
        return new JSONObject(content);
    }


}

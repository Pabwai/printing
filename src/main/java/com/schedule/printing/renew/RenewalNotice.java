package com.schedule.printing.renew;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bouncycastle.util.test.Test;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PushbuttonField;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;

public class RenewalNotice {	
	
	private String fontbase;	//full path font
	

	public void setFilePDF(String data) {	 

		try {
			
			
			JSONObject jsonObject = parseJSONFile(data);
			
			JSONObject object = (JSONObject) jsonObject.get("path");
			String pathfile = object.getString("pathfile");
			String namefile = object.getString("namefile");	
			fontbase = object.getString("fontbase");			
			String fileout = pathfile + namefile;	
			
			
			JSONArray arr = (JSONArray)jsonObject.get("schedule");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Document document = new Document();		    
			PdfCopy copy = new PdfCopy(document, bos);
			
			for (int i = 0; i < arr.length(); i++) {
				
				JSONObject jdata  = (JSONObject)arr.get(i);				
				
				JSONArray setting = (JSONArray)jdata.get("setting");

				JSONObject deatil = (JSONObject)jdata.get("detail");
				
				Map<String, String> map  =  new HashMap<String, String>() ;
			    
			    Iterator<String> keysItr = deatil.keys();
			    while(keysItr.hasNext() ) {
			        String key = keysItr.next();
			        String value = deatil.getString(key);
			        
			        map.put(key, value);
			    }
			    

			    document.open();    
			    for (int j = 0; j < setting.length(); j++) {
			    	JSONObject setDetail = new JSONObject();
		    		setDetail = (JSONObject)setting.get(j);	    		
		    		int copyPage = Integer.parseInt(setDetail.get("pageCopy").toString());
			    	
			    	setForm(copy,copyPage,setDetail.getString("formPage"),map);
			    }	
			    
			    map.clear();
			}
		   		  
		    document.close();
		    

		    if(object.getString("scheduleSet").equals("YES")) {

		    	File filePDF = new File(fileout);
				if(filePDF.getAbsoluteFile().exists()) { 
				    mergePdfs(bos,fileout,pathfile,namefile);
					bos.close();					
				}else {
					FileOutputStream fileOutputStream = new FileOutputStream(fileout);
			    	bos.writeTo(fileOutputStream);
			    	bos.close();
			    	fileOutputStream.close();
				}
		    }else {
		    	FileOutputStream fileOutputStream = new FileOutputStream(fileout);
		    	bos.writeTo(fileOutputStream);
		    	bos.close();
		    	fileOutputStream.close();
		    }

		    //System.out.println("AddTextOnPDF Complete");	    
		
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
	
	public void mergePdfs(ByteArrayOutputStream bos ,String fileout,String pathfile,String namefile ){
        try {
        	 //File file = File.createTempFile("temp", ".pdf",new File("D:\\workspec\\data\\temp")); // C:\Users\amit_\AppData\Local\Temp\ if not fix path
         	 File file = File.createTempFile("temp", ".pdf");
        	 InputStream is = new ByteArrayInputStream(bos.toByteArray());
             Document pDFCombineUsingJava = new Document();

             PdfCopy copy = new PdfCopy(pDFCombineUsingJava , new FileOutputStream(file.getAbsolutePath()));
             pDFCombineUsingJava.open();
             PdfReader ReadInputPDF = null;

             for (int i = 0; i < 2; i++) {
            	 if(i==0)ReadInputPDF = new PdfReader(fileout);
            	 else ReadInputPDF = new PdfReader(is);
                 copy.addDocument(ReadInputPDF);
                 copy.freeReader(ReadInputPDF);
                 ReadInputPDF.close();
             }     
             
             pDFCombineUsingJava.close();    
             //Thread.sleep(1000);
             
             if (!file.exists()) throw new java.io.IOException("Not found file exists........");
             Path copyTO = Paths.get(pathfile+namefile);
             Path temp = Paths.get(file.getAbsolutePath()); 
             Files.copy(temp, copyTO, StandardCopyOption.REPLACE_EXISTING);
             file.deleteOnExit();

             //System.out.println("Complete");
           }
           catch (Exception i)
           {
               System.out.println(i);
           }
   }
	
	
	
	protected void  setForm(PdfCopy copy,int pageCopy, String form,Map<String, String> map) throws FileNotFoundException, IOException, DocumentException {
    	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();		
    	for(int i = 0; i < pageCopy ; i++) {
    		
	    	PdfReader reader = new PdfReader(new FileInputStream(form));
	    	PdfStamper stamper = new PdfStamper(reader, baos);
	    	
		    AcroFields fields = stamper.getAcroFields();		        	
		    fields.setGenerateAppearances(true);
            stamper.setFormFlattening(true);
		   // System.out.println(i); 
		    
            
		    setNameField(fields,map );		

            //stamper.setFormFlattening(false);
		    			    
	        stamper.close();		        
	        reader = new PdfReader(baos.toByteArray());
	        copy.addPage(copy.getImportedPage(reader,1)); // Choose page 
	        reader.close();
	        
	    }
    	baos.close();
    	

     }
	
	protected void setNameField(AcroFields fields,Map<String, String> data) throws IOException, DocumentException {
        // Set font size.
		
		final BaseFont font = BaseFont.createFont(fontbase, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
		
		Map<String, AcroFields.Item> map = new HashMap<String, AcroFields.Item>();
		map = fields.getFields();
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			String fieldName = iterator.next();
//			String fieldValue = fields.getField(fieldName);
//			int fieldType = fields.getFieldType(fieldName);	
//			System.out.println(fieldName + "(" + fieldType + ") = " + fieldValue );
//			fields.setField(fieldName,fieldName);
			
			
			for (Entry<String, String> dataValue : data.entrySet()) {	    		
	    		if (dataValue.getKey().equals(fieldName)) {
	    			
	    			if(fieldName.equals("img_head")) {
	    				setImgField(fields,fieldName,dataValue.getValue());
	    				
	    			}else if(fieldName.equals("barcode")) {
	    				setBarcode(fields,fieldName,dataValue.getValue());
	    			}else if(fieldName.equals("qrcode")) {
	    				setQRCode(fields,fieldName,dataValue.getValue());
	    			}else {
	    				fields.setFieldProperty(fieldName, "textfont", font, null);    	
		    	    	fields.setFieldProperty(fieldName, "textsize", 10f, null);
		    	    	//fields.setFieldProperty(fieldName, "fflags", PdfFormField., null);
		    			fields.setField(fieldName,dataValue.getValue());
	    			}   			
	    		
	    		} 	
	    	}				
			
	    		
		}
		
	
     }
	
	
	
	protected void  setImgField(AcroFields fields,String field,String value) {
	
    	
    	try {
    		
    		PushbuttonField ad = fields.getNewPushbuttonFromField(field);
        	ad.setLayout(PushbuttonField.LAYOUT_ICON_ONLY);
        	ad.setProportionalIcon(true);

			ad.setImage(Image.getInstance(value));
			fields.replacePushbuttonField(field, ad.getField());
		} catch (BadElementException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

     }
	
	
    
    protected void setBarcode(AcroFields fields,String field,String value ) {  

        Barcode128  barcode = new Barcode128();
        //barcode.setBaseline(-1); //text to top
        //final BaseFont font = BaseFont.createFont(fontbase1, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
       // barcode.setFont(font);  //null removes the printed text under the barcode
        barcode.setBarHeight(45f); // great! but what about width???
        barcode.setSize(9f);
        barcode.setX(1f); 
        barcode.setFont(null);
        barcode.setCodeType(Barcode.CODE128);
        barcode.setCode(value);     

	    try {
	    	PushbuttonField ad = fields.getNewPushbuttonFromField(field);
			ad.setLayout(PushbuttonField.LAYOUT_ICON_ONLY); 
			ad.setProportionalIcon(true);
			ad.setImage(Image.getInstance(barcode.createAwtImage(Color.BLACK, Color.WHITE), null, true));
			fields.replacePushbuttonField(field, ad.getField());
		} catch (BadElementException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
               
    }
    
    protected void setQRCode(AcroFields fields,String field,String value ) {  
    
		Map<EncodeHintType, Object> mHints = new HashMap<>();
	    mHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
	    mHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");	
		
		BarcodeQRCode qrcode = new BarcodeQRCode(value, 33, 33, mHints);	
		mHints.clear();
		
		try {
			Image qrcodeImage = qrcode.getImage();
			qrcodeImage.scalePercent(200);
			PushbuttonField ad = fields.getNewPushbuttonFromField(field);
			ad.setLayout(PushbuttonField.LAYOUT_ICON_ONLY); 
			ad.setProportionalIcon(true);
		    ad.setImage(Image.getInstance(qrcodeImage));
			fields.replacePushbuttonField(field, ad.getField()); 
		} catch (BadElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //qrcodeImage.setAbsolutePosition(10,500);     
    }
    
    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)),StandardCharsets.UTF_8);
        return new JSONObject(content);
    }


}

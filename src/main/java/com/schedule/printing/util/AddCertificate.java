package com.schedule.printing.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class AddCertificate {
	
	
	public ByteArrayOutputStream AddCert(ByteArrayOutputStream os) throws IOException, Exception {

		HttpURLConnection connection;

		// connection = (HttpURLConnection) new
		// URL("http://16.90.10.43:8080/DigitalSign/services/DigitalSignatureWebService").openConnection();
		connection = (HttpURLConnection) new URL(
				"http://tmssapip04.tmith.net:8080/DigitalSign/services/DigitalSignatureWebService").openConnection();
		String SOAPAction = "";

		String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dig=\"http://digitalsignature.ws.jds.isc.com/\">\r\n"
				+ "   <soapenv:Header/>\r\n" + "   <soapenv:Body>\r\n" + "      <dig:signPdfRequest>\r\n"
				+ "         <configName>pkcs11_prd1_ocs</configName>\r\n"
				+ "         <password>STYsign25^@</password>\r\n" + "         <unsignedPdf>" + new String(Base64.getEncoder().encode(os.toByteArray()))
				+ "</unsignedPdf>\r\n" + "         <pdfPassword></pdfPassword>\r\n"
				+ "      </dig:signPdfRequest>\r\n" + "   </soapenv:Body>\r\n" + "</soapenv:Envelope>";

		// Compress the data to save bandwidth
		byte[] compressedData = xml.getBytes();

		// Add headers
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		connection.setRequestProperty("SOAPAction", SOAPAction);

		// Send data
		connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.write(compressedData);
		outputStream.flush();
		outputStream.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(new String(response)));
		Document doc = builder.parse(src);
		String status = doc.getElementsByTagName("respDesc").item(0).getTextContent();
		String data = doc.getElementsByTagName("signedPdf").item(0).getTextContent();

		if (status.equals("Success")) {
			os.reset();
			os.write(Base64.getDecoder().decode(data));
		}
		return os;
	}

}

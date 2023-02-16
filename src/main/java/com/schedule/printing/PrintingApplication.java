package com.schedule.printing;


import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import com.schedule.printing.renew.RenewalNotice;


@SpringBootApplication
@EnableAsync
public class PrintingApplication {

	public static void main(String[] args) throws IOException {
		java.lang.Runtime.getRuntime().exec("java -Xmx1024m");
		SpringApplication.run(PrintingApplication.class, args);
		
		if (args.length != 1){ 
			System.err.println("usage: java " + PrintingApplication.class.getName() + " <input>"); System.exit(1); 
			
		}
		 RenewalNotice re = new RenewalNotice();
         re.setFilePDF(args[0]);
         
         //File file = new File(args[0]);  
         //if (file.exists()) file.delete();
	}

}

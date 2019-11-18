package com.uplus.ledger;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UplusLedgerApplication {

	public static boolean NoLog = true;
	static final Logger logger = Logger.getLogger(UplusLedgerApplication.class);
	
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
	
	public static void PrintLog ( String message )
	{
		if ( NoLog )
		{
			logger.debug(message);
			//logger.trace(message);
		}
	}
	
	public static void PrintLogException ( String message )
	{
		if ( NoLog )
			logger.debug(message);
		System.out.println ( message );
	}
	
	public static void PrintSystemLog ( String message )
	{
		System.out.println ( message );
		PrintLog( message );
	}

	public static void main(String[] args) {		
		if ( args.length >= 1 )
		{
			String log = args[0];
			if ( log.equals("NOLOG") )
			{
				NoLog = false;
				PrintSystemLog("#####################################################");
				PrintSystemLog("    UPLUS for Ledger service VER 1.0        	     ");
				PrintSystemLog("    Start Ledger service			   	     		 ");
				PrintSystemLog("#####################################################");		
			} else {
				PropertyConfigurator.configure(log);
				PrintLog("LOG-SETUP-LOCALTION:" + log);
				PrintSystemLog("LOG-SETUP-LOCALTION:" + log);
				PrintLog("#####################################################");
				PrintLog("    UPLUS for Ledger service VER 1.0       	       ");
				PrintLog("    Start Ledger service			 	  	     	   ");
				PrintLog("#####################################################");	
			}
			SpringApplication app = new SpringApplication(UplusLedgerApplication.class);
			app.setBannerMode(Mode.OFF);
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
			app.run(args);
		} else {
			PrintLog ( "check application args.");
		}
	}
}

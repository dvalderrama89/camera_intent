package edu.ucsb.cs.cs185.dvalderrama.dvalderramagpscam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GPSToFile {
	
	private static String textToXML = "";
	private static String pathToXMLFile = "";
	
	public GPSToFile(){}
	
	/*
	 * File output will be formatted like this:
	 * 
	 * <image>
	 * <name>photo-001.jpg</name>
	 * <lat>34.4208333</lat>
	 * <long>119.6972222</long>
	 * </image>
	 * 
	 * ...etc
	 */
	public void toXML(String filename, String pathToXMLFile, double latitude, double longitude)
	{
		textToXML += "<image>\n<name>" + filename + "</name>\n<lat>" + latitude + "</lat>\n";
		textToXML += "<long>" + longitude + "</long>\n</image>";
		this.pathToXMLFile = pathToXMLFile;
		
		appendLog(textToXML);
	}
	
	public void appendLog(String text)
	{       
	   File logFile = new File(pathToXMLFile);
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.flush();
	      buf.close();
	      
	      //After the text has been written to a file, clear it out
	      textToXML = "";
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
}

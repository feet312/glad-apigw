package com.kt.icis.sa.icisapigw.common.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlbeans.XmlObject;
import org.json.JSONObject;

public class CommonUtil {
	protected static String serverIpAddress = "";
	
	public static String getPrettyXml(XmlObject xml) {
		return getPrettyXml(xml.xmlText());
	}
	
	public static String getPrettyXml(String xml) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, Charset.defaultCharset().displayName());
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);
			
			transformer.transform(new StreamSource(xml), streamResult);
			return stringWriter.toString();
		} catch (Exception e) {
			return xml;
		}
	}
	
	public static String getDateTime() {
		return getDateTime("YYYY.MM.DD HH:MI:SS.MS");
	}
	
	public static String getDateTime(String format) {
		String dateTime;
		
		if(format != null && "".equals(format)) {
			dateTime = "YYYY.MM.DD HH:MI:SS.MS";
		} else {
			dateTime = format.toUpperCase();
		}
		
		Calendar cal = Calendar.getInstance();
		String yyyy = Integer.toString(cal.get(Calendar.YEAR));
		String mm = ((cal.get(Calendar.MONTH) + 1 < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.MONTH) + 1);
		String dd = ((cal.get(Calendar.DATE) < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.DATE));
		int ampm = cal.get(Calendar.AM_PM);
		String hh12 = ((cal.get(Calendar.HOUR) < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.HOUR));
		String hh = ((cal.get(Calendar.HOUR_OF_DAY) < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		String mi = ((cal.get(Calendar.MINUTE) < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.MINUTE));
		String ss = ((cal.get(Calendar.SECOND) < 10) ? "0" : "") + Integer.toString(cal.get(Calendar.SECOND));
		DecimalFormat df = new DecimalFormat("000");
		String ms = df.format(cal.get(Calendar.MILLISECOND));
		
		dateTime = dateTime.toUpperCase();
		dateTime = dateTime.replaceAll("12H", hh12);
		dateTime = dateTime.replaceAll("YYYY", yyyy);
		dateTime = dateTime.replaceAll("MM", mm);
		dateTime = dateTime.replaceAll("DD", dd);
		dateTime = dateTime.replaceAll("HH", hh);
		dateTime = dateTime.replaceAll("MI", mi);
		dateTime = dateTime.replaceAll("SS", ss);
		dateTime = dateTime.replaceAll("MS", ms);
		dateTime = dateTime.replaceAll("AM_PM", (ampm == Calendar.AM) ? "AM" : "PM");
		
		return dateTime;
	}
	
	public static long getTimeStamp() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	public static String getJsonValue(String jsonString, String jsonKey) {
		String jsonValue = "";
		
		if(jsonString != null && jsonString.length() > 0 && jsonString.indexOf(jsonKey) > -1) {
			try {
				JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
				JsonObject arbitraryKeyJson = jsonReader.readObject();
				jsonValue = arbitraryKeyJson.getString(jsonKey);
			} catch (Exception e) {
				jsonValue = "";
			}
		}
		return jsonValue;
	}
	
	public static String getServerIpAddress() throws UnknownHostException {
		if(serverIpAddress == null || "".equals(serverIpAddress)) {
			serverIpAddress = java.net.Inet4Address.getLocalHost().getHostAddress();
		}
		return serverIpAddress;
	}
}

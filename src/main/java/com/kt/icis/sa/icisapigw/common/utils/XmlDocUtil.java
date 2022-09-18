package com.kt.icis.sa.icisapigw.common.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.xml.sax.SAXException;


import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * XML Data를 만들기 위해 특수문자를 없애거나, XML에 포함된 특수문자를 복구한다. <br>
 * TobeSoft의 UI Tool용 Lib를 복사해서 일부 수정한 것이다. 
 * @author sehwan
 *
 */
public final class XmlDocUtil {

	/**
	 * XML 문자열로 바꿀대상 
	 */
	static final String NEED_CHARS = "\000\t\r\n\"&\\<>";
	
	public XmlDocUtil() {
		
	}
	
	/**
	 * 문자열에서 XML로 사용될 수 없는 부분을 변경한다. 
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		return encode(str.toCharArray());
	}
	
	/**
	 * 문자열에서 XML로 사용될 수 없는 부분을 변경한다. 
	 * @param ch
	 * @return
	 */
	public static String encode(char ch[]) {
		StringBuffer buffer = new StringBuffer();
		int length = ch.length;
		for(int i=0; i < length; i++) {
			if(needToChange(ch[i])) {
				buffer.append(encode(ch[i], false));
			} else {
				buffer.append(ch[i]);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * XML로 사용될 수 없는 문자이면 변경한다.
	 * @param ch
	 * @return
	 */
	public static String encode(char ch) {
		return encode(ch, true);
	}
	
	/**
	 * 문자열에서 XML로 사용될 수 없는 부분을 변경한다.
	 * @param ch
	 * @param writer
	 * @throws IOException
	 */
	public static void encode(char ch[], Writer writer) throws IOException {
		PrintWriter out = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);
		int length = ch.length;
		for(int i=0; i < length; i++) {
			if(needToChange(ch[i])) {
				out.write(encode(ch[i], false));
			} else {
				out.write(ch[i]);
			}
		}
	}
	
	
	/**
	 * XML에 포함된 문자열을 일반문자로 원복한다. 
	 * @param str
	 * @return
	 */
	public static String decode(String str) {
		StringBuffer buffer = new StringBuffer();
		int length = str.length();
		for(int i=0; i < length; i++) {
			char ch = str.charAt(i);
			if(ch != '&') {
				buffer.append(ch);
				continue;
			}
			if(str.charAt(i + 1) == 'l' && str.charAt(i + 2) == 't' && str.charAt(i + 3) == ';') {
				buffer.append('<');
				i += 3;
				continue;
			}
			if(str.charAt(i + 1) == 'g' && str.charAt(i + 2) == 't' && str.charAt(i + 3) == ';') {
				buffer.append('>');
				i += 3;
				continue;
			}
			if(str.charAt(i + 1) == 'a' && str.charAt(i + 2) == 'm' && str.charAt(i + 3) == 'p' && str.charAt(i + 4) == ';') {
				buffer.append('&');
				i += 4;
				continue;
			}
			if(str.charAt(i + 1) == 'q' && str.charAt(i + 2) == 'u' && str.charAt(i + 3) == 'o' && str.charAt(i + 4) == 't' && str.charAt(i + 5) == ';') {
				buffer.append('"');
				i += 5;
				continue;
			}
			if(str.charAt(i + 1) == 'a' && str.charAt(i + 2) == 'p' && str.charAt(i + 3) == 'o' && str.charAt(i + 4) == 's' && str.charAt(i + 5) == ';') {
				buffer.append('\'');
				i += 5;
				continue;
			}
			if(str.charAt(i + 1) == '#') {
				int found = str.indexOf(";", i + 1);
				if(found < 0) {
					buffer.append(str.charAt(i));
					continue;
				}
				if(str.charAt(i + 2) == 'x' || str.charAt(i + 2) == 'X') {
					i = found;
					continue;
				}
				String num = str.substring(i + 2, found);
				if(num.equals("9")) {
					buffer.append('\t');
				} else if(num.equals("32")) {
					buffer.append(' ');
				} else if(num.equals("13")) {
					buffer.append('\r');
				} else if(num.equals("10")) {
					buffer.append('\n');
				}
				i = found;
			} else {
				buffer.append(str.charAt(i));
			}
		}
		return buffer.toString();
	}
	
	/**
	 * XML로 사용될 수 없는 문자이면 변경한다.
	 * @param ch
	 * @param isCheck
	 * @return
	 */
	private static String encode(char ch, boolean isCheck) {
		if(isCheck && !needToChange(ch)) {
			return String.valueOf(ch);
		}
		switch(ch) {
		case 0 :
			return "";
		case 9 :
			return "\t";
		case 13 :
			return "&#13;";
		case 10 :
			return "&#10;";
		case 34 : 
			return "&quot;";
		case 38 :
			return "&amp;";
		case 39 : 
			return "&apos;";
		case 60 : 
			return "&lt;";
		case 62 :
			return "&gt;";
		default :
			return String.valueOf(ch);
		}
	}
	
	/**
	 * XML 문자로 바꿀 대상인지 검토
	 * @param ch
	 * @return
	 */
	private static boolean needToChange(char ch) {
		if(NEED_CHARS.indexOf(ch) >= 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * XML에서 Namespace를 제거한다. 
	 * 
	 * @param xml
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws JDOMException
	 */
	public static String removeNamespace(final String xml) throws SAXException, IOException, ParserConfigurationException, JDOMException {
		
		if(xml == null || "".equals(xml)) return "";
		
		Document sourceDoc = XmlDocUtil.string2Document(xml);
		Element rootElement = sourceDoc.getRootElement();
		
		rootElement.removeNamespaceDeclaration(Namespace.getNamespace("http://schemas.xmlsoap.org/soap/envelope/"));
		rootElement = removeElement(rootElement);
		
		return XmlDocUtil.document2String(sourceDoc).replaceAll("xmlns=\"\"", "");
	}
	
	/**
	 * Element에서 Namespace를 제거한다.
	 * @param element
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws IOException
	 */
	protected static Element removeElement(Element element) throws TransformerFactoryConfigurationError, IOException {
		element.removeNamespaceDeclaration(element.getNamespace(""));
		element.removeNamespaceDeclaration(element.getNamespace());
		
		Element resElement = element.setNamespace(Namespace.NO_NAMESPACE);
		
		if(resElement.getChildren().size() > 0) {
			for(Element childrenElement : resElement.getChildren()) {
				String removeInheritedFlag = "false";
				if(childrenElement.getChildren().size() == 0) {
					
					if("true".equalsIgnoreCase(removeInheritedFlag)) {
						for(Namespace namespace : childrenElement.getNamespaceInherited()) {
							childrenElement.removeNamespaceDeclaration(namespace);
						}
					} else {
						childrenElement.removeNamespaceDeclaration(childrenElement.getNamespace(""));
					}
					childrenElement.setNamespace(Namespace.NO_NAMESPACE);
				} else {
					childrenElement = removeElement(childrenElement);
				}
			}
		}
		return resElement;
	}
	
	/**
	 * Element 의 Root Element Name을 Replace 한다.
	 * 
	 * @param body
	 * @param elementName
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws JDOMException
	 * @throws XmlException
	 * @throws TransformerException
	 */
	public static XmlObject replaceRootElementName(XmlObject body, String elementName) throws TransformerFactoryConfigurationError, SAXException, IOException, ParserConfigurationException, JDOMException, XmlException, TransformerException{
		Document document = XmlDocUtil.string2Document(body.xmlText());
		Element rootElement = document.getRootElement().getchildren().get(0);
		rootElement.setName(elemmentName);
		
		return XmlObject.Factory.parse(XmlDocUtil.element2String(rootElement));
	}
	
	/**
	 * XML Format의 String을 XML Document Format으로 변환 한다.
	 * 
	 * @param xmlString
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws JDOMException
	 */
	public static Document string2Document(String xmlString) throws SAXException, IOException, ParserConfigurationException, JDOMException {
		SAXBuilder builder = new SAXBuilder();	// org.jdom2
		builder.setIgnoringElementContentWhitespace(false);	// 여백 무시 가능 미설정 
		
		return builder.build(new StringReader(xmlString));
	}
	
	/**
	 * XML Document Format을 XML Type의 String Format으로 변환 한다.
	 * 
	 * @param document
	 * @return
	 */
	public static String document2String(Document document) {
		XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());	// org.jdom2
		return outputter.outputString(document);
	}
	
	/**
	 * Element를 String으로 변환한다. 
	 * 
	 * @param element
	 * @return
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public static String element2String(Element element) throws TransformerFactoryConfigurationError, TransformerException {
		XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
		return outputter.outputString(element);
	}
}

package com.kt.icis.sa.icisapigw.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kt.icis.sa.icisapigw.common.security.PayloadHash;
import com.kt.icis.sa.icisapigw.transform.model.ServiceResponse;

// Nexacro API for KT
import com.nexacro.xapi.data.DataSet;
import com.nexacro.xapi.data.DataSetList;
import com.nexacro.xapi.data.DataTypes;
import com.nexacro.xapi.data.PlatformData;
import com.nexacro.xapi.data.VariableList;
import com.nexacro.xapi.tx.PlatformException;
import com.nexacro.xapi.tx.PlatformRequest;
import com.nexacro.xapi.tx.PlatformResponse;
import com.nexacro.xapi.tx.PlatformType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransformUtil {

	private static XmlMapper xmlMapper;
	
	public static XmlMapper getInstanceXmlMapper() {
		if(xmlMapper == null) {
			xmlMapper = (XmlMapper) new XmlMapper().registerModule(new JavaTimeModule());
		}
		return xmlMapper;
	}
	
	private static Environment environment;
	
	public static Environment getEnvironment() {
		if(environment == null) {
			environment = (Environment) CommonBeanUtil.getBean(Environment.class);
		}
		return environment;
	}
	
	public static String xmlToJson(ObjectMapper mapper, String xmlBody) {
		JSONObject jObject = XML.toJSONObject(xmlBody);
		String jsonStr = null;
		try {
			Object jsonObj = mapper.readValue(jObject.toString(), Object.class);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return jsonStr;
	}
	
	public static String xmlToJsonArray(ObjectMapper mapper, String xmlBody) {
		JSONObject jObject = XML.toJSONObject(xmlBody);
		String jsonStr = null;
		
		JSONObject requestObject = (JSONObject) jObject.get("service_request");
		JSONObject saSecurityObject = (JSONObject) requestObject.get("saSecurity");
		String [] payloadNames = saSecurityObject.getString("paloadNames").split(",");
		Set<String> payloadNonArrayNames = new HashSet<>(Arrays.asList(payloadNames));
		
		if(xmlBody.contains("<typeHeader")) {
			// 배열데이터셋 처리 
			Object objects = requestObject.get("typeHeader");
			JSONArray typeObjects = null;
			
			if(objects instanceof JSONArray) {
				typeObjects = (JSONArray) objects;
			} else {
				typeObjects = new JSONArray();
				typeObjects.put((JSONObject) objects);
			}
			
			for(Object object : typeObjects) {
				JSONObject typeObject = (JSONObject) object;
				String datasetName = typeObject.getString("DATASET_NAME").substring(3);
				int datasetRowCnt = typeObject.getInt("DATASET_ROWS");
				payloadNonArrayNames.remove(datasetName);		// 처리된 배열건들은 제외 
				
				JSONArray payloadArray = null;
				if(datasetRowCnt == 0) {
					payloadArray = new JSONArray();
				} else if(requestObject.get(datasetName) instanceof JSONArray) {
					payloadArray = (JSONArray) requestObject.get(datasetName);
				} else {
					JSONObject payloadObject = (JSONObject) requestObject.get(datasetName);
					payloadArray = new JSONArray();
					payloadArray.put(payloadObject);
				}
				requestObject.put(datasetName, payloadArray);
			}
			
		}
		
		// 단건 데이터셋 처리 
		for(String payloadNonArrayName : payloadNonArrayNames) {
			JSONObject payloadObject = null;
			if(requestObject.has(payloadNonArrayName)) {
				payloadObject = (JSONObject)requestObject.get(payloadNonArrayName);
			} else {
				payloadObject = new JSONObject();
			}
			requestObject.put(payloadNonArrayName, payloadObject);
		}
		
		try {
			Object jsonObj = mapper.readValue(jObject.toString(), Object.class);
			jsonStr = mapper.writeValueAsString(jsonObj);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return jsonStr;
	}
	
	public static <T> T jsonToObject(ObjectMapper mapper, String jsonStr, String keyPath, TypeReference<T> typeReference) {
		T objT = null;
		try {
			String json = mapper.readTree(jsonStr).at(keyPath).toString();
			if(json != null) {
				objT = mapper.readValue(json, typeReference);
			}
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return objT;
	}
	
	public static <T> T xmlToObject(String xmlString, final TypeReference<T> typeReference) {
		ObjectMapper objectMapper = getInstanceXmlMapper();
		try {
			return objectMapper.readValue(xmlString, typeReference);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
	public static String getXmlResponseBody(Map<String, Object> serviceResponseMap) {
		XmlMapper xmlMapper = getInstanceXmlMapper();
		String xmlResponseBody = null;
		
		try {
			xmlResponseBody = xmlMapper.writerWithDefaultPrettyPrinter().withRootName("service_response").writeValueAsString(serviceResponseMap);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return xmlResponseBody;
	}
	
	public static void changeRequestUrl(ServerWebExchange exchange, String url, String path) {
		if(!path.startsWith("/")) path = "/" + path;
		String newUrl = new StringBuilder(url).append(path).toString();
		
		try {
			exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, new URI(newUrl));
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
	}
	
	public static boolean isPostMethod(ServerWebExchange exchange) {
		HttpMethod method = exchange.getRequest().getMethod();
		if(method == HttpMethod.POST) {
			return true;
		}
		return false;
	}
	
	
	public static boolean isRequestJsonType(ServerWebExchange exchange) {
		return isJsonContentType(exchange.getRequest().getHeaders());
	}
	
	public static boolean isResonseJsonType(ServerWebExchange exchange) {
		return isJsonContentType(exchange.getResponse().getHeaders());
	}
	
	private static boolean isJsonContentType(HttpHeaders headers) {
		String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
		if(MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType)) {
			return true;
		} return false;
	}
	
	public static void setRequestContentType(ServerWebExchange exchange, MediaType mediaType) {
		exchange.getRequest().getHeaders().setContentType(mediaType);
	}
	
	public static void setResponseContentType(ServerWebExchange exchange, MediaType mediaType) {
		exchange.getResponse().getHeaders().setContentType(mediaType);
	}
	
	public static void setRequestHeader(ServerWebExchange exchange, String headerName, String headerValue) {
		exchange.getRequest().getHeaders().set(headerName, headerValue);
	}
	
	public static void setResponseHeader(ServerWebExchange exchange, String headerName, String headerValue) {
		exchange.getResponse().getHeaders().set(headerName, headerValue);
	}
	
	public static void addRequestHeader(ServerWebExchange exchange, String headerName, String headerValue) {
		exchange.getRequest().getHeaders().add(headerName, headerValue);
	}
	
	public static void addResponseHeader(ServerWebExchange exchange, String headerName, String headerValue) {
		exchange.getResponse().getHeaders().add(headerName, headerValue);
	}
	
	/**
	 * RequestBody 를 X-API PlatformData --> String 으로 변환한다. From KOS ESB Code
	 * @param requestBody
	 * @param encCharSet
	 * @param hashFlag
	 * @return
	 */
	public static String getRequestXmlStr(String requestBody, String encCharSet, String hashFlag) {

		PlatformRequest aPlatformRequest;	// Nexacro API 
		String uixml;
		String requestXmlStr = "";
		InputStream inStream;
		try {
			Environment environment = getEnvironment();
			String activeProfile = String.valueOf(environment.getProperty("spring.profile.active"));
			String ssvFlag = String.valueOf(environment.getProperty("icis.ui.ssv.flag"));
			
			if(activeProfile.equals("local")) {
				uixml = requestBody.substring(44);
			} else {
				uixml = PayloadHash.checkRequestHash(requestBody, hashFlag);
			}
			
			if(uixml.equals("")) return "";
			
			inStream = new ByteArrayInputStream(uixml.getBytes(encCharSet));
			
			if(ssvFlag.equals("true")) {
				aPlatformRequest = new PlatformRequest(inStream, PlatformType.CONTENT_TYPE_SSV);
			} else {
				aPlatformRequest = new PlatformRequest(inStream, PlatformType.CONTENT_TYPE_XML);
			}
			
			aPlatformRequest.receiveData();
			inStream.close();
			PlatformData platformData = aPlatformData.getData();
			
			// nexacro 로 변환된 요청을 xml document로 리턴 
			Element element = platformData2Xml(platformData);
			
			// Xml document를 String로 변환.
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(element), new StreamResult(buffer));
			requestXmlStr = buffer.toString();
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new IllegalArgumentException("ICSS1007");
		}
		return requestXmlStr;
	}
	
	/**
	 * X-API PlatformData를 XML로 변환한다. OSB에서 사용하는 w3c의 Element Type을 사용한다. FROM KOS ESB CODE
	 * @param data	PlatformData(Nexacro)
	 * @return org.w2c.dom.Element
	 * @throws Exception
	 */
	public static Element platformData2Xml(PlarformData data) throws Exception {
		
//		DataSetList dataSetList = data.getDataSetList();	// Nexacro API
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		
		Element rootElement = document.createElement("service_request");
		
		Environment environment = getEnvironment();
		boolean removeNullFlag = Boolean.valueOf(environment.getProperty("icis.ui.remove.null.element"));
		boolean removeNullStringFlag = Boolean.valueOf(environment.getProperty("icis.ui.remove.null.string.element"));
		
		List<String> datasetNameList = new ArrayList<String>();
		String localPort = "";
		
		for(int i=0; i < dataSetList.size(); i++) {
			DataSet dataSet = dataSetList.get(i);
			String dataSetName = dataSet.getName();
			datasetNameList.add(dataSetName);
			
			for(int j=0; j < dataSet.getRowCount(); j++) {
				org.w3c.dom.Element element = document.createElement(dataSet.getName());
				
				for(int k=0; k < dataSet.getColumnCount(); k++) {
					String elementName = dataSet.getColumn(k).getName();
					String elementValue = dataSet.getString(j, elementName);
					if(("commonHeader".equals(dataSetName) || "bizHeader".equals(dataSetName))
							&& ("svcName".equals(elementName) || "cbSvcName".equals(elementName))
							&& StringUtils.isNotBlank(elementValue)) {
						String svcName = elementValue;
						int pos = svcName.indexOf("?");
						if(pos > 0) {
							elementValue = svcName.substring(0, pos);
							localPort = svcName.substring(pos+11);
						}
					}
					
					if((removeNullFlag && elementValue == null) || (removeNullStringFlag && "".equals(elementValue))) {
						continue;
					}
					
					org.w3c.dom.Element childElement = document.createElement(elementName);
					childElement.appendChild(document.createTextNode(elementValue));
					element.appendChild(childElement);
				}
				rootElement.appendChild(element);
			}					
		}
		
		// 공통 Element Set - saSecurity 만들기. 
		Element securityElement = document.createElement("saSecurity");
		Element payloadNamesChild = document.createElement("payloadNames");
		Element payloadOriginChild = document.createElement("payloadOrigin");
		Element localPortChild = document.createElement("localPort");
		
		List<String> payloadNameList = datasetNameList.stream().filter(t -> !t.equalsIgnoreCase("commonHeader"))
				.filter(t -> !t.equalsIgnoreCase("bizHeader")).filter(t -> !t.equalsIgnoreCase("typeHeader")).collect(Collectors.toList());
		String payloadNames = payloadNameList.stream().map(t -> t).collect(Collectors.joining(","));
		
		payloadNamesChild.setTextContent(payloadNames);
		payloadOriginChild.setTextContent("A");	// A:APIGW, S:SERVICE
		localPortChild.setTextContent(localPort);
		securityElement.appendChild(payloadNamesChild);
		securityElement.appendChild(payloadOriginChild);
		securityElement.appendChild(localPortChild);
		
		rootElement.appendChild(securityElement);
		return rootElement;
	}
	
	/**
	 * Xml 구조의 Text를 UI Format의 Xml구조로 변경한다.
	 *  
	 * @param inputXml
	 * @return
	 * @throws Exception
	 */
	public static String getXml2UiFormatXml(final String inputXml) throws Exception {
		org.jdom2.Document sourceDoc = XmlDocUtil.string2Document(inputXml);
		
		List<org.jdom2.Element> resElementList = new ArrayList<org.jdom2.Element>();
		
		org.jdom2.Element rootElement = new org.jdom2.Element("response");
		org.jdom2.Document resDoc = new org.jdom2.Document(rootElement);
		
		resElementList = createElement(resElementList, sourceDoc.getRootElement());
		resDoc.getRootElement().addContent(resElementList);
		
		return XmlDocUtil.document2String(resDoc);
		
	}
	
	/**
	 * Xml 구조의 Text를 UI Format의 Xml구조로 변경한다. (Sub Method)
	 * 
	 * @param resElementList
	 * @param element
	 * @return
	 */
	public static List<org.jdom2.Element> createElement(final List<org.jdom2.Element> resElementList, org.jdom2.Element element) {
		
		List<org.jdom2.Element> resultElementList = resElementList;
		org.jdom2.Element resElement = null;
		
		
		if(element.getChildren().size() > 0) {
			resElement = new org.jdom2.Element(element.getName());
			
			for(org.jdom2.Element childrenElement : element.getChildren()) {
				if(childrenElement.getChildren().size() == 0) {
					resElement.addContent(new org.jdom2.Element(children.getName().setText(childrenElement.getText())));
				} else {
					resultElementList = createElement(resultElementList, childrenElement);
				}
			}
			
			if(resElement.getChildren().size() > 0)
				resultElementList.add(resElement);
		}
		return resultElementList;
	}
	
	/**
	 * SOAP의 처리결과를 Nexacro PlatformData로 값을 리턴한다. 
	 * @param soaptext
	 * @param encodeflag
	 * @return
	 * @throws Exception
	 */
	public static PlatformData convertPlatformData(String soaptext, boolean encodeflag) throws Exception {
		PlatformData platformData = new PlatformData();	// Nexacro API
		DataSetList dataSetList = new DataSetList();	// Nexacro API
		
		org.jdom2.Document document = new SAXBuilder().build(new InputSource(new ByteArrayInputStream(soaptext.getBytes("UTF-8"))));
		org.jdom2.Element rootElement = document.getRootElement();
		org.jdom2.Element commonHeader = rootElement.getChild("commonHeader");
		
		if(commonHeader == null) {
			throw new IOException("commonHeader 가 존재하지 않습니다.");
		}
		
		List<org.jdom2.Element> elementList = rootElement.getChildren();
		List<String> dataSetNameList = new ArrayList<String>();
		
		for(org.jdom2.Element element : elementList) {
			String dataSetName = element.getName();
			boolean checkDuplicate  = false;
			
			for(String setName : dataSetNameList) {
				if(dataSetName.equals(setName)) {
					checkDuplicate = true;
					break;
				}
			}
			if(checkDuplicate) 
				continue;
			
			DataSet dataSet = new DataSet(dataSetName);
			dataSet.setChangeStructureWithData(true);
			dataSetNameList.add(dataSetName);
			
			for(org.jdom2.Element dataSetElement : rootElement.getChildren(element.getName())) {
				int rowNum = dataSet.newRow();
				
				for(org.jdom2.Element dataSetRow : dataSetElement.getChildren()) {
					String elementName = dataSetRow.getName();
					String elementValue = dataSetRow.getText();
					try {
						dataSet.set(rowNum, elementName, elementValue);
					} catch (java.lang.IllegalArgumentException e) {	// 해당 column이 없는 경우 추가 후 값을 넣는다. 
						dataSet.addColumn(dataSetRow.getName(), DataTypes.STRING);
						dataSet.set(rowNum, elementName, elementValue);
					}
				}
			}
			dataSetList.add(dataSet);
		}
		
		VariableList variableList = new VariableList();	// Nexacro API
		platformData.setDataSetList(dataSetList);
		platformData.setVariableList(variableList);
		
		return platformData;
		
	}
	
	public static String getXmlResponseBodyFromObj(ServiceResponse serviceResponse) {
		XmlMapper xmlMapper = getInstanceXmlMapper();
		String xmlResponseBody = null;
		
		try{
			xmlResponseBody = xmlMapper.writerWithDefaultPrettyPrinter().withRootName("service_response").writeValueAsString(serviceResponse);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return xmlResponseBody;
	}
	
	public static String transformObjToResponse(String uiDtoFormat) throws Exception, PlatformException, IOException, UnsupportedEncodingException {
		String strData;
		PlatformData platformData = TransformUtil.convertPlatformData(uiDtoFormat, flase);
		
		com.nexacro.xapi.data.VariableList variableList = new com.nexacro.xapi.data.VariableList();
		variableList.add("ErrorCode", "-99");
		platformData.setVariableList(variableList);
		
		ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
		PlatformResponse res = new PlatformResponse(aByteArrayOutputStream);
		
		res.setData(platformData);
		res.sendData();
		aByteArrayOutputStream.close();
		strData = aByteArrayOutputStream.toString("UTF-8");
		return strData;
	}
	
	
	
	
}

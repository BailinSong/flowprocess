package com.blueline.commons;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * xml������
 * 
 * @author Bailin
 *
 */
@SuppressWarnings("rawtypes")
public class XmlUtils {
	private XmlUtils() {

	}

	/**
	 * ��XMLת����MAP
	 * 
	 * @param xml_path
	 *            XML�ļ���ַ
	 * @return ת�����xml
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Map XML2MAP(String xml_path) throws InstantiationException, IllegalAccessException,
			ParserConfigurationException, SAXException, IOException {
		return XML2MAP(xml_path, ConcurrentHashMap.class);
	}

	public static Map XMLString2MAP(String xml) throws DOMException, ParserConfigurationException, SAXException,
			IOException, InstantiationException, IllegalAccessException {
		return XMLString2MAP(xml, ConcurrentHashMap.class);
	}

	/**
	 * ��XMLת����MAP
	 * 
	 * @param xml_path
	 *            XML�ļ���ַ
	 * @param class_type
	 *            ��������
	 * @return ת�����xml
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws DOMException
	 */
	public static Map XML2MAP(String xml_path, Class<? extends Map> class_type) throws ParserConfigurationException,
			SAXException, IOException, DOMException, InstantiationException, IllegalAccessException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		InputStream input = null;
		try {
			input = XmlUtils.class.getResourceAsStream(xml_path);
		} catch (Exception e) {
			;
		}
		if (input == null) {
			try {
				input = new FileInputStream(xml_path);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}

		db = dbf.newDocumentBuilder();
		Document document = db.parse(input);
		// ������нڵ㣬�ݹ�����ڵ�
		NodeList employees = document.getChildNodes();
		// ��xml�ļ�����
		return (Map) parserXml(employees, class_type);

	}

	public static Map XMLString2MAP(String xml, Class<? extends Map> class_type) throws ParserConfigurationException,
			SAXException, IOException, DOMException, InstantiationException, IllegalAccessException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		db = dbf.newDocumentBuilder();

		Document document = db.parse(new InputSource(new StringReader(xml)));
		// ������нڵ㣬�ݹ�����ڵ�
		NodeList employees = document.getChildNodes();
		// ��xml�ļ�����
		return (Map) parserXml(employees, class_type);
	}

	public static String FileToXMLString(String path) {
		return FileToXMLString(path, "GBK");
	}

	public static String FileToXMLString(String path, String encoding) {
		InputStream input = null;
		try {
			StringBuffer sb = new StringBuffer();

			try {
				input = XmlUtils.class.getResourceAsStream(path);
			} catch (Exception e) {
				;
			}
			if (input == null) {
				try {
					input = new FileInputStream(path);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}

			InputStreamReader read = new InputStreamReader(input, encoding);// ���ǵ������ʽ
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				//System.out.println(lineTxt);
				sb.append(lineTxt);
			}
			read.close();

			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {

					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * XMLת����
	 * 
	 * @param employees
	 *            Ҫ�����Ľڵ㼯��
	 * @param class_type
	 *            ��Ҫ���صĶ������ͣ����û���ӽڵ��򷵻�string���͵�ֵ
	 * @return ����map��String
	 * @throws DOMException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings({ "unchecked" })
	private static Object parserXml(NodeList employees, Class<? extends Map> class_type)
			throws DOMException, InstantiationException, IllegalAccessException {
		Map map = class_type.newInstance();
		String value = null;
		for (int i = 0; i < employees.getLength(); i++) {

			// ȡ��һ���ڵ�
			Node employee = employees.item(i);

			if (employee.getNodeType() != 8)
				if (employee.getNodeType() == 1 || employee.getNodeValue() == null
						|| employee.getNodeValue().trim().isEmpty()) {
					if (employee.hasChildNodes() || employee.hasAttributes()) {
						Object sub_map = parserXml(employee.getChildNodes(), class_type);
						String name = employee.getNodeName();
						NamedNodeMap attributes = employee.getAttributes();
						for (int attribute_index = 0; attribute_index < attributes.getLength(); attribute_index++) {
							Node attribute = attributes.item(attribute_index);
							String attribute_nameString = attribute.getNodeName().trim();
							if (attribute_nameString.equalsIgnoreCase("name")) {
								name = (attribute.getNodeValue() == null || attribute.getNodeValue().trim().isEmpty())
										? name : attribute.getNodeValue().trim();
							} else {
								if (sub_map instanceof Map) {
									((Map) sub_map).put(attribute_nameString,
											(attribute.getNodeValue() == null
													|| attribute.getNodeValue().trim().isEmpty()) ? null
															: attribute.getNodeValue().trim());
								} else {
									String sub_value = (String) sub_map;
									sub_map = class_type.newInstance();
									// ((Map) sub_map).put("", sub_value);
									((Map) sub_map).put(name, sub_value);
									((Map) sub_map).put(attribute_nameString,
											(attribute.getNodeValue() == null
													|| attribute.getNodeValue().trim().isEmpty()) ? null
															: attribute.getNodeValue().trim());
								}
							}
						}
						if (sub_map != null) {
							Object sub_map_list = map.get(name);

							if (sub_map_list == null) {
								map.put(name, sub_map);
							} else {
								if (!(sub_map_list instanceof List)) {
									sub_map_list = new ArrayList();
									((List) sub_map_list).add(map.get(name));
									map.put(name, sub_map_list);
								}
								((List) sub_map_list).add(sub_map);
							}
						}
					}
				} else {
					value = ((employee.getNodeValue().trim().isEmpty()) ? null : employee.getNodeValue().trim());
				}
		}
		if (map.size() <= 0 && value != null) {
			return value;
		} else {
			if (value != null) {
				map.put("", value);
			}
			return map;
		}
	}

	public static String map2Xml(Map map) {
		StringBuffer sbf = new StringBuffer();
		map2Xml(sbf, map);
		return xmlFormat(sbf.toString());
	}

	private static String xmlFormat(String xml) {
		StringWriter writer;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;

			db = dbf.newDocumentBuilder();

			Document doc = db.parse(new InputSource(new StringReader(xml)));
			DOMSource source = new DOMSource(doc);
			writer = new StringWriter();
			Result result = new StreamResult(writer);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
			return (writer.getBuffer().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void map2Xml(StringBuffer buffer, Map map) {
		Set set = map.entrySet();
		Iterator iterator = set.iterator();
		StringBuffer childSbf = new StringBuffer();
		while (iterator.hasNext()) {
			Entry e = (Entry) iterator.next();
			String key = (String) e.getKey();

			Object value = e.getValue();
			if (value instanceof List) {
				List list = (List) value;
				for (int j = 0; j < list.size(); j++) {
					childSbf.append("<").append(key).append(">");
					Map valueMap = (Map) list.get(j);
					map2Xml(childSbf, valueMap);
					childSbf.append("</").append(key).append(">");
				}
			} else if (value instanceof Map) {
				if (key != null) {
					childSbf.append("<").append(key).append(">");
				}
				Map valueMap = (Map) value;
				map2Xml(childSbf, valueMap);
				if (key != null) {
					childSbf.append("</").append(key).append(">");
				}
			} else {
				if (key != null) {
					childSbf.append("<").append(key).append(">");
				}
				if (null != value) {
					childSbf.append(value);
				} else {
					childSbf.append("");
				}
				if (key != null) {
					childSbf.append("</").append(key).append(">");
				}
			}

		}
		buffer.append(childSbf);
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			ParserConfigurationException, SAXException, IOException {
		Map map = XML2MAP("./conf/MTRouter.xml");
		System.out.println(map);
		// Map map1 = (Map) map.get("Config");
		// List<Map> list = (List<Map>) map1.get("Rule");
		// map1 = (Map) ((Map)list.get(0)).get("Conditions");
		// map1 = (Map)map1.get("User");
		// System.out.println(map1.get("action"));
		// System.out.println(map1.get("User"));
		// System.out.println(map2Xml(map));
	}
}

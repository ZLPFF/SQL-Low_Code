package com.sunwayworld.cloud.module.lcdp.apiintegration.webservice.util;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunwayworld.framework.exception.core.ApplicationRuntimeException;
import com.sunwayworld.framework.utils.CollectionUtils;
import com.sunwayworld.framework.utils.ObjectUtils;
import com.sunwayworld.framework.utils.StringUtils;

/**
 * @author wangda@sunwayworld.com
 * @date 2022/6/20 16:44
 * @description
 */
public class EsbXmlUtils {
    private static String defaultDocumentBuilderFactory = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";
    private static boolean namespaceAware = true;

    public static Map<String, Object> xmlToMap(String xmlStr) {
        return xmlToMap(xmlStr, new HashMap<>());
    }

    public static Map<String, Object> xmlToMap(String xmlStr, Map<String, Object> result) {
        Document doc = parseXml(xmlStr);
        Element root = getRootElement(doc);
        root.normalize();
        return xmlToMap(root, result);
    }

    public static Document parseXml(String xmlStr) {
        if (StringUtils.isBlank(xmlStr)) {
            throw new IllegalArgumentException("XML content string is empty !");
        } else {
            xmlStr = cleanInvalid(xmlStr);
            return readXML(getReader(xmlStr));
        }
    }

    public static Element getRootElement(Document doc) {
        return null == doc ? null : doc.getDocumentElement();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> xmlToMap(Node node, Map<String, Object> result) {
        if (null == result) {
            result = new HashMap<>();
        }

        NodeList nodeList = node.getChildNodes();
        int length = nodeList.getLength();

        for (int i = 0; i < length; ++i) {
            Node childNode = nodeList.item(i);
            if (isElement(childNode)) {
                Element childEle = (Element) childNode;
                Object value = result.get(childEle.getNodeName());
                Object newValue;
                if (childEle.hasChildNodes()) {
                    Map<String, Object> map = xmlToMap(childEle);
                    if (!CollectionUtils.isEmpty(map)) {
                        newValue = map;
                    } else {
                        newValue = childEle.getTextContent();
                    }
                } else {
                    newValue = childEle.getTextContent();
                }

                if (null != newValue) {
                    if (null != value) {
                        if (value instanceof List) {
                            ((List<Object>) value).add(newValue);
                        } else {
                            result.put(childEle.getNodeName(), Arrays.asList(value, newValue));
                        }
                    } else {
                        result.put(childEle.getNodeName(), newValue);
                    }
                }
            }
        }

        return result;
    }

    public static Map<String, Object> xmlToMap(Node node) {
        return xmlToMap(node, new HashMap<>());
    }

    public static boolean isElement(Node node) {
        return null != node && 1 == node.getNodeType();
    }

    public static StringReader getReader(CharSequence str) {
        return null == str ? null : new StringReader(str.toString());
    }

    public static String cleanInvalid(String xmlContent) {
        return xmlContent == null ? null : xmlContent.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
    }

    public static Document readXML(Reader reader) {
        return readXML(new InputSource(reader));
    }

    public static Document readXML(InputSource source) {
        DocumentBuilder builder = createDocumentBuilder();

        try {
            return builder.parse(source);
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    public static DocumentBuilder createDocumentBuilder() {
        try {
            return createDocumentBuilderFactory().newDocumentBuilder();
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory;
        if (!ObjectUtils.isEmpty(defaultDocumentBuilderFactory)) {
            factory = DocumentBuilderFactory.newInstance(defaultDocumentBuilderFactory, null);
        } else {
            factory = DocumentBuilderFactory.newInstance();
        }

        factory.setNamespaceAware(namespaceAware);
        factory.setIgnoringElementContentWhitespace(true);
        return disableXXE(factory);
    }

    public static String toStr(Node doc) {
        return toStr(doc, false);
    }

    public static String toStr(Document doc) {
        return toStr((Node) doc);
    }

    public static String toStr(Node doc, boolean isPretty) {
        return toStr(doc, "UTF-8", isPretty);
    }

    public static String toStr(Document doc, boolean isPretty) {
        return toStr((Node) doc, isPretty);
    }

    public static String toStr(Node doc, String charset, boolean isPretty) {
        return toStr(doc, charset, isPretty, true);
    }

    public static String toStr(Document doc, String charset, boolean isPretty) {
        return toStr((Node) doc, charset, isPretty);
    }

    public static String toStr(Node doc, String charset, boolean isPretty, boolean omitXmlDeclaration) {
        StringWriter writer = new StringWriter();

        try {
            write(doc, (Writer) writer, charset, isPretty ? 2 : 0, omitXmlDeclaration);
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }

        return writer.toString();
    }

    public static void write(Node node, Writer writer, String charset, int indent, boolean omitXmlDeclaration) {
        transform(new DOMSource(node), new StreamResult(writer), charset, indent, omitXmlDeclaration);
    }

    public static void transform(Source source, Result result, String charset, int indent, boolean omitXmlDeclaration) {
        TransformerFactory factory = TransformerFactory.newInstance();

        try {
            Transformer xformer = factory.newTransformer();
            if (indent > 0) {
                xformer.setOutputProperty("indent", "yes");
                xformer.setOutputProperty("doctype-public", "yes");
                xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            }

            if (!StringUtils.isBlank(charset)) {
                xformer.setOutputProperty("encoding", charset);
            }

            if (omitXmlDeclaration) {
                xformer.setOutputProperty("omit-xml-declaration", "yes");
            }

            xformer.transform(source, result);
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    public static String removeWhiteSpace(String xml) {
        Pattern pa = Pattern.compile(">(\\s*|\n|\t|\r)<");
        Matcher matcher = pa.matcher(xml);
        return matcher.replaceAll("><");
    }

    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        try {
            String feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
        }

        return dbf;
    }

    /**
     * @param jsonObj
     * @return
     */
    public static String json2xmlString(JSONObject jsonObj) {
        StringBuffer buff = new StringBuffer();
        JSONObject tempObj = null;
        JSONArray tempArr = null;
        buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buff.append("<root>");
        buff.append("<element>");
        for (String temp : jsonObj.keySet()) {
            buff.append("<" + temp.trim() + ">");
            jsonObj.get(temp);
            if (jsonObj.get(temp) instanceof JSONObject) {
                tempObj = (JSONObject) jsonObj.get(temp);
                buff.append(json2xmlString(tempObj));
            } else if (jsonObj.get(temp) instanceof JSONArray) {
                tempArr = (JSONArray) jsonObj.get(temp);
                if (tempArr.size() > 0) {
                    for (int i = 0; i < tempArr.size(); i++) {
                        tempObj = (JSONObject) tempArr.get(0);
                        buff.append(json2xmlString(tempObj));
                    }
                }
            } else {
                String tempStr = jsonObj.get(temp).toString();
                buff.append(tempStr.trim());
            }
            buff.append("</" + temp.trim() + ">");
        }
        buff.append("</element></root>");
        return buff.toString();
    }

    public static final boolean isXml(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        try {
            DocumentHelper.parseText(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

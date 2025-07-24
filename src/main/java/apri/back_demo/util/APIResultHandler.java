package apri.back_demo.util;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import apri.back_demo.exception.TourAPIVKError;



    // how to use
    // String msg = tourApiCall.apiLocationBasedList(posX, posY, 50000);
    // APIResultHandler handler = new APIResultHandler();

    // System.out.println(msg);
    //List<Map<String, Object>> list = handler.returnAsList(msg);

    // 

    // TODO : Exception based on tourapi errorcode 


public class APIResultHandler {
    //constructor... 
    public APIResultHandler(){}


    /**
     * API response comes with a list of maps. 
     * JSON string from api call -> List<Map<String, Object>>
     * In current settings, upto 50 maps per call.
     * @param responseString
     * @return
     * @throws TourAPIVKError
     */
    public List<Map<String, Object>> returnAsList(String responseString) throws TourAPIVKError{
        ObjectMapper mapper = new ObjectMapper();

        if (isXML(responseString)) {
            parseXmlError(responseString); 
            // Expected to throw exception with proper info
        }

        if (!isJson(responseString)) {
            throw new IllegalArgumentException("Invalid response format: not JSON or XML");
        }

        try {
            JsonNode root = mapper.readTree(responseString);
            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

            if (!itemsNode.isArray()) {
                throw new IllegalStateException("'items.item' is not an array in JSON");
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (JsonNode item : itemsNode) {

                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = mapper.convertValue(item, Map.class);
                list.add(itemMap);
                
            }

            return list;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
    

    /**
     * @param body
     * @return true if body starts with "<"
     */ 
    public boolean isXML(String body){
        return body.trim().startsWith("<");
    }
    /**
     * 
     * @param body
     * @return true if body can be mapped into json
     */
    private boolean isJson(String body){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(body);
            return true;
        } catch (JsonMappingException e) {
            return false;
        } catch (JsonProcessingException e) {
            return false; 
        }
    }

    private void parseXmlError(String xml) throws TourAPIVKError{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            //throw sth
        }
        InputSource is = new InputSource(new StringReader(xml));
        Document doc=null;
        try {
            doc = builder.parse(is);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //throw sth
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // throw sth
        }
        doc.getDocumentElement().normalize();

        String errMsg = getTagValue("errMsg",doc);
        String authMsg = getTagValue("returnauthMsg",doc);
        String reasonCode = getTagValue("returnReasonCode",doc);
        Integer reasonCodeInt = Integer.valueOf(reasonCode);

        System.err.printf("! API Error:\n  Code: %s\n  Message: %s\n  Auth Message: %s\n",
                          reasonCode, errMsg, authMsg);
        throw new TourAPIVKError(errMsg+authMsg,reasonCodeInt);

        //change this when integrating with main server
    }
    private static String getTagValue(String tag, Document doc) {
        NodeList list = doc.getElementsByTagName(tag);
        if (list.getLength() > 0) {
            Node node = list.item(0);
            return node.getTextContent();
        }
        return null;
    }
}

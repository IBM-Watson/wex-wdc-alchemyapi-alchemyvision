package com.ibm.watson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.json4j.JSONObject;



@Path("/getRankedImageKeywords")
public class AlchemyVision {
    String ORIGINAL_SERVICE_URL = "https://access.alchemyapi.com";
    String API_PATH = "/calls/image/ImageGetRankedImageKeywords";
    String SERVICE_NAME = "user-provided";
    String XML_DEC = "<?xml version='1.0' encoding='UTF-8'?>";
    
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public String getRankedImageKeywords(String incomingPOSTData) {
    /* We will pass the POST data we receive directly on to the service, so there
    *  is no need to break it into individual parameters and URL decode their
    *  values via a param annotation.  This should save us a bit of cpu and mem
    *  as well, since we are dealing with binary data (images).
    */
        HttpURLConnection conn = null;
        String serviceURL = "";
           
        try {

            // 'VCAP_SERVICES' contains all the details of services bound to this application
            JSONObject serviceInfo = new JSONObject(System.getenv("VCAP_SERVICES"));
            
            // Get the Service Credentials for AlchemyAPI
            JSONObject credentials = serviceInfo.getJSONArray(SERVICE_NAME)
                    .getJSONObject(0).getJSONObject("credentials");
            try {
                serviceURL = credentials.getString("url");
            } catch (Exception e) {}
            // If we didn't find a URL for the AlchemyAPI service in VCAP_SERVICES,
            // use the original.
            if ("".equals(serviceURL)) {
                serviceURL = ORIGINAL_SERVICE_URL;
            }
            
            String apikey = credentials.getString("apikey");
            
            // Prepare the outgoing POST data
            // It is expected that the client will supply all necessary parameters
            // except the apikey, which was specified when the service was created.
            String outgoingPOSTData = incomingPOSTData + "&apikey=" + URLEncoder.encode(apikey, "UTF-8");
            
            // Prepare the HTTP connection to the service
            conn = (HttpURLConnection) new URL(serviceURL + API_PATH).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            // make the connection
            conn.connect();
            
            // send the POST request
            DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            output.writeBytes(outgoingPOSTData);
            output.flush();
            output.close();
            
            // Read the response from the service
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            //System.out.println("The result is: " + sb.toString());
            
            // Return the response from the service
            return sb.toString();
        } catch(Exception e){
            e.printStackTrace();
            return XML_DEC + "<results><status>ERROR</status><statusInfo>Bluemix " + e.getClass().getName() + ": " + e.getMessage() + "</statusInfo></results>";
        }finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return XML_DEC + "<results><status>ERROR</status><statusInfo>Bluemix " + e.getClass().getName() + ": " + e.getMessage() + "</statusInfo></results>";
            }
        }
        
    }

}
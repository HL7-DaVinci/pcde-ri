package ca.uhn.fhir.jpa.starter;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler {
    String baseUrl;
    private final String USER_AGENT = "Mozilla/5.0";
    public RequestHandler(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String sendGet(String endPoint, String params) throws Exception {
        // 'https://davinci-pcde-ri.logicahealth.org/fhir/Patient?given='+given+'&family='+family+'&birthdate='+bdate
        String url = baseUrl + endPoint + "/" + params;
        System.out.println("Sending Get to: " + url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        //con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        return response.toString();

  }
}

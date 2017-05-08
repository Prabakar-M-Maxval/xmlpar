/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author prabakar
 */
public class OPSAPIToken {

   // static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
   // static Pattern countryCodePattern = Pattern.compile("^([A-Za-z]{0,4})", Pattern.CASE_INSENSITIVE);
   // static Matcher matcher;
    //static String countryCode;
    static List<String> patentNos;

    public static String getEPOAccessToken() throws IOException {
        Gson gson = new Gson();
        String accessToken = null;

        URL url = new URL("https://ops.epo.org/3.1/auth/accesstoken");

        HttpsURLConnection connection = (HttpsURLConnection) url
                .openConnection();

        connection.setReadTimeout(60 * 1000);
        connection.setConnectTimeout(60 * 1000);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String authorization = "Eo2RlE5B8AFKTbrfEEPfn7kYv7eNLKW2:qlojpopNYxtbIjOX";
        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(authorization.getBytes());

        connection.setRequestProperty("Authorization", encodedAuth);
        String body = "grant_type=client_credentials";
        connection.setDoInput(true);
        connection.setDoOutput(true);
        byte[] buffer = body.getBytes(StandardCharsets.US_ASCII);

        try (
                OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(buffer, 0, buffer.length);
        }
        // connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("response code" + responseCode);

        BufferedReader bufferedReader = null;
        String output = "";
        Map<String, Object> responseToken = new HashMap<>();
        if (responseCode == 200) {
            bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            StringBuilder builder = new StringBuilder();
            while ((output = bufferedReader.readLine()) != null) {
                builder.append(output);
            }
            responseToken = (Map<String, Object>) gson.fromJson(builder.toString(), responseToken.getClass());
            System.out.println("json output" + responseToken);
            accessToken = (String) responseToken.get("access_token");
            System.out.println("access token Key " + responseToken.get("access_token"));
            return accessToken;
        } else {
            return null;
        }
    }

    public static List<String> getMultipleKindCode(String patentNo, String cntryCode) throws IOException {
        String getToken = getEPOAccessToken();
        Properties prop = new Properties();       

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("ExtractBiblio/kindcode.properties");

        Properties properties = new Properties();
        properties.load(input);

        String countryKindCode = properties.getProperty(cntryCode);
        List<String> items = Arrays.asList(countryKindCode.split("\\s*,\\s*"));
        String tempPatentNo = patentNo;
        patentNos = new ArrayList<>();
        for (String item : items) {
            patentNo = patentNo + "." + item;
            String opsURL1 = "https://ops.epo.org/rest-services/published-data/publication/epodoc/";
            URL urlFinall = new URL(opsURL1 + patentNo + "/biblio");
            System.out.println("url" + urlFinall);
            HttpsURLConnection connectionGet = (HttpsURLConnection) urlFinall
                    .openConnection();
            String authorizationHeader = "Bearer " + getToken;
            System.out.println("token"+getToken);
            connectionGet.setRequestProperty("Authorization", authorizationHeader);
            connectionGet.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connectionGet.setRequestMethod("GET");
            int responseCode = connectionGet.getResponseCode();
            if (responseCode != 404) {
                patentNos.add(patentNo);
            }
            patentNo = tempPatentNo;
        }       
        System.out.println("bs" + patentNos);
        return patentNos;
    }

}

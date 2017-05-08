/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author prabakar
 */
public class ClaimParseResult extends HttpServlet {
    
    
    static List<String> independentClaims = new ArrayList();
    static List<String> dependentClaims = new ArrayList<>();
    static Map<Integer, Set<Integer>> claims = new TreeMap<>();
    static Map<Integer, Set<String>> allClaims = new TreeMap<>();
    
    static Pattern childClaim = Pattern.compile("\\bclaims?\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    static Pattern indDepClaim = Pattern.compile("(.*\\b(in claim|in claims|to claim|to claims)\\b.*?)", Pattern.CASE_INSENSITIVE);
    //static Pattern checkKindCodePattern = Pattern.compile("([a-zA-z])", Pattern.CASE_INSENSITIVE);
    static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
    static Matcher matcher;
    static String kindCode;
    static String countryCode;
    static String patORPubNo;

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
       out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
        response.setContentType("text/html;charset=UTF-8");
        request.getParameter("PatPubNumber");
        
        
        String inputNo = request.getParameter("PatPubNumber").trim();
        countryCode = ValidateInput.splitPatPubNo.get("countrycode").trim();
        patORPubNo = ValidateInput.splitPatPubNo.get("patpubno").trim();
        kindCode = ValidateInput.splitPatPubNo.get("kindcode").trim();
        System.out.println("gb"+countryCode+patORPubNo+kindCode);
        String patentNo = countryCode+patORPubNo + "." + kindCode;        

        //String patentNo = request.getParameter("PatPubNumber").trim();
        
                
        String getToken = (String) request.getAttribute("token");
        
        try {

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            String opsURL1 = "https://ops.epo.org/rest-services/published-data/publication/epodoc/";
            URL urlFinall = new URL(opsURL1 + patentNo + "/claims");
            HttpsURLConnection connectionGet = (HttpsURLConnection) urlFinall
                    .openConnection();
            String authorizationHeader = "Bearer " + getToken;
            connectionGet.setRequestProperty("Authorization", authorizationHeader);
            connectionGet.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connectionGet.setRequestMethod("GET");
            int responseCode = connectionGet.getResponseCode();
            if (responseCode != 404 && responseCode != 400) {
                InputStream inputstreamFinal = connectionGet.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputstreamFinal, "UTF-8"));
                String tempXml = "";
                String XmlString = "";

                while ((tempXml = bufferedReader.readLine()) != null) {
                    XmlString = XmlString.concat(tempXml);
                }

                InputSource xmlSource = new InputSource();
                xmlSource.setCharacterStream(new StringReader(XmlString));
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document xmlDocument = builder.parse(xmlSource);

                xmlDocument.getDocumentElement().normalize();
                System.out.println("Root element :" + xmlDocument.getDocumentElement().getNodeName());
                
                 NodeList claimList = (NodeList) xPath.compile("//claims[@lang =\"EN\"]//claim-text//text()").evaluate(xmlDocument, XPathConstants.NODESET);
                    
                    for (int i = 0; i < claimList.getLength(); i++) {
                        String dummy = claimList.item(i).getNodeValue();

                      
                        matcher = indDepClaim.matcher(dummy);
                        Boolean isAvailable = matcher.find(1);
                        if (isAvailable) {
                            dependentClaims.add("<p>" + claimList.item(i).getNodeValue());
                        } else {
                            independentClaims.add("<p>" + claimList.item(i).getNodeValue());
                        }
                    }

                    for (int i = 0; i < claimList.getLength(); i++) {
                        String text = claimList.item(i).getNodeValue();
                        int claimNo = Integer.parseInt(text.replaceFirst("(?s)^(\\d*).*$", "0$1"), 10);
                        if (claimNo == 0) { //dont care
                            continue;
                        }
                        claims.put(claimNo, new TreeSet());

                        matcher = childClaim.matcher(text);
                        while (matcher.find()) {
                            int refClaimNo = Integer.parseInt(matcher.group(1));
                            if (!claims.containsKey(refClaimNo)) { //Dont care
                                //claims.put(refClaimNo, new TreeSet());
                                allClaims.put(refClaimNo, new TreeSet());
                            }
                            allClaims.get(refClaimNo).add(text);
                        }
                        allClaims.put(claimNo, new TreeSet());
                        allClaims.get(claimNo).add(text);

                    }

                    allClaims.entrySet().stream().filter((e) -> (e.getValue().size() > 1)).map((e) -> {
                        out.println(e.getKey());
                        return e;
                    }).forEach((e) -> {
                        e.getValue().stream().forEach((s) -> {
                            out.println("<p>"+ s+"<p>");
                        });
                    });

                    out.println("<b><u>"+ "<p>"+"Dependent Claims</b></u>"+dependentClaims);
                    out.println("<b><u>"+ "<p>"+"Independent Claims</b></u>"+independentClaims);
                    out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");

                
                } else {
                out.println("request is invalid: Invalid reference. OR Document not available in EPO Service");
            }

        } catch (SAXException | ParserConfigurationException | IOException e) {
        } catch (XPathExpressionException ex) {
            Logger.getLogger(ClaimParseResult.class.getName()).log(Level.SEVERE, null, ex);
        }
                
}
}

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
public class FamilyParseResult extends HttpServlet {
    //static Pattern checkKindCodePattern = Pattern.compile("([a-zA-z])", Pattern.CASE_INSENSITIVE);
   // static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
   // static Matcher matcher;
    
    static String countryCode;
    static String patORPubNo;
    static String kindCode;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
        response.setContentType("text/html;charset=UTF-8");
       

       // String patentNo = request.getParameter("PatPubNumber").trim();
        
        countryCode = ValidateInput.splitPatPubNo.get("countrycode").trim();
        patORPubNo = ValidateInput.splitPatPubNo.get("patpubno").trim();
        kindCode = ValidateInput.splitPatPubNo.get("kindcode").trim();
        System.out.println("gb"+countryCode+patORPubNo+kindCode);
        
        String patentNo = countryCode+patORPubNo;
        
        //NO KIND CODE
              
        String getToken = (String) request.getAttribute("token");
        List<FamilyInfo> familyList;
        familyList = new ArrayList<>();
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            String opsURL1 = "https://ops.epo.org/3.1/rest-services/family/publication/epodoc/";
            URL urlFinall = new URL(opsURL1 + patentNo);
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

                NodeList familyMembers;
                familyMembers = (NodeList) xPath.compile("//family-member/publication-reference/document-id[@document-id-type=\"docdb\"]//text()").evaluate(xmlDocument, XPathConstants.NODESET);

                ArrayList mainFamily = new ArrayList();
                ArrayList cnt = new ArrayList();
                for (int i = 0; i < familyMembers.getLength(); i++) {
                    String _a = familyMembers.item(i).getNodeValue();
                    if (_a.trim().length() > 0) {
                        mainFamily.add(familyMembers.item(i).getNodeValue());
                    }
                }

                for (int i = 0; i < mainFamily.size(); i += 4) {

                    String countryCode = (String) mainFamily.get(i);
                    String patPubNo = (String) mainFamily.get(i + 1);
                    String kindCode = (String) mainFamily.get(i + 2);
                    String pubORIssueDate = (String) mainFamily.get(i + 3);
                    cnt.add(countryCode + patPubNo + kindCode + " | " + pubORIssueDate);
                }
                familyList.add(getFamilyInfo(cnt));

                familyList.stream().forEach((FamilyInfo familyInformation) -> {
                    out.println(familyInformation.toString());
                });

            } else {
                out.println("request is invalid: Invalid reference. Wrong epodoc format number");
            }

        } catch (SAXException | ParserConfigurationException | IOException e) {
        } catch (XPathExpressionException ex) {
            Logger.getLogger(FamilyParseResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static FamilyInfo getFamilyInfo(ArrayList familyNumbers) {
        FamilyInfo familyInfo = new FamilyInfo();
        String tempNumber = "";

        for (Object familyMemberInfo : familyNumbers) {
            tempNumber = tempNumber.concat((String) familyMemberInfo + "<p>");
        }
        familyInfo.setFamilyInfo(tempNumber);
        return familyInfo;
    }
}

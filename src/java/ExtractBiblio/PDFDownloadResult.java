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

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author prabakar
 */
public class PDFDownloadResult extends HttpServlet {

    static String countryCode;
    static String patORPubNo;
    static String kindCode;
    static HttpsURLConnection connectionGet;
    
    //static Pattern kindCodePattern = Pattern.compile("([a-zA-z])", Pattern.CASE_INSENSITIVE);
    //static Pattern kindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
    //static Pattern countryCodePattern = Pattern.compile("([a-zA-z]{2,})(\\d+)", Pattern.CASE_INSENSITIVE);
   // static Matcher matcher;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<InputStream> sources = new ArrayList<>();
        InputSource xmlSource = new InputSource();
        PrintWriter out = response.getWriter();
       // String patentNo = request.getParameter("PatPubNumber").trim();
        
        //String inputNo = request.getParameter("PatPubNumber").trim();
        countryCode = ValidateInput.splitPatPubNo.get("countrycode").trim();
        patORPubNo = ValidateInput.splitPatPubNo.get("patpubno").trim();
        kindCode = ValidateInput.splitPatPubNo.get("kindcode").trim();
        System.out.println("gb"+countryCode+patORPubNo+kindCode);
        //String patentNo = countryCode+patORPubNo + "." + kindCode;        
        
       if (kindCode != null && !kindCode.isEmpty()) {

            String getToken = (String) request.getAttribute("token");

            if (getToken != null && !getToken.isEmpty()) {
                try {
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    String finalPatPubNo = countryCode + "." + patORPubNo + "." + kindCode;
                    String opsURL1 = "https://ops.epo.org/rest-services/published-data/publication/docdb/";
                    URL firstPageImage = new URL(opsURL1 + finalPatPubNo + "/images");
                    System.out.println(firstPageImage);
                    connectionGet = (HttpsURLConnection) firstPageImage
                            .openConnection();
                    String authorizationHeader = "Bearer " + getToken;
                    connectionGet.setRequestProperty("Authorization", authorizationHeader);
                    connectionGet.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connectionGet.setRequestMethod("GET");
                    int responseCode = connectionGet.getResponseCode();
                    System.out.println("response Code from main" + responseCode);

                    if (responseCode != 404) {

                        InputStream inputstreamFinal = connectionGet.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputstreamFinal, "UTF-8"));

                        String tempXml = "";
                        String XmlString = "";

                        while ((tempXml = bufferedReader.readLine()) != null) {
                            XmlString = XmlString.concat(tempXml);
                        }
                        xmlSource.setCharacterStream(new StringReader(XmlString));
                        XPath xPath = XPathFactory.newInstance().newXPath();
                        Document xmlDocument = builder.parse(xmlSource);

                        xmlDocument.getDocumentElement().normalize();
                        System.out.println("Root element :" + xmlDocument.getDocumentElement().getNodeName());

                        String pageCountPath = "//document-instance[@desc='FullDocument']/@number-of-pages";
                        String pageCountValue = (String) xPath.compile(pageCountPath).evaluate(xmlDocument, XPathConstants.STRING);
                        System.out.println("file page count" + pageCountValue);

                        String contentType = connectionGet.getContentType();

                        System.out.println("Content-Type = " + contentType);
                        Integer totalPageCount = Integer.parseInt(pageCountValue);
                        
                        String pdfFileName = countryCode + patORPubNo + kindCode + ".pdf";
                        String contextPath = request.getServletContext().getRealPath("/");
                        try {
                            for (int i = 1; i <= 2; i++) {
                                String inputValue = countryCode + "/" + patORPubNo + "/" + kindCode;

                                String pdfUrl = "https://ops.epo.org/rest-services/published-data/images/" + inputValue + "/fullimage.pdf?Range=" + i;
                                URL pdfDownload = new URL(pdfUrl);
                                connectionGet = (HttpsURLConnection) pdfDownload.openConnection();
                                String authorizationHeader1 = "Bearer " + getToken;
                                connectionGet.setRequestProperty("Authorization", authorizationHeader1);
                                connectionGet.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                connectionGet.setRequestMethod("GET");
                                int pageResponseCode = connectionGet.getResponseCode();
                                System.out.println("individual page response code" + pageResponseCode);
                                if (pageResponseCode != 404) {
                                    sources.add(connectionGet.getInputStream());
                                }
                            }
                            PDFMergerUtility pdfMerger = new PDFMergerUtility();
                            pdfMerger.addSources(sources);

                            pdfMerger.setDestinationFileName(contextPath + pdfFileName);
                            try {
                                pdfMerger.mergeDocuments();
                            } catch (COSVisitorException ex) {
                                Logger.getLogger(PDFDownloadResult.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } finally {
                            // cleanup
                            sources.stream().forEach((source) -> {
                                IOUtils.closeQuietly(source);
                            });
                            out.println("Document Downloaded Successfully in below server Path" + "<p><b>" + contextPath + pdfFileName + "</b>");
                        }
                    } else {
                        out.println("No file to download. Server replied HTTP code: " + responseCode);
                    }

                } catch (ParserConfigurationException | SAXException | XPathExpressionException ex) {
                    Logger.getLogger(PDFDownloadResult.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                out.println("authentication failed");
            }
        } else {
            out.println("kind code empty");
        }
        //connectionGet.disconnect();
        out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
    }
}

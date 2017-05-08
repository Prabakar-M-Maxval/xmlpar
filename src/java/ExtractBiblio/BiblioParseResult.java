/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;

import static ExtractBiblio.OPSAPIToken.getMultipleKindCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import java.net.URL;
import java.text.ParseException;

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
public class BiblioParseResult extends HttpServlet {

    //static Pattern checkKindCodePattern = Pattern.compile("([a-zA-z])", Pattern.CASE_INSENSITIVE);
    //static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
   // static Pattern countryCodePattern = Pattern.compile("([a-zA-z]{2,})(\\d+)", Pattern.CASE_INSENSITIVE);
   // static Matcher matcher;
    static String kindCode;
    static String countryCode;
    static String patORPubNo;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
        response.setContentType("text/html;charset=UTF-8");
        //request.getParameter("PatPubNumber");

        String inputNo = request.getParameter("PatPubNumber").trim();
        countryCode = ValidateInput.splitPatPubNo.get("countrycode").trim();
        patORPubNo = ValidateInput.splitPatPubNo.get("patpubno").trim();
        kindCode = ValidateInput.splitPatPubNo.get("kindcode").trim();
        System.out.println("gb"+countryCode+patORPubNo+kindCode);
        String patentNo = countryCode+patORPubNo + "." + kindCode;
       
            if (countryCode.equalsIgnoreCase("EP")&& kindCode.isEmpty()) {
                List<String> ListOfPatentWithKindCode = getMultipleKindCode(inputNo,countryCode);
            }
        
        String getToken = (String) request.getAttribute("token");
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            String opsURL1 = "https://ops.epo.org/rest-services/published-data/publication/epodoc/";
            URL urlFinall = new URL(opsURL1 + patentNo + "/biblio");
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

                List<Biblio> bibList = new ArrayList<>();
                List<PriorityInfo> priorityList = new ArrayList<>();
                List<Classification> classification = new ArrayList<>();

                //getting biblio data
                String pubOrPatentNumber = xPath.compile("//publication-reference//document-id[@document-id-type=\"epodoc\"]/doc-number").evaluate(xmlDocument);
                String countryCodeXml = xPath.compile("//publication-reference//country").evaluate(xmlDocument);
                String kindCodeValue = xPath.compile("//publication-reference//kind").evaluate(xmlDocument);
                String pubOrIssueDate = xPath.compile("//publication-reference//date").evaluate(xmlDocument);
                if (pubOrIssueDate != null && !pubOrIssueDate.isEmpty()) {
                    String applicationNumber = xPath.compile("//application-reference//document-id[@document-id-type=\"epodoc\"]/doc-number").evaluate(xmlDocument);
                    String filingDate = xPath.compile("//application-reference//document-id[@document-id-type=\"epodoc\"]/date").evaluate(xmlDocument);
                    String InventionTitle = xPath.compile("//invention-title[@lang =\"en\"]").evaluate(xmlDocument);

                    bibList.add(getBiblio(pubOrPatentNumber, countryCodeXml, kindCodeValue, pubOrIssueDate, applicationNumber, filingDate, InventionTitle));

                    //getting priority Information doc type check
                    NodeList priorityNumbers = (NodeList) xPath.compile("//priority-claims//priority-claim//document-id[@document-id-type=\"epodoc\"]//doc-number//text()").evaluate(xmlDocument, XPathConstants.NODESET);
                    NodeList priorityDates = (NodeList) xPath.compile("//priority-claims//priority-claim//document-id[@document-id-type=\"epodoc\"]//date//text()").evaluate(xmlDocument, XPathConstants.NODESET);

                    //getting inventors and assignee
                    NodeList inventorsName = (NodeList) xPath.compile("//inventors//inventor[@data-format=\"epodoc\"]//name//text()").evaluate(xmlDocument, XPathConstants.NODESET);
                    NodeList assigneeName = (NodeList) xPath.compile("//applicants//applicant[@data-format=\"epodoc\"]//name//text()").evaluate(xmlDocument, XPathConstants.NODESET);

                    //getting internationalclassification ipcr
                    NodeList ipcrClassification = (NodeList) xPath.compile("//classifications-ipcr//text//text()").evaluate(xmlDocument, XPathConstants.NODESET);
                    NodeList usClassification = (NodeList) xPath.compile("//patent-classifications//patent-classification[classification-scheme/@office=\"US\"]//classification-symbol//text()").evaluate(xmlDocument, XPathConstants.NODESET);

                    //getting abstract
                    String abstractData = xPath.compile("//abstract").evaluate(xmlDocument);

                    //collect priority numbers
                    ArrayList priorityNumber = new ArrayList();
                    for (int i = 0; i < priorityNumbers.getLength(); i++) {
                        priorityNumber.add(priorityNumbers.item(i).getNodeValue());
                    }

                    //collect priority date
                    ArrayList priorityDate = new ArrayList();
                    for (int i = 0; i < priorityDates.getLength(); i++) {
                        priorityDate.add(priorityDates.item(i).getNodeValue());
                    }

                    priorityList.add(getPrioritiesInfo(priorityNumber, priorityDate));

                    //collect inventor name
                    ArrayList inventorList = new ArrayList();
                    for (int i = 0; i < inventorsName.getLength(); i++) {
                        inventorList.add(inventorsName.item(i).getNodeValue());
                    }

                    //collect assignee name
                    ArrayList assigneeList = new ArrayList();
                    for (int i = 0; i < assigneeName.getLength(); i++) {
                        assigneeList.add(assigneeName.item(i).getNodeValue());
                    }
                    //collect ipcr list
                    ArrayList ipcrList = new ArrayList();
                    for (int i = 0; i < ipcrClassification.getLength(); i++) {
                        ipcrList.add(ipcrClassification.item(i).getNodeValue());
                    }

                    //collect US classification
                    ArrayList usClassList = new ArrayList();
                    for (int i = 0; i < usClassification.getLength(); i++) {
                        usClassList.add(usClassification.item(i).getNodeValue());
                    }

                    classification.add(getAssigneeInventorInfo(inventorList, assigneeList, ipcrList, usClassList, abstractData));

                    //lets print biblio list information
                    bibList.stream().forEach((bib) -> {
                        out.println(bib.toString());
                    });

                    //lets print priority list
                    priorityList.stream().forEach((priInfo) -> {
                        out.println(priInfo.toString());
                    });

                    //lets print classification list
                    classification.stream().forEach((classi) -> {
                        out.println(classi.toString());
                    });
                    out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
                } else {
                    out.println("Given patent/Pub no not available in EPO");
                }
            } else {
                out.println("request is invalid: Invalid reference. Wrong epodoc format number");
            }

        } catch (SAXException | ParserConfigurationException | XPathExpressionException | IOException e) {
        } catch (ParseException ex) {
            Logger.getLogger(BiblioParseResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Biblio getBiblio(String pubOrPatentNumber, String countryCode, String kindCode, String pubOrIssueDate, String applicationNumber, String filingDate, String InventionTitle) throws ParseException {
        Biblio bib = new Biblio();
        bib.setpatPubNo(pubOrPatentNumber);
        bib.setcountryCode(countryCode);
        bib.setkindcode(kindCode);
        bib.setpubOrIssueDate(pubOrIssueDate);
        bib.setApplicantionNumber(applicationNumber);
        bib.setFilingDate(filingDate);
        bib.setTitle(InventionTitle);
        return bib;
    }

    private static PriorityInfo getPrioritiesInfo(ArrayList priorityNumber, ArrayList priorityDate) {
        PriorityInfo priorityInfo = new PriorityInfo();
        String tempPriority = "";
        String tempPriorityDate = "";
        String priorityNos = "";
        String priorityDates = "";

        for (Object priorityNumber1 : priorityNumber) {

            tempPriority = tempPriority.concat(priorityNumber1 + "|");
            priorityNos = tempPriority.substring(0, tempPriority.length() - 1);
        }

        for (Object priorityDate1 : priorityDate) {
            tempPriorityDate = tempPriorityDate.concat(priorityDate1 + "|");
            priorityDates = tempPriorityDate.substring(0, tempPriorityDate.length() - 1);
        }

        priorityInfo.setPriorityNo(priorityNos);
        priorityInfo.setPriorityDate(priorityDates);
        return priorityInfo;
    }

    private static Classification getAssigneeInventorInfo(ArrayList inventorName, ArrayList AssigneeName, ArrayList ipcrNumber, ArrayList usClassification, String abstractValue) {

        Classification classification = new Classification();
        String tempAname = "";
        String tempIname = "";
        String tempIpcr = "";
        String tempUsClassification = "";

        for (Object inventorName1 : inventorName) {
            tempIname = tempIname.concat(inventorName1 + "|");
            tempIname = tempIname.substring(0, tempIname.length() - 1);
        }
        for (Object AssigneeName1 : AssigneeName) {
            tempAname = tempAname.concat(AssigneeName1 + "|");
            tempAname = tempAname.substring(0, tempAname.length() - 1);
        }

        for (Object ipcrNumber1 : ipcrNumber) {
            tempIpcr = tempIpcr.concat(ipcrNumber1 + "|").replaceAll("\\s+", "");
            tempIpcr = tempIpcr.substring(0, tempIpcr.length() - 1);
        }

        for (Object usClassification1 : usClassification) {
            tempUsClassification = tempUsClassification.concat(usClassification1 + "|");
            tempUsClassification = tempUsClassification.substring(0, tempUsClassification.length() - 1);
        }

        classification.setInventorName(tempIname);
        classification.setAssigneeName(tempAname);
        classification.setInternationalClassification(tempIpcr);
        classification.setUsClassification(tempUsClassification);
        classification.setAbstractData(abstractValue);
        return classification;
    }

}

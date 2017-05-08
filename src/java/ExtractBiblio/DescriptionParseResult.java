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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author prabakar
 */
public class DescriptionParseResult extends HttpServlet {

    static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);

    static Matcher matcher;
    static String kindCode;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");
        response.setContentType("text/html;charset=UTF-8");
        request.getParameter("PatPubNumber");

        String patentNo = request.getParameter("PatPubNumber").trim();
        matcher = checkKindCodePattern.matcher(patentNo);
        Boolean isAvailable = matcher.find();
        if (isAvailable) {
            //Insert a Dot "US8069506.B2" if Kind code Passing
            patentNo = matcher.group(1);
            kindCode = matcher.group(2);
            patentNo = patentNo + "." + kindCode;
        }

        String getToken = (String) request.getAttribute("token");
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            String opsURL1 = "https://ops.epo.org/rest-services/published-data/publication/epodoc/";
            URL urlFinall = new URL(opsURL1 + patentNo + "/description");
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
                List<Description> ftDes = new ArrayList<>();

                String description = xPath.compile("//description[@lang =\"EN\"]").evaluate(xmlDocument);
                String fullText = null;
                ftDes.add(getDescription(description));

                ftDes.stream().forEach((ftdesc) -> {
                    out.println(ftdesc.toString());
                });
                out.println("<a href = 'Home.html' style=\" text-align:left; float:right; width:120px;\">Return Home</a>");

            } else {
                out.println("request is invalid: Getting Response Code from service" + responseCode);
            }
        } catch (SAXException | XPathExpressionException | ParserConfigurationException ex) {
            Logger.getLogger(DescriptionParseResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Description getDescription(String description) {
        Description ftd = new Description();
        ftd.setDescription(description);
        return ftd;
    }
}

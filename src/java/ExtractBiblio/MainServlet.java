/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author prabakar
 */
public class MainServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String patPubNo = request.getParameter("PatPubNumber");
        out.println(patPubNo);       

        if (request.getParameter("biblio") != null) {
            if (ValidateInput.checkInput1(patPubNo)) {
                
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("ParseResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }
        } else if (request.getParameter("claims") != null) {

            if (ValidateInput.checkInput1(patPubNo)) {
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("ClaimParseResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }
   
        }
        else if (request.getParameter("pdf") != null) {

            if (ValidateInput.checkInput1(patPubNo)) {
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("PDFDownloadResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }   
        }
        
         else if (request.getParameter("family") != null) {

            if (ValidateInput.checkInput1(patPubNo)) {
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("FamilyParseResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }
   
        }
        else if (request.getParameter("image") != null) {

            if (ValidateInput.checkInput(patPubNo)) {
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("ImageDownloadResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }
   
        }
        else if (request.getParameter("ft") != null) {

            if (ValidateInput.checkInput(patPubNo)) {
                String getToken = OPSAPIToken.getEPOAccessToken();
                if (getToken != null && !getToken.isEmpty()) {
                    request.setAttribute("token", getToken);
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("DescriptionParseResult");
                    requestDispatcher.forward(request, response);
                } else {
                    RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                    requestDispatcher.forward(request, response);
                }

            } else {
                RequestDispatcher requestDispatcher = request.getRequestDispatcher("info.jsp");
                requestDispatcher.forward(request, response);
            }
   
        }

    }

}

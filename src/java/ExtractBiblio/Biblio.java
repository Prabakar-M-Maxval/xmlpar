/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;

/**
 *
 * @author prabakar
 */
public class Biblio {

    private String patOrPubNumber;
    private String countryCode;
    private String kindCode;
    private String pubOrIssueDate;
    private String applicationNumber;
    private String filingDate;
    private String title;            
    

    public String getPatPubNo() {
        return patOrPubNumber;
    }

    public void setpatPubNo(String patOrPubNo) {
        this.patOrPubNumber = patOrPubNo;
    }
    
    public String getcountryCode() {
        return countryCode;
    }

    public void setcountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
      public String getkindcode() {
        return kindCode;
    }

    public void setkindcode(String kindcode) {
        this.kindCode = kindcode;
    }
    
      public String getPubOrIssueDate() {
        return pubOrIssueDate;
    }

    public void setpubOrIssueDate(String pubOrIssueDate) {
        this.pubOrIssueDate = pubOrIssueDate;
    }    

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicantionNumber(String applicantionNumber) {
        this.applicationNumber = applicantionNumber;
    }    

    public String getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle (String title){
        this.title = title;
        
    }

    @Override
    public String toString() {
        return "<b><u>Bibliographic Data::</b></u>"+ "<p>"+ "<b>Pat/Pub Number :.</b>" + this.patOrPubNumber +"<p>"
                +  "<b>Country Code :.</b>" + this.countryCode +"<p>"
                + "<b>Kind Code :.</b>" + this.kindCode +"<p>"
                +  "<b>pub/Issue Date :.</b>" + this. pubOrIssueDate +"<p>"
                +  "<b>Application Number :.</b>"+ this. applicationNumber +"<p>"
                + "<b>Filing Date :.</b>" +this.filingDate +"<p>"
                + "<b>Invention Title :.</b>" +this.title+"<p>" ;
    }

}

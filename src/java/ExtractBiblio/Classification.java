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
public class Classification {

    private String inventorName;
    private String assigneeName;
    private String ipcrCode;
    private String usClassification;
    private String abstractData;
    
    public String getInventorName() {
        return inventorName;
    }

    public void setInventorName(String inventorName) {
        this.inventorName = inventorName;
    }
    
    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
    
      public String getInternationalClassification() {
        return ipcrCode;
    }

    public void setInternationalClassification(String classificationInternational) {
        this.ipcrCode = classificationInternational;
    }
    
      public String getUsClassification() {
        return usClassification;
    }

    public void setUsClassification(String coClassification) {
        this.usClassification = coClassification;
    }
    
    public String getAbstractData() {
        return abstractData;        
    }

    public void setAbstractData(String abstractdata){
        this.abstractData = abstractdata;
    }
   

    @Override
    public String toString() {
        return "<b><u>Classification Data::</b></u>"+ "<p>"+ "<b>Inventor Name :.</b>" + this.inventorName +"<p>"
                +  "<b>Assignee Name :.</b>" + this.assigneeName +"<p>"
                + "<b>Classification IPCR code :.</b>" + this.ipcrCode +"<p>"
                +  "<b>USClassification :.</b>" + this. usClassification +"<p>"
                +  "<b>Abstract :.</b>"+ this. abstractData+"<p>";
                
    }

}

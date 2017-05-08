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
public class FamilyInfo {

    private String publicationNumber;
    
    public String getFamilyInfo() {
        return publicationNumber;
    }

    public void setFamilyInfo(String pubNo) {
        this.publicationNumber = pubNo;
    } 
      
    @Override
    public String toString() {
        return "<b><u>Family Info::</b></u>" +"<p>"+ "<b>Family Members :.</b>" + this.publicationNumber +"<p>" ;
        
    }

}

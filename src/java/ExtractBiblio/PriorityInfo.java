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
public class PriorityInfo {

    private String priorityNo;
    private String priorityDate;
    
    

    public String getPriorityNo() {
        return priorityNo;
    }

    public void setPriorityNo(String priorityNo) {
        this.priorityNo = priorityNo;
    }
    
    public String getPriorityDate() {
        return priorityDate;
    }

    public void setPriorityDate(String priorityDate) {
        this.priorityDate = priorityDate;
    }
    
      

    @Override
    public String toString() {
        return "<b><u>Priority Info::</b></u>" +"<p>"+ "<b>Priority Number :.</b>" + this.priorityNo +"<p>"
                +  "<b>Priority Date :.</b>" + this.priorityDate+"<p>";
    }

}

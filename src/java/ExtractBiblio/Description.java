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
public class Description {

    
    private String description;
    
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
      
    @Override
    public String toString() {
        return "Description / Full Text Data::"+ "<p>"+  this.description;
    }

}

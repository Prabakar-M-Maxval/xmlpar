/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ExtractBiblio;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author prabakar
 */
public class ValidateInput {
    
    
    static Pattern checkKindCodePattern = Pattern.compile("^(.*)([A-Za-z].?)$", Pattern.CASE_INSENSITIVE);
    static Pattern countryCodePattern = Pattern.compile("([a-zA-z]{2,})(\\d+)", Pattern.CASE_INSENSITIVE);
    static Matcher matcher;
    static String kindCode;
    static String countryCode;
    static String patORPubNo;
    static Map<String, String> splitPatPubNo = new LinkedHashMap<>();
   // static String patPubNo ="EP405960";
    
    
    static Pattern checkCountryCode = Pattern.compile("([a-zA-z]{2,})", Pattern.CASE_INSENSITIVE);
   // static Matcher matcher;
    public static boolean checkInput(String patPubNo) 
     {
        boolean verification =false;
      
      if (patPubNo != null && !patPubNo.isEmpty()){
          matcher = checkCountryCode.matcher(patPubNo);
        Boolean isAvailableCountryCode = matcher.find();
        if (isAvailableCountryCode){
           verification = true; 
        } 
      }
      return verification;
     }
     
     public static boolean checkInput1(String patPubNo){
          boolean verification = false;

        if (patPubNo != null && !patPubNo.isEmpty()) {
            matcher = countryCodePattern.matcher(patPubNo);
            Boolean isAvailableCountryCode = matcher.find();
            if (isAvailableCountryCode) {
                splitPatPubNo.put("countrycode", matcher.group(1));
                splitPatPubNo.put("patpubno", matcher.group(2));
                verification = true;
            }else{
                splitPatPubNo.put("countrycode", "");
                verification = false;
               
            }
            matcher = checkKindCodePattern.matcher(patPubNo);
        Boolean isAvailableKindCode = matcher.find();
        if (isAvailableKindCode) {
            if (!splitPatPubNo.containsKey("patpubno")){
            splitPatPubNo.put("patpubno", matcher.group(1));
            }
            splitPatPubNo.put("kindcode", matcher.group(2));
           // verification = true;
        }else{
            //splitPatPubNo.put("patpubno", "");
            splitPatPubNo.put("kindcode", "");
        }
            
        }
        System.out.println("boolean"+verification);
        System.out.println("map"+splitPatPubNo);
        return verification;
     }
     
}

         
      
    



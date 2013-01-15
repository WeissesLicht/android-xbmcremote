package org.xbmc.android.util;

public class NameOptionsSplitter {
	   // Parallel arrays used in the conversion process.
    private static final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
    private static final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                           40,   10,    9,   5,   4,    1};
    
    //=========================================================== binaryToRoman
    public static String binaryToRoman(int binary) {
        if (binary <= 0 || binary >= 4000) {
            //throw new NumberFormatException("Value outside roman numeral range.");
            return "Number too big";
        }
        String roman = "";         // Roman notation will be accumualated here.
        
        // Loop from biggest value to smallest, successively subtracting,
        // from the binary value while adding to the roman representation.
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }  
    
    public String replaceIntwithRN(String pString) {
    	String romanString = "";
    	boolean wasLastCharAnInt = false;
    	boolean areThereNumbers = false;
    	int numToUse = 0;
    	for (int i = 0; i < pString.length(); i++) {
    		char c = pString.charAt(i);
    		if (c < '0' || c > '9') {
    			if (wasLastCharAnInt) {
    				romanString = romanString.concat(binaryToRoman(numToUse));
    				wasLastCharAnInt = false;
    				numToUse = 0;
    			}
    			romanString = romanString.concat(pString.substring(i, i+1));
    		} else {
    			numToUse = numToUse * 10 + Integer.parseInt(pString.substring(i, i+1));
     			wasLastCharAnInt = true;
     			areThereNumbers = true;
    		}
    			
    	}
    	if (wasLastCharAnInt) {
    		romanString = romanString.concat(binaryToRoman(numToUse));
    	}
    	if (areThereNumbers) {
    		return romanString;
    	}
    	return null;
    }
}

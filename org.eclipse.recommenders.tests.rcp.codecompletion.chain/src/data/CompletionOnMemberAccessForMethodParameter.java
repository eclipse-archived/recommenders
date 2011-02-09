package data;

import java.io.File;

public class CompletionOnMemberAccessForMethodParameter {
   
    File findMe = new File("");
    
    int bla = callMe(<^Space>);
    /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
     * expected type --> File
     * variable name --> XXX here we need a convention!
     */ 
   
    
    public CompletionOnMemberAccessForMethodParameter()  {
        int bla = callMe(<^Space>);
        /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
         * expected type --> File
         * variable name --> XXX here we need a convention!
         */ 
    }
    
    
    public int callMe(File fillMe){
        return 0;
    }
    
    
    public void method () {
        int i = callMe(<^Space>);
        /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
         * expected type --> File
         * variable name --> XXX here we need a convention!
         */
        
    }
}

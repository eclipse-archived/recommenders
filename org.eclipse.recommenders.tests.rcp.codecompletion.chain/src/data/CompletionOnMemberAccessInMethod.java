package data;

import java.io.File;

public class CompletionOnMemberAccessInMethod {

    File findMe = new File("");
    
    public void method() {
        File c = <^Space>
        /* calling context --> 'this' aka CompletionOnMemberAccessInMethod
         * expected type --> File
         * variable name --> c
         */
    }
    
}

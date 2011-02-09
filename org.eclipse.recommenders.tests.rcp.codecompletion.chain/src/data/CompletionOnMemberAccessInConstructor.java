package data;

import java.io.File;

public class CompletionOnMemberAccessInConstructor {
    
    File findMe = new File("");

    public CompletionOnMemberAccessInConstructor(){
        File c = <^Space>;
        /* calling context --> 'this' aka CompletionOnMemberAccessInGlobal
         * expected type --> File
         * variable name --> c
         */
    }
}

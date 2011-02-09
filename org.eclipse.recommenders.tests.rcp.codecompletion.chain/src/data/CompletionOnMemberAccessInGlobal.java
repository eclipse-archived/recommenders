package data;

import java.io.File;

public class CompletionOnMemberAccessInGlobal {
    File findMe = new File("");
    File c = <^Space>;
    /* calling context --> 'this' aka CompletionOnMemberAccessInGlobal
     * expected type --> File
     * variable name --> c
     */
}

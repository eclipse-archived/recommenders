package data;

import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnStaticAccessInConstructorWithOutImport {
    
    public  CompletionOnStaticAccessInConstructorWithOutImport() {
        
        IWorkbenchHelpSystem c = PlatformUI.<^Space> 
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> c
         */
    }
}

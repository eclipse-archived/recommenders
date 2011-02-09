package data;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class CompletionOnStaticAccessForMethodParameter {

    int bla = callMe(PlatformUI.<^Space>);
    /* calling context --> PlatformUI
     * expected type --> IWorkbenchHelpSystem
     * variable name --> XXX here we need a convention!
     */ 
   
    
    public CompletionOnStaticAccessForMethodParameter()  {
        int bla = callMe(PlatformUI.<^Space>);
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> XXX here we need a convention!
         */ 
    }
    
    
    public int callMe(IWorkbenchHelpSystem fillMe){
        return 0;
    }
    
    
    public void method () {
        int i = callMe(PlatformUI.<^Space>);
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> XXX here we need a convention!
         */
        
    }
    
    
    
}

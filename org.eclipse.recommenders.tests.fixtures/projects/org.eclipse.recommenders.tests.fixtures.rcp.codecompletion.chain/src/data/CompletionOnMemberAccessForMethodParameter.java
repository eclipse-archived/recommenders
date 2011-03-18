/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import java.io.File;

public class CompletionOnMemberAccessForMethodParameter {
   
    File findMe = new File("");
    int findMe2 = new Integer(0);
    
    int bla = callMe(findMe, <@Ignore^Space>);
    /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
     * expected type --> File
     * variable name --> XXX here we need a convention!
     */ 
   
    
    public CompletionOnMemberAccessForMethodParameter()  {
        final int bla = callMe(<@Ignore^Space>);
        /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
         * expected type --> File
         * variable name --> XXX here we need a convention!
         */ 
    }
    
    
    public int callMe(final File fillMe, Integer i){
        return 0;
    }
    
    public int callMe(final Integer fillMe){
        return 0;
    }
    
    public void method () {
        final int i = callMe(<@Ignore^Space>);
        /* calling context --> 'this' aka CompletionOnMemberAccessForMethodParameter
         * expected type --> File
         * variable name --> XXX here we need a convention!
         */
        
    }
}

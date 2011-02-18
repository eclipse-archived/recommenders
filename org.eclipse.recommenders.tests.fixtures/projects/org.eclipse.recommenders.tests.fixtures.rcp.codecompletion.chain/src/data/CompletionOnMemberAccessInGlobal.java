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

public class CompletionOnMemberAccessInGlobal {
    //@start
    File findMe = new File("");
    File c = <^Space|findMe.*(1 element).*>;
    //@end
    //File findMe = new File("");
    //File c = findMe;
    /* calling context --> 'this' aka CompletionOnMemberAccessInGlobal
     * expected type --> File
     * variable name --> c
     */
}

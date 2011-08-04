/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package data;

import java.io.File;

import helper.FileFindHelper;

//call chain 1 ok --> 1 element chain does not lead to expected solution 
public class LocalNameClashWithField {

    FileFindHelper var = new FileFindHelper();

    class A {
        public Integer findMe() {
            return 0;
        }
    }

    class B {
        public boolean findMe() {
            return true;
        }
    }

    public LocalNameClashWithField() {
    	
    	final A a;
    	final B b;
    	
    	final boolean c = 
    	
    	
    	//@start
        final File var = <^Space|.*var.findMe.*2 elements.*>
        //@end
        //final File var = this.var.findMe
    }
}

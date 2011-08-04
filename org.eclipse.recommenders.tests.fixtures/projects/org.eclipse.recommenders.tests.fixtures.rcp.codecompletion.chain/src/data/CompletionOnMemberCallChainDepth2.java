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

// call chain 1 ok
public class CompletionOnMemberCallChainDepth2 {
    public class A {
        public B b = new B();

        public class B {
            public File findMember = new File("");
            
            public File findMethod() {
            	return null;
            }
        }
    }
    
    //@start
    A a = new A();
    File c = <^Space|a.b.findMember.*>
    //@end
    //A a = new A();
    //File c = a.b.findMember
    
  //@start
    File c2 = <^Space|a.b.findMethod.*>
    //@end
    //File c2 = a.b.findMethod()

    public CompletionOnMemberCallChainDepth2(){
        //@start
        final A a = new A();
        final File c = <^Space|a.b.findMember.*>
        //@end
//        final A a = new A();
//        final File c = a.b.findMember
    }
    
    public void method() {
        //@start
        final A a = new A();
        final File c = <^Space|this.a.b.findMember.*>
        //@end
//        final A a = new A();
//        final File c = this.a.b.findMember
    }
}

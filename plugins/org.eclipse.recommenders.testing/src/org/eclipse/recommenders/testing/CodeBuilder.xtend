/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing

import java.util.concurrent.atomic.AtomicInteger

class CodeBuilder {

    private static AtomicInteger classCounter = new AtomicInteger()

    def static classname() {
        "TestClass" + classCounter.addAndGet(1)
    }

    def static classbody(CharSequence classbody) {
        classbody(classname, classbody);
    }

    def static classbody(CharSequence classname, CharSequence classbody) {
        classDeclaration('''public class «classname» ''', classbody)
    }

    def static classDeclaration(CharSequence declaration, CharSequence body) {
        '''
            import java.lang.reflect.*;
            import java.lang.annotation.*;
            import java.math.*;
            import java.io.*;
            import java.text.*;
            import java.util.*;
            import java.util.concurrent.*;
            import java.util.concurrent.atomic.*;
            import javax.annotation.*;
            import javax.xml.ws.Action;
            «declaration» {
            	«body»
            }
        '''
    }

    def static method(CharSequence methodbody) {
        classbody(
            '''
            public void __test() throws Exception {
            	«methodbody»
            }''')
    }

    def static method(CharSequence classname, CharSequence methodbody) {
        classbody(classname,
            '''
            public void __test() throws Exception {
            	«methodbody»
            }''')
    }

    def static classWithFieldsAndTestMethod(CharSequence fieldDeclarations, CharSequence methodbody) {
        classbody(
            '''
            
            «fieldDeclarations»
            
            public void __test() {
            	«methodbody»
            }''')
    }
}

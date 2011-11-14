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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//call chain 1 ok
public class CompletionOnArrayMemberAccessInMethod {

    public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
    public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];

    public static void method1() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger c = <^Space|obj.findUs\[.+\].*>
        //@end
        //final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        //final AtomicInteger c = obj.findUs[i]
    }

    public static void method2() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
		final AtomicInteger[] c = <^Space|obj.findUs.*>
		//@end
//		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
//      final AtomicInteger[] c = obj.findUs
    }

    public static void method3() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean[][][] c = <^Space|obj.findUs.*>
        //@end
        //final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        //final AtomicBoolean[][][] c = obj.findUs1
    }

    public static void method4() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean[][] c = <^Space|obj.findUs.*>
        //@end
        //final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        //final AtomicBoolean[][] c = obj.findUs1[i]
    }

    public static void method5() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean c[] = <^Space|obj.findUs.*>
        //@end
        //final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        //final AtomicBoolean c[] = obj.findUs1[i][j]
    }

    public static void method6() {
        //@start
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean c = <^Space|obj.findUs.*>
        //@end
        //final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        //final AtomicBoolean c = obj.findUs1[i][j][k]
    }
}

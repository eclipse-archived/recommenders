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

public class CompletionOnArrayMemberAccessInMethod {

    public AtomicInteger findUs[] = { new AtomicInteger(1), new AtomicInteger(2) };
    public AtomicBoolean findUs1[][][] = new AtomicBoolean[1][1][1];
    
    public static void method1() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger c = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger
         * expected completion --> obj.findUs[i]
         * variable name --> c
         */
    }

    public static void method2() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
		final AtomicInteger[] c = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger[]
         * expected completion --> obj.findUs
         * variable name --> c
         */
    }

    public static void method3() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicInteger[][] c = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger[][]
         * NOT expected completion --> obj.findUs
         * variable name --> c
         */
    }
    
    public static void method4() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean[][] c = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger
         * expected completion --> obj.findUs[i]
         * variable name --> c
         */
    }
    
    public static void method5() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean c[] = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger
         * expected completion --> obj.findUs[i]
         * variable name --> c
         */
    }
    
    public static void method6() {
		final CompletionOnArrayMemberAccessInMethod obj = new CompletionOnArrayMemberAccessInMethod();
        final AtomicBoolean c = <@Ignore^Space>
        /* calling context --> static
         * expected type --> AtomicInteger
         * expected completion --> obj.findUs[i]
         * variable name --> c
         */
    }
    
}

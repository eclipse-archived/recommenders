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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompletionOnGenericTypeInMethod {

    public List<String> findMe = new ArrayList<String>();

    public static void test_exactGenericType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<String> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<String>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_exactButWrongGenericType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<Integer> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<Integer>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_anonymousGenericType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<?> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<?>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_genericSuperType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<? extends Serializable> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? extends Serializable>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_wrongGenericSuperType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<? extends File> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? extends File>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_genericSubType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<? super String> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? super String>
         * expected completion --> variable.findMe
		 * variable name --> c
		 */
	}

    public static void test_wrongGenericSubType() {
		final CompletionOnGenericTypeInMethod variable = new CompletionOnGenericTypeInMethod();
		final List<? super Serializable> c = <^Space>
		/*
		 * calling context --> static
		 * expected type --> List<? super Serializable>
         * NOT expected completion --> variable.findMe
		 * variable name --> c
		 */
	}
}

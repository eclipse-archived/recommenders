/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.analysis;

import org.junit.Ignore;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

@Ignore
public class TestConstants {

    public static final String organisation = "com.android.ide";

    public static final String module = "com.android.ide.eclipse.adt";

    public static final String revision = "0.9.5.v200911191123-20404";

    public static TypeReference TYPE_OBJECT = TypeReference.JavaLangObject;

    public static Selector SELECTOR_HASHCODE = Selector.make("hashCode()I");

    public static MethodReference METHOD_OBJECT_HASHCODE = MethodReference.findOrCreate(TYPE_OBJECT, SELECTOR_HASHCODE);

    public static MethodReference METHOD_CLASS_FOR_NAME = MethodReference.JavaLangClassForName;
}

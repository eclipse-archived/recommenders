/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.fixtures.rcp.utils;

public interface Methods {

    public void __testVoid();

    public void __test(String s);

    public String __testString();

    public void __test(String... s);

    public void __test(String[] s, String s2);

    // Currently no support for generics
    // public <T> T __test(T c);
    // public void __test(List<String> l);

}

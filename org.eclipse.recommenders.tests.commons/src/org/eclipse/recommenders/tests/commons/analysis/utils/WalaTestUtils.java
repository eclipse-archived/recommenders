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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class WalaTestUtils {

    /**
     * Uses the application classloader to lookup the give java class in the
     * given class hierarchy.
     * 
     * @return the wala requested class or throws a runtime exception if no
     *         matching wala class could be found in the class hierarchy
     * 
     */
    public static IClass lookupClass(IClassHierarchy cha, Class<?> javaClass) {
        TypeName typeName = WalaNameUtils.java2walaTypeName(javaClass);
        TypeReference typeReference = TypeReference.findOrCreate(ClassLoaderReference.Application, typeName);
        IClass res = cha.lookupClass(typeReference);
        return ensureIsNotNull(res, "failed to lookup class '%s' in cha.", javaClass);
    }

    public static IMethod lookupTestMethod(IClassHierarchy cha, Class<?> javaClass) {
        IClass clazz = lookupClass(cha, javaClass);
        for (IMethod m : clazz.getDeclaredMethods()) {
            String name = m.getName().toString();
            if (name.equals("__test")) {
                return m;
            }
        }
        throw Throws.throwIllegalArgumentException("class '%s' does not have a '__test' method.", javaClass);
    }
}

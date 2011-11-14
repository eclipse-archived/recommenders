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

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.InputStream;

import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.utils.Throws;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
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
    public static IClass lookupClass(final IClassHierarchy cha, final Class<?> javaClass) {
        final TypeName typeName = WalaNameUtils.java2walaTypeName(javaClass);
        final TypeReference typeReference = TypeReference.findOrCreate(ClassLoaderReference.Application, typeName);
        final IClass res = cha.lookupClass(typeReference);
        return ensureIsNotNull(res, "failed to lookup class '%s' in cha.", javaClass);
    }

    public static IMethod lookupTestMethod(final IClassHierarchy cha, final Class<?> javaClass) {
        final IClass clazz = lookupClass(cha, javaClass);
        for (final IMethod m : clazz.getDeclaredMethods()) {
            final String name = m.getName().toString();
            if (name.equals("__test")) {
                return m;
            }
        }
        throw Throws.throwIllegalArgumentException("class '%s' does not have a '__test' method.", javaClass);
    }

    public static XMLMethodSummaryReader getNativeSummaries(final IClassHierarchy cha) {
        final ClassLoader cl = Util.class.getClassLoader();
        final InputStream s = cl.getResourceAsStream("natives.xml");
        final AnalysisScope scope = cha.getScope();
        final XMLMethodSummaryReader summary = new XMLMethodSummaryReader(s, scope);
        return summary;
    }
}

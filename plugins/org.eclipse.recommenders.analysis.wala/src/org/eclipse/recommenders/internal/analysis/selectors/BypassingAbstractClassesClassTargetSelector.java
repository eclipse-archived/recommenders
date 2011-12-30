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
package org.eclipse.recommenders.internal.analysis.selectors;

import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ClassTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassSyntheticClass;
import com.ibm.wala.ipa.summaries.BypassSyntheticClassLoader;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class BypassingAbstractClassesClassTargetSelector implements ClassTargetSelector {
    private static ClassLoaderReference SYNTHETIC_CLASSLOADER_REF = new ClassLoaderReference(AnalysisScope.SYNTHETIC,
            null, null);

    @Override
    public IClass getAllocatedTarget(final CGNode caller, final NewSiteReference site) {
        final IClassHierarchy cha = caller.getClassHierarchy();
        final IClass targetClass = lookupClassDefinitionInCha(cha, site);
        if (null == targetClass) {
            return null;
        } else if (targetClass.isAbstract()) {
            return returnBypassedClassDefinition(targetClass);
        } else {
            return targetClass;
        }
    }

    private IClass returnBypassedClassDefinition(final IClass realType) {
        final IClassHierarchy cha = realType.getClassHierarchy();
        final BypassSyntheticClassLoader loader = lookupBypassClassLoader(cha);
        final TypeReference realTypeRef = realType.getReference();
        final TypeName bypassTypeName = BypassSyntheticClass.getName(realTypeRef);
        IClass bypassClass = loader.lookupClass(bypassTypeName);
        if (bypassClass == null || !bypassClass.getName().toString().startsWith("L$")) {
            bypassClass = createAndRegisterBypassClass(realType, cha, bypassTypeName, loader);
        }
        // why?
        ensureIsInstanceOf(bypassClass, BypassSyntheticClass.class);
        return bypassClass;
    }

    private IClass createAndRegisterBypassClass(final IClass realType, final IClassHierarchy cha,
            final TypeName bypassTypeName, final BypassSyntheticClassLoader bypassClassLoader) {
        final IClass bypassClass = new BypassSyntheticClass(realType, bypassClassLoader, cha);
        bypassClassLoader.registerClass(bypassTypeName, bypassClass);
        return bypassClass;
    }

    private BypassSyntheticClassLoader lookupBypassClassLoader(final IClassHierarchy cha) {
        final BypassSyntheticClassLoader res = (BypassSyntheticClassLoader) cha.getLoader(SYNTHETIC_CLASSLOADER_REF);
        return ensureIsNotNull(res);
    }

    private IClass lookupClassDefinitionInCha(final IClassHierarchy cha, final NewSiteReference site) {
        final TypeReference nominalRef = site.getDeclaredType();
        return cha.lookupClass(nominalRef);
    }
}

/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.depersonalizer;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnitVisitor;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeReference;

public class ObjectUsageFilter extends CompilationUnitVisitor implements ICompilationUnitDepersonalizer {

    public ObjectUsageFilter(final Set<String> allowedjarFingerprints) {
        super();
        Checks.ensureIsNotNull(allowedjarFingerprints);
        if (allowedjarFingerprints.contains(null)) {
            throw new IllegalArgumentException("the allowed fingerprints must not contain the null value");
        }
        this.allowedJarFingerprints = allowedjarFingerprints;
    }

    Set<String> allowedJarFingerprints;
    Set<ITypeName> allowedTypes;

    @Override
    public CompilationUnit depersonalize(final CompilationUnit compilationUnit) {
        allowedTypes = new HashSet<ITypeName>();
        compilationUnit.accept(this);
        return compilationUnit;
    }

    @Override
    public boolean visit(final ObjectInstanceKey objectInstanceKey) {
        if (objectInstanceKey.definitionSite != null) {
            objectInstanceKey.definitionSite.definedByMethod = filter(objectInstanceKey.definitionSite.definedByMethod);
        }
        filterParameterCallSites(objectInstanceKey.parameterCallSites);
        return super.visit(objectInstanceKey);
    }

    private void filterParameterCallSites(final Set<ParameterCallSite> parameterCallSites) {
        for (final ParameterCallSite site : parameterCallSites.toArray(new ParameterCallSite[0])) {
            if (!isAllowed(site.targetMethod.getDeclaringType())) {
                parameterCallSites.remove(site);
            }
        }
    }

    @Override
    public boolean visit(final CompilationUnit compilationUnit) {
        for (final TypeReference ref : compilationUnit.imports.toArray(new TypeReference[0])) {
            if (ref.fingerprint != null && allowedJarFingerprints.contains(ref.fingerprint)) {
                allowedTypes.add(ref.name);
            } else {
                compilationUnit.imports.remove(ref);
            }
        }
        return super.visit(compilationUnit);
    }

    @Override
    public boolean visit(final TypeDeclaration type) {
        type.superclass = filter(type.superclass);
        filter(type.interfaces);
        filter(type.fields);
        return super.visit(type);
    }

    @Override
    public boolean visit(final MethodDeclaration method) {
        method.firstDeclaration = filter(method.firstDeclaration);
        method.superDeclaration = filter(method.superDeclaration);

        for (final ObjectInstanceKey key : method.objects.toArray(new ObjectInstanceKey[0])) {
            if (!isAllowed(key.type)) {
                method.objects.remove(key);
            }
        }
        return super.visit(method);
    }

    private boolean isAllowed(final ITypeName type) {
        return allowedTypes.contains(type);
    }

    private IMethodName filter(final IMethodName method) {
        if (method != null && !isAllowed(method.getDeclaringType())) {
            return null;
        }
        return method;
    }

    private ITypeName filter(final ITypeName type) {
        if (!isAllowed(type)) {
            return null;
        }
        return type;
    }

    private void filter(final Collection<ITypeName> set) {
        for (final ITypeName entry : set.toArray(new ITypeName[set.size()])) {
            if (!isAllowed(entry)) {
                set.remove(entry);
            }
        }
    }

}

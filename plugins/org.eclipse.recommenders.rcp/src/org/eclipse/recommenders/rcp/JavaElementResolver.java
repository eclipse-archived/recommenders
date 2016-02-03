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
package org.eclipse.recommenders.rcp;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.internal.rcp.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Logs.log;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceMethod;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceType;
import org.eclipse.jdt.internal.corext.util.SearchUtils;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class JavaElementResolver {

    public static JavaElementResolver INSTANCE;

    public JavaElementResolver() {
        INSTANCE = this;
    }

    private final BiMap<IName, IJavaElement> cache = HashBiMap.create();
    public Set<IMethodName> failedRecMethods = new HashSet<>();
    public Set<ITypeName> failedRecTypes = new HashSet<>();

    public Optional<IType> toJdtType(final ITypeName recType) {
        ensureIsNotNull(recType);

        if (failedRecTypes.contains(recType)) {
            return absent();
        }

        IType jdtType = (IType) cache.get(recType);
        if (jdtType == null) {
            jdtType = resolveType(recType).orNull();

            if (jdtType != null) {
                registerRecJdtElementPair(recType, jdtType);
            } else {
                failedRecTypes.add(recType);
            }
        } else if (!jdtType.exists()) {
            // found in cache but not existing anymore?
            // restart resolution process:
            cache.remove(recType);
            return toJdtType(recType);
        }
        return fromNullable(jdtType);
    }

    public ITypeName toRecType(IType jdtType) {
        ensureIsNotNull(jdtType);
        jdtType = JdtUtils.resolveJavaElementProxy(jdtType);
        ITypeName recType = (ITypeName) cache.inverse().get(jdtType);
        if (recType == null) {
            String fullyQualifiedName = jdtType.getFullyQualifiedName();
            recType = VmTypeName.get('L' + fullyQualifiedName.replace('.', '/'));
            registerRecJdtElementPair(recType, jdtType);
        }
        return recType;
    }

    private Optional<IType> resolveType(final ITypeName recType) {
        // TODO woah, what a hack just to find a nested/anonymous type... this
        // definitely needs refactoring!
        ensureIsNotNull(recType);
        if (recType.isArrayType()) {
            // TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=339806
            // should throw an exception? or return an Array type?
            log(ERROR_ARRAY_TYPE_IN_JAVA_ELEMENT_RESOLVER, recType);
            return absent();
        }

        if (recType.isNestedType()) {
            final ITypeName declaringType = recType.getDeclaringType();
            final String simpleName = StringUtils.substringAfterLast(recType.getIdentifier(), "$"); //$NON-NLS-1$

            final IType parent = resolveType(declaringType).orNull();
            if (parent != null) {
                try {
                    for (final IType nested : parent.getTypes()) {
                        final String key = nested.getKey();
                        if (key.equals(recType.getIdentifier() + ';')) {
                            return fromNullable(nested);
                        }
                    }

                    for (final IMethod m : parent.getMethods()) {
                        for (final IJavaElement children : m.getChildren()) {
                            if (children instanceof IType) {
                                final IType nested = (IType) children;
                                if (nested.getKey().endsWith(simpleName + ';')) {
                                    return of(nested);
                                }

                                final String key = nested.getKey();
                                if (key.equals(recType.getIdentifier() + ';')) {
                                    return fromNullable(nested);
                                }
                            }
                        }
                    }
                } catch (final Exception x) {
                    return absent();
                }
            }
            return absent();
        }
        final IType[] res = new IType[1];
        final IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
        final SearchEngine search = new SearchEngine();
        final String srcTypeName = Names.vm2srcTypeName(recType.getIdentifier());
        final SearchPattern pattern = SearchPattern.createPattern(srcTypeName, IJavaSearchConstants.TYPE,
                IJavaSearchConstants.DECLARATIONS, SearchPattern.R_FULL_MATCH);
        try {
            search.search(pattern, SearchUtils.getDefaultSearchParticipants(), scope, new SearchRequestor() {

                @Override
                public void acceptSearchMatch(final SearchMatch match) throws CoreException {
                    IType element = (IType) match.getElement();
                    // with the current settings the engine matches 'Lnull' with 'Ljava/lang/ref/ReferenceQueue$Null'
                    if (toRecType(element).equals(recType)) {
                        res[0] = element;
                    }
                }
            }, null);
        } catch (final CoreException e) {
            throwUnhandledException(e);
        }
        return fromNullable(res[0]);
    }

    private void registerRecJdtElementPair(final IName recName, final IJavaElement jdtElement) {
        ensureIsNotNull(recName);
        ensureIsNotNull(jdtElement);
        if (jdtElement instanceof AssistSourceType) {
            return;
        } else if (jdtElement instanceof AssistSourceMethod) {
            return;
        }
        cache.forcePut(recName, jdtElement);
        // XXX checkIsNull(put);
    }

    public Optional<IMethod> toJdtMethod(final IMethodName recMethod) {
        ensureIsNotNull(recMethod);
        // failedRecMethods.clear()
        if (failedRecMethods.contains(recMethod)) {
            return absent();
        }

        IMethod jdtMethod = (IMethod) cache.get(recMethod);
        if (jdtMethod != null && !jdtMethod.exists()) {
            jdtMethod = null;
        }
        if (jdtMethod == null) {
            jdtMethod = resolveMethod(recMethod).orNull();
            if (jdtMethod == null) {
                // if (!recMethod.isSynthetic()) {
                // System.err.printf("resolving %s failed. Is it an compiler generated constructor?\n.",
                // recMethod.getIdentifier());
                // }
                failedRecMethods.add(recMethod);
                return absent();
            }
            registerRecJdtElementPair(recMethod, jdtMethod);
        } else if (!jdtMethod.exists()) {
            // found in cache but not existing anymore?
            // restart resolution process:
            cache.remove(recMethod);
            return toJdtMethod(recMethod);
        }
        return fromNullable(jdtMethod);
    }

    /**
     * Returns null if we fail to resolve all types used in the method signature, for instance generic return types
     * etc...
     *
     */
    // This method should return IMethodNames in all cases but yet it does not work completely as we want it to work
    public Optional<IMethodName> toRecMethod(@Nullable IMethod jdtMethod) {
        if (jdtMethod == null) {
            return absent();
        }
        if (!jdtMethod.exists()) {
            // compiler generated methods (e.g., calls to constructors to inner non-static classes do not exist.
            return absent();
        }
        JdtUtils.resolveJavaElementProxy(jdtMethod);
        IMethodName recMethod = (IMethodName) cache.inverse().get(jdtMethod);
        if (recMethod == null) {
            try {
                final IType jdtDeclaringType = jdtMethod.getDeclaringType();
                ITypeParameter[] typeParameters = jdtMethod.getTypeParameters();
                //
                final String[] unresolvedParameterTypes = jdtMethod.getParameterTypes();
                final String[] resolvedParameterTypes = new String[unresolvedParameterTypes.length];
                for (int i = resolvedParameterTypes.length; i-- > 0;) {
                    resolvedParameterTypes[i] = resolveType(jdtDeclaringType, unresolvedParameterTypes[i],
                            typeParameters);
                }

                String resolvedReturnType = resolveType(jdtDeclaringType, jdtMethod.getReturnType(), typeParameters);

                final String methodSignature = Names.src2vmMethod(
                        jdtMethod.isConstructor() ? "<init>" : jdtMethod.getElementName(), resolvedParameterTypes, //$NON-NLS-1$
                        resolvedReturnType);
                final ITypeName recDeclaringType = toRecType(jdtDeclaringType);
                recMethod = VmMethodName.get(recDeclaringType.getIdentifier(), methodSignature);
                registerRecJdtElementPair(recMethod, jdtMethod);
            } catch (final Exception e) {
                log(ERROR_FAILED_TO_RESOLVE_METHOD, jdtMethod, e.getMessage(), e);
                return absent();
            }
        }
        return fromNullable(recMethod);
    }

    private String resolveType(final IType jdtDeclaringType, final String unresolvedType,
            final ITypeParameter[] typeParameters) throws JavaModelException {
        final int arrayCount = Signature.getArrayCount(unresolvedType);
        String toResolve = unresolvedType.substring(arrayCount);
        if (typeParameters != null && toResolve.startsWith("Q")) { //$NON-NLS-1$
            String typeName = toResolve.substring(1, toResolve.length() - 1);
            for (ITypeParameter typeParameter : typeParameters) {
                if (!typeParameter.getElementName().equals(typeName)) {
                    continue;
                }
                String[] boundsSignatures = typeParameter.getBoundsSignatures();
                if (boundsSignatures.length > 0) {
                    toResolve = boundsSignatures[0];
                    break;
                }
            }
        }

        String resolvedType = JdtUtils
                .resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(toResolve, jdtDeclaringType)
                .or(Signature.SIG_VOID);
        resolvedType = resolvedType + StringUtils.repeat("[]", arrayCount); //$NON-NLS-1$
        return resolvedType;
    }

    private Optional<IMethod> resolveMethod(final IMethodName recMethod) {
        ensureIsNotNull(recMethod);
        try {
            final IType jdtType = toJdtType(recMethod.getDeclaringType()).orNull();
            if (!isSuccessfullyResolvedType(jdtType)) {
                return absent();
            }
            List<IType> supertypes = createListOfSupertypes(jdtType);
            for (final IType t : supertypes) {
                for (final IMethod m : t.getMethods()) {
                    if (sameSignature(recMethod, m)) {
                        return of(m);
                    }
                }
            }
            return absent();
        } catch (final Exception e) {
            log(ERROR_FAILED_TO_RESOLVE_METHOD, recMethod, e.getMessage(), e);
            return absent();
        }
    }

    private List<IType> createListOfSupertypes(final IType jdtType) throws JavaModelException {
        final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(jdtType);
        List<IType> supertypes = Lists.newArrayList(jdtType);
        for (IType supertype : hierarchy.getAllSupertypes(jdtType)) {
            supertypes.add(supertype);
        }
        if (jdtType.isInterface()) {
            // ensure java.lang.Object is in the list to resolve, e.g., calls to 'interface.getClass()'
            for (IType s : hierarchy.getRootClasses()) {
                supertypes.add(s);
            }
        }
        return supertypes;
    }

    private boolean sameSignature(final IMethodName recMethod, final IMethod jdtMethod) throws JavaModelException {
        if (!(bothConstructors(recMethod, jdtMethod) || sameName(recMethod, jdtMethod))) {
            return false;
        }
        final ITypeName[] recTypes = recMethod.getParameterTypes();
        final String[] jdtTypes = jdtMethod.getParameterTypes();
        if (!sameNumberOfParameters(recMethod, jdtMethod)) {
            return false;
        }
        for (int i = 0; i < recTypes.length; i++) {
            final Optional<ITypeName> jdtType = JdtUtils.resolveUnqualifiedJDTType(jdtTypes[i], jdtMethod);
            // TODO XXX: checking for simple names is not clean! getClassName()
            if (!jdtType.isPresent()) {
                return false;
            }
            if (!sameSimpleTypes(recTypes[i], jdtType.get()) || !sameArrayDimensions(recTypes[i], jdtTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean sameSimpleTypes(final ITypeName t1, final ITypeName t2) {
        return t1.getClassName().equals(t2.getClassName());
    }

    private boolean sameArrayDimensions(final ITypeName t1, final String jdtTypes) {
        int dim1 = t1.getArrayDimensions();
        int dim2 = Signature.getArrayCount(jdtTypes.toCharArray());
        return dim1 == dim2;
    }

    private boolean sameNumberOfParameters(final IMethodName recMethod, final IMethod m) throws JavaModelException {
        return recMethod.getParameterTypes().length == m.getParameters().length;
    }

    private boolean sameName(final IMethodName recMethod, final IMethod m) {
        return recMethod.getName().equals(m.getElementName());
    }

    private boolean bothConstructors(final IMethodName recMethod, final IMethod m) throws JavaModelException {
        return recMethod.isInit() && m.isConstructor();
    }

    private boolean isSuccessfullyResolvedType(final IType jdtType) throws JavaModelException {
        return jdtType != null && jdtType.isStructureKnown();
    }
}

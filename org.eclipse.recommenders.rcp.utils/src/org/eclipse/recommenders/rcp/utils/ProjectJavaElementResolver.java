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
package org.eclipse.recommenders.rcp.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceMethod;
import org.eclipse.jdt.internal.codeassist.impl.AssistSourceType;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.internal.RecommendersUtilsPlugin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class ProjectJavaElementResolver {

    private final IJavaProject project;

    public ProjectJavaElementResolver(final IJavaProject project) {
        this.project = project;
    }

    private final BiMap<IName, IJavaElement> cache = HashBiMap.create();
    private final HashSet<IName> unresolvableNames = Sets.newHashSet();

    public IType toType(final ITypeName name) {
        ensureIsNotNull(name);
        if (unresolvableNames.contains(name)) {
            return null;
        }
        IType type = findInCache(name);
        if (type != null) {
            return type;
        }

        try {
            type = resolveType(name);
            if (type != null) {
                registerRecJdtElementPair(name, type);
            } else {
                unresolvableNames.add(name);
            }
        } catch (final JavaModelException e) {
            logAndRegisterFail(name);
            unresolvableNames.add(name);
        }
        return type;
    }

    public ITypeName toRecType(IType jdtType) {
        ensureIsNotNull(jdtType);
        jdtType = JdtUtils.resolveJavaElementProxy(jdtType);
        ITypeName recType = (ITypeName) cache.inverse().get(jdtType);
        if (recType == null) {
            String fullyQualifiedName = jdtType.getFullyQualifiedName();
            fullyQualifiedName = StringUtils.substringBefore(fullyQualifiedName, "<");
            recType = VmTypeName.get("L" + fullyQualifiedName.replace('.', '/'));
            registerRecJdtElementPair(recType, jdtType);
        }
        return recType;
    }

    private IType resolveType(final ITypeName name) throws JavaModelException {
        ensureIsNotNull(name);

        // // TODO woah, what a hack just to find a nested/anonymous type...
        // this
        // // definitely needs refactoring!
        // if (name.isArrayType()) {
        // // TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=339806
        // // should throw an exception? or return an Array type?
        // System.err.println("array type in JavaElementResolver. Decision  bug 339806 pending...?");
        // return null;
        // }
        //

        final String srcName = Names.vm2srcTypeName(name.getIdentifier());
        IType res = project.findType(srcName);
        if (res != null) {
            return res;
        }

        if (name.isNestedType()) {
            final ITypeName parentName = name.getDeclaringType();
            final IType parentType = resolveType(parentName);
            if (parentType == null) {
                logAndRegisterFail(parentName);
                return null;
            }

            res = searchInMembers(name, parentType);
            if (res == null) {
                logAndRegisterFail(name);
            }
            return res;
        }
        return res;
    }

    private IType searchInMembers(final ITypeName name, final IType parentType) throws JavaModelException {
        final String searchKey = name.getIdentifier() + ";";

        for (final IJavaElement child : parentType.getChildren()) {
            if (child instanceof IType) {
                final IType nestedType = (IType) child;
                if (matchesSearchKey(searchKey, nestedType)) {
                    return nestedType;
                }
            }

            if (child instanceof IMember) {
                final IMember member = (IMember) child;
                for (final IJavaElement memChild : member.getChildren()) {
                    if (memChild instanceof IType) {
                        final IType nestedType = (IType) memChild;
                        if (matchesSearchKey(searchKey, nestedType)) {
                            return nestedType;
                        }
                    }
                }

            }
        }
        return null;
    }

    private boolean matchesSearchKey(final String searchKey, final IType nestedType) {
        final String key = nestedType.getKey();
        return key.equals(searchKey);
    }

    // final IType[] res = new IType[1];
    // final IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    // final SearchEngine search = new SearchEngine();
    // final String srcTypeName = srcName;
    // final SearchPattern pattern =
    // SearchPattern.createPattern(srcTypeName, IJavaSearchConstants.TYPE,
    // IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
    // try {
    // search.search(pattern, SearchUtils.getDefaultSearchParticipants(),
    // scope, new SearchRequestor() {
    //
    // @Override
    // public void acceptSearchMatch(final SearchMatch match) throws
    // CoreException {
    // res[0] = (IType) match.getElement();
    // }
    // }, null);
    // } catch (final CoreException e) {
    // throwUnhandledException(e);
    // }
    // return res[0];

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

    public IMethod toJdtMethod(final IMethodName recMethod) {
        ensureIsNotNull(recMethod);
        if (unresolvableNames.contains(recMethod)) {
            return null;
        }

        IMethod jdtMethod = findInCache(recMethod);
        if (jdtMethod == null) {
            jdtMethod = resolveMethod(recMethod);
            if (jdtMethod == null) {
                logAndRegisterFail(recMethod);
                return null;
            }
            registerRecJdtElementPair(recMethod, jdtMethod);
        }
        return jdtMethod;
    }

    private void logAndRegisterFail(final IName name) {
        RecommendersUtilsPlugin.logWarning(
                "resolving %s failed. Is it an compiler generated element or does it use generics?\n.",
                name.getIdentifier());
        unresolvableNames.add(name);
    }

    /**
     * Returns null if we fail to resolve all types used in the method
     * signature, for instance generic return types etc...
     */
    public IMethodName toRecMethod(IMethod jdtMethod) {
        ensureIsNotNull(jdtMethod);
        jdtMethod = JdtUtils.resolveJavaElementProxy(jdtMethod);
        IMethodName recMethod = findInCache(jdtMethod);
        if (recMethod == null) {
            try {
                final IType jdtDeclaringType = jdtMethod.getDeclaringType();

                final String srcDeclaringType = toSrcType(jdtDeclaringType);
                final String srcParameterTypes[] = createSrcParameterTypes(jdtMethod, jdtDeclaringType);
                final String srcReturnType = unresolvedSigType2SrcType(jdtDeclaringType, jdtMethod.getReturnType());
                final String srcMethodName = jdtMethod.isConstructor() ? "<init>" : jdtMethod.getElementName();
                final String srcMethodSignature = Names.src2vmMethod(srcDeclaringType, srcMethodName,
                        srcParameterTypes, srcReturnType);

                recMethod = VmMethodName.get(srcMethodSignature);
                registerRecJdtElementPair(recMethod, jdtMethod);
            } catch (final Exception e) {
                RecommendersUtilsPlugin.logError(e, "failed to resolve jdt method '%s'.", jdtMethod);
                return null;
            }
        }
        return recMethod;
    }

    @SuppressWarnings("unchecked")
    private <T extends IMember> T findInCache(final IName name) {
        return (T) cache.get(name);
    }

    @SuppressWarnings("unchecked")
    private <T extends IName> T findInCache(final IMember jdtMember) {
        return (T) cache.inverse().get(jdtMember);
    }

    private String toSrcType(final IType jdtType) {
        String fullyQualifiedName = jdtType.getFullyQualifiedName();
        fullyQualifiedName = StringUtils.substringBefore(fullyQualifiedName, "<");
        return fullyQualifiedName;
    }

    private String[] createSrcParameterTypes(final IMethod jdtMethod, final IType jdtDeclaringType)
            throws JavaModelException {
        final String[] unresolvedParameterTypes = jdtMethod.getParameterTypes();
        final String[] resolvedParameterTypes = new String[unresolvedParameterTypes.length];
        for (int i = resolvedParameterTypes.length; i-- > 0;) {
            resolvedParameterTypes[i] = unresolvedSigType2SrcType(jdtDeclaringType, unresolvedParameterTypes[i]);
        }
        return resolvedParameterTypes;
    }

    private String unresolvedSigType2SrcType(final IType declaringClass, final String unresolvedSignatureTypeName)
            throws JavaModelException {
        final int arrayCount = Signature.getArrayCount(unresolvedSignatureTypeName);
        final String resolvedTypeName = JavaModelUtil.getResolvedTypeName(unresolvedSignatureTypeName, declaringClass);
        return resolvedTypeName + StringUtils.repeat("[]", arrayCount);
    }

    private IMethod resolveMethod(final IMethodName recMethod) {
        ensureIsNotNull(recMethod);
        try {
            final IType jdtType = toType(recMethod.getDeclaringType());
            if (!isSuccessfullyResolvedType(jdtType)) {
                return null;
            }
            final String[] jdtParamTypes = createJDTParameterTypeStrings(recMethod);
            final TypeHierarchy hierarchy = new TypeHierarchy(jdtType, new ICompilationUnit[0],
                    jdtType.getJavaProject(), false);
            hierarchy.refresh(null);
            final IMethod jdtMethod = JavaModelUtil.findMethodInHierarchy(hierarchy, jdtType, recMethod.getName(),
                    jdtParamTypes, recMethod.isInit());
            return jdtMethod;
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    private boolean isSuccessfullyResolvedType(final IType jdtType) throws JavaModelException {
        return jdtType != null && jdtType.isStructureKnown();
    }

    private String[] createJDTParameterTypeStrings(final IMethodName method) {
        /*
         * Note, JDT expects declared-types (also declared array-types) given as
         * parameters to (i) use dots as separator, and (ii) end with a
         * semicolon. this conversion is done here:
         */
        final ITypeName[] paramTypes = method.getParameterTypes();
        final String[] jdtParamTypes = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            jdtParamTypes[i] = createJdtParameterTypeString(paramTypes[i]);
        }
        return jdtParamTypes;
    }

    private String createJdtParameterTypeString(final ITypeName type) {
        final String identifier = type.getIdentifier();
        if (type.isDeclaredType()) {
            return identifier.replace('/', '.') + ";";
        } else if (type.isArrayType() && type.getArrayBaseType().isDeclaredType()) {
            return identifier.replace('/', '.') + ";";
        } else {
            return identifier;
        }
    }
}

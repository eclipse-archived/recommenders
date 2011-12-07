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
package org.eclipse.recommenders.internal.analysis.utils;

import static org.eclipse.recommenders.utils.Checks.ensureIsFalse;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

public class MethodUtils {
    public static Collection<IMethod> findAllFinalAndPublicOrProtectedMethods(final IClass clazz) {
        final List<IMethod> res = Lists.newLinkedList();
        final Set<Selector> history = Sets.newHashSet();
        for (IClass cur = clazz; cur != null; cur = cur.getSuperclass()) {
            for (final IMethod method : findDeclaredFinalAndPublicOrProtectedMethods(cur)) {
                if (history.add(method.getSelector())) {
                    res.add(method);
                }
            }
        }
        sortMethodsBySignature(res);
        return res;
    }

    public static SummarizedMethod createEmptyStubMethod(final IClass receiver, final MethodReference ref) {
        return new SummarizedMethod(ref, new MethodSummary(ref), receiver);
    }

    public static Collection<IMethod> findAllDeclaredPublicInstanceMethodsWithImplementation(final IClass clazz) {
        final List<IMethod> res = Lists.newLinkedList();
        for (final IMethod method : clazz.getDeclaredMethods()) {
            if (method.isAbstract()) {
                continue;
            } else if (method.isStatic()) {
                continue;
            } else if (method.isInit()) {
                continue;
            } else if (!method.isPublic()) {
                continue;
            }
            res.add(method);
        }
        sortMethodsBySignature(res);
        return res;
    }

    public static Collection<IMethod> findAllOverridableMethods(final IClass clazz) {
        final LinkedList<IMethod> res = new LinkedList<IMethod>();
        final HashSet<Selector> history = new HashSet<Selector>();
        for (IClass cur = clazz; cur != null; cur = cur.getSuperclass()) {
            for (final IMethod method : findDeclaredOverridableMethods(cur)) {
                if (history.add(method.getSelector())) {
                    res.add(method);
                }
            }
        }
        for (final IClass interfaze : clazz.getAllImplementedInterfaces()) {
            for (final IMethod method : findDeclaredOverridableMethods(interfaze)) {
                if (history.add(method.getSelector())) {
                    res.add(method);
                }
            }
        }
        sortMethodsBySignature(res);
        return res;
    }

    /**
     * Returns the constructors of the given class.
     */
    public static Collection<IMethod> findDeclaredConstructors(final IClass clazz) {
        final HashSet<IMethod> res = new HashSet<IMethod>();
        for (final IMethod method : clazz.getDeclaredMethods()) {
            if (method.isInit()) {
                res.add(method);
            }
        }
        return res;
    }

    public static Collection<IMethod> findDeclaredFinalAndPublicOrProtectedMethods(final IClass clazz) {
        final HashSet<IMethod> res = new HashSet<IMethod>();
        for (final IMethod method : clazz.getDeclaredMethods()) {
            if (method.isFinal() && (method.isProtected() || method.isPublic())
                    && !(method.isStatic() || method.isInit())) {
                res.add(method);
            }
        }
        return res;
    }

    /**
     * Returns all methods that might be overridden by subclasses.
     */
    public static Collection<IMethod> findDeclaredOverridableMethods(final IClass clazz) {
        final HashSet<IMethod> res = new HashSet<IMethod>();
        for (final IMethod method : clazz.getDeclaredMethods()) {
            if (isOverridable(method)) {
                res.add(method);
            }
        }
        return res;
    }

    /**
     * @return the absolute first method declaration of the given method (AKA
     *         'root method')- or the method itself if it does not override any
     *         other methods
     */
    public static IMethod findRootDeclaration(final IMethod method) {
        ensureIsNotNull(method);
        ensureIsFalse(method.isInit(), "works for methods only");
        if (!mayHaveSuperDeclaration(method)) {
            return null;
        }
        IMethod last = null;
        for (IMethod cur = method; cur != null; cur = findSuperDeclaration(cur)) {
            last = cur;
        }
        return last;
    }

    public static IMethod findSuperDeclaration(final IMethod method) {
        if (!mayHaveSuperDeclaration(method)) {
            return null;
        }
        final IMethod superImpl = findSuperImplementation(method);
        if (superImpl != null) {
            return superImpl;
        }
        // lookup method declaration in implemented interfaces
        final Selector search = method.getSelector();
        final IClass clazz = method.getDeclaringClass();
        for (final IClass interface_ : clazz.getAllImplementedInterfaces()) {
            final IMethod match = interface_.getMethod(search);
            if (null != match && isOverridable(match)) {
                return match;
            }
        }
        return null;
    }

    public static IMethod findSuperImplementation(final IMethod method) {
        if (!mayHaveSuperDeclaration(method)) {
            return null;
        }
        final IClass clazz = method.getDeclaringClass();
        final IClass superclazz = clazz.getSuperclass();
        if (superclazz == null) {
            return null;
        }
        final Selector search = method.getSelector();
        final IMethod match = superclazz.getMethod(search);
        if (match == null || !isOverridable(method)) {
            return null;
        }
        return match;
    }

    public static boolean haveEqualSelector(final IMethod m1, final IMethod m2) {
        return m2.getSelector().equals(m1.getSelector());
    }

    public static boolean haveSameParameters(final IMethod m1, final IMethod m2) {
        if (m1.getNumberOfParameters() != m2.getNumberOfParameters()) {
            return false;
        }
        for (int i = m1.getNumberOfParameters(); i-- > 0;) {
            final TypeReference p1 = m1.getParameterType(i);
            final TypeReference p2 = m2.getParameterType(i);
            if (!p1.equals(p2)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOverridable(final IMethod method) {
        return !(method.isInit() || method.isFinal() || method.isStatic() || method.isPrivate());
    }

    public static boolean isPackageVisible(final IMethod method) {
        return !(method.isPublic() || method.isProtected() || method.isPrivate());
    }

    public static boolean isCompilerGeneratedStaticAccessMethod(final IMethod method) {
        if (!method.isStatic()) {
            return false;
        }
        final String methodName = method.getName().toString();
        return methodName.startsWith("access$");
    }

    public static boolean mayHaveSuperDeclaration(final IMethod method) {
        return !(method.isStatic() || method.isPrivate() || method.isInit());
    }

    public static boolean modifiersAllowOverridingMethodDeclaration(final IMethod overriddenDeclarationCandidate,
            final IMethod methodDeclaration) {
        if (!isOverridable(overriddenDeclarationCandidate) || !mayHaveSuperDeclaration(methodDeclaration)) {
            return false;
        } else if (overriddenDeclarationCandidate.isPublic()) {
            return methodDeclaration.isPublic();
        } else if (overriddenDeclarationCandidate.isProtected()) {
            return methodDeclaration.isPublic() || methodDeclaration.isProtected();
        } else if (isPackageVisible(overriddenDeclarationCandidate)) {
            return !methodDeclaration.isPrivate();
        }
        throw throwUnreachable();
    }

    private static void sortMethodsBySignature(final List<IMethod> res) {
        Collections.sort(res, new Comparator<IMethod>() {
            @Override
            public int compare(final IMethod o1, final IMethod o2) {
                return o1.getSignature().compareTo(o2.getSignature());
            }
        });
    }

    /**
     * Searches a method that matches the given parameters within the hierarchy
     * of the given base-class. <b>Note:</b> If the class doe not contain a
     * matching method all its superclasses and all its implemented interfaces
     * are searched (uses wala internal methods that behave in that way).
     * 
     * @param clazz
     *            the class where to start the lookup
     * @param methodReference
     *            the method's name like 'toString' for
     *            java.lang.Object.toString()
     * @param returnType
     *            the concrete typeReference of the return value or {@code null}
     *            if void
     * @param paramTypes
     *            the param types - may be {@code null}.
     * @return a method object declared by clazz or one of its superclasses
     */
    public static IMethod findMethod(final IClass clazz, final String methodReference, TypeName returnType,
            final TypeName[] paramTypes) {
        ensureIsNotNull(clazz, "clazz");
        ensureIsNotNull(methodReference, "methodReference");
        if (returnType == null) {
            returnType = TypeReference.Void.getName();
        }
        final Atom name = Atom.findOrCreateUnicodeAtom(methodReference);
        final Descriptor desc = Descriptor.findOrCreate(paramTypes, returnType);
        return clazz.getMethod(new Selector(name, desc));
    }

    public static boolean isFakeRoot(final CGNode node) {
        ensureIsNotNull(node);
        final IMethod method = node.getMethod();
        final MethodReference reference = method.getReference();
        return FakeRootMethod.isFakeRootMethod(reference);
    }
}

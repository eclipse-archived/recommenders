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
package org.eclipse.recommenders.internal.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmFieldName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.summaries.BypassSyntheticClass;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * Contains utility methods that convert wala method (and type) names to their corresponding recommenders method (and
 * type) name - and maybe reverse.
 * <p>
 * Conversion methods that convert wala names to Strings directly may be added over time. But then use and extend
 * {@link Names} for doing all the string works.
 * 
 * @see Names
 */
public class WalaNameUtils {
    public static Selector rec2walaSelector(final IMethodName method) {
        ensureIsNotNull(method);
        return Selector.make(method.getSignature());
    }

    public static TypeReference rec2walaType(final ITypeName type) {
        ensureIsNotNull(type);
        return TypeReference.findOrCreate(ClassLoaderReference.Application, type.getIdentifier());
    }

    /**
     * Create the WALA TypeReference from a given Java class
     */
    public static com.ibm.wala.types.TypeName java2walaTypeName(final Class<?> javaClazz) {
        ensureIsNotNull(javaClazz);
        return com.ibm.wala.types.TypeName.findOrCreate("L" + javaClazz.getName().replaceAll("\\.", "/"));
    }

    public static IMethodName wala2recMethodName(final IMethod method) {
        ensureIsNotNull(method);
        return VmMethodName.get(wala2vmMethodName(method));
    }

    public static IMethodName wala2recMethodName(final MethodReference method) {
        ensureIsNotNull(method);
        return VmMethodName.get(walaMethodSignature2vmMethodName(method.getSignature()));
    }

    public static ITypeName wala2recTypeName(final IClass clazz) {
        ensureIsNotNull(clazz);
        TypeReference ref = null;
        if (clazz instanceof BypassSyntheticClass) {
            final BypassSyntheticClass syntheticClass = (BypassSyntheticClass) clazz;
            ref = syntheticClass.getRealType().getReference();
        } else {
            ref = clazz.getReference();
        }
        return wala2recTypeName(ref);
    }

    public static ITypeName wala2recTypeName(final TypeReference typeReference) {
        ensureIsNotNull(typeReference);
        final String identifier = typeReference.getName().toString();
        return VmTypeName.get(identifier);
    }

    public static Collection<ITypeName> wala2recTypeNames(final Collection<IClass> classes) {
        ensureIsNotNull(classes);
        final List<ITypeName> res = Lists.newArrayList();
        for (final IClass clazz : classes) {
            final ITypeName typeRef = wala2recTypeName(clazz);
            res.add(typeRef);
        }
        return res;
    }

    private static String wala2vmMethodName(final IMethod method) {
        ensureIsNotNull(method);
        return walaMethodSignature2vmMethodName(method.getSignature());
    }

    private static String walaMethodSignature2vmMethodName(final String walaMethodSignature) {
        ensureIsNotNull(walaMethodSignature);
        final int lastdot = walaMethodSignature.lastIndexOf(".");
        // methodReference with preceding ".":
        final String methodReference = walaMethodSignature.substring(lastdot);
        final String typeReference = "L" + walaMethodSignature.substring(0, lastdot).replaceAll("\\.", "/");
        return typeReference + methodReference;
    }

    public static List<ITypeName> walaFields2recTypeNames(final List<IField> declaredFields) {
        ensureIsNotNull(declaredFields);
        final List<ITypeName> res = Lists.newLinkedList();
        for (final IField f : declaredFields) {
            final TypeReference ref = f.getFieldTypeReference();
            res.add(wala2recTypeName(ref));
        }
        return res;
    }

    public static IFieldName wala2recFieldName(final FieldReference field) {
        ensureIsNotNull(field);
        final String identifier = field.getSignature().replace(' ', ';');
        return VmFieldName.get(identifier);
    }

    /**
     * Wala type: Lorg/class/X --&gt;Lorg.class.X
     */
    public static String wala2srcName(final IClass clazz) {
        ensureIsNotNull(clazz);
        return clazz.getName().toString().replace('/', '.');
    }
}

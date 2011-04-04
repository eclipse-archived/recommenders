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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.recommenders.commons.utils.Fingerprints;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.ClassReader;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

public class ClassUtils {
    public static List<IClass> getAllSuperclasses(final IClass clazz) {
        final List<IClass> res = Lists.newLinkedList();
        for (IClass superclass = clazz.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            res.add(superclass);
        }
        return res;
    }

    public static boolean inSamePackage(final IClass class1, final IClass class2) {
        final String cName1 = class1.getName().toString();
        final String cName2 = class2.getName().toString();
        final String packageClass1 = cName1.substring(1, cName1.lastIndexOf("/"));
        final String packageClass2 = cName2.substring(1, cName2.lastIndexOf("/"));
        return packageClass1.equals(packageClass2);
    }

    /**
     * Tries to resolve the WALA {@link IClass} representation of the given Java
     * class using the given {@link IClassHierarchy}.
     */
    public static IClass findClass(final Class<?> javaClazz, final IClassHierarchy cha) {
        ensureIsNotNull(javaClazz, "javaClazz");
        ensureIsNotNull(cha, "cha");
        final com.ibm.wala.types.TypeName typeReference = WalaNameUtils.java2walaTypeName(javaClazz);
        // try application class loader first:
        final TypeReference typeRef = TypeReference.findOrCreate(ClassLoaderReference.Application, typeReference);
        final IClass res = cha.lookupClass(typeRef);
        return res;
    }

    /**
     * Searches for a class named classReference within the given
     * {@link IClassHierarchy}.
     * <p>
     * <b>Note:</b> Uses the application classloader only! Primordial classes
     * are not resolved.
     */
    public static IClass findClass(final String classReference, final IClassHierarchy cha) {
        ensureIsNotNull(classReference, "classReference");
        ensureIsNotNull(cha, "cha");
        final TypeReference type = TypeReference.findOrCreate(ClassLoaderReference.Application, classReference);
        return cha.lookupClass(type);
    }

    public static IClass findClass(final TypeReference type, final IClassHierarchy cha) {
        return cha.lookupClass(type);
    }

    /**
     * @param nestedClazz
     *            the class that might be nested
     * @param enclosingClazz
     *            the class that might contain the declaration of nestedClass
     */
    public static boolean isNestedClass(final IClass nestedClazz, final IClass enclosingClazz) {
        ensureIsNotNull(nestedClazz);
        ensureIsNotNull(enclosingClazz);
        final String nestedReference = nestedClazz.getReference().getName().toString();
        final String primaryReference = enclosingClazz.getReference().getName().toString();
        return nestedReference.startsWith(primaryReference + "$");
    }

    /**
     * Returns true if the given class belongs to the primordinal (determined by
     * checking the {@link ClassLoaderReference}.
     * 
     * @param clazz
     *            the class to check
     */
    public static boolean isPrimordial(final IClass clazz) {
        ensureIsNotNull(clazz, "clazz");
        return ClassLoaderReference.Primordial.equals(clazz.getClassLoader().getReference());
    }

    public static boolean isPrimordial(final TypeReference typeRef) {
        ensureIsNotNull(typeRef, "typeRef");
        return ClassLoaderReference.Primordial.equals(typeRef.getClassLoader());
    }

    public static boolean isStatic(final IClass definition) {
        return 0 != (definition.getModifiers() & Modifier.STATIC);
    }

    public static String fingerprint(final IClass clazz) {
        if (clazz instanceof ShrikeClass) {
            final ShrikeClass shrike = (ShrikeClass) clazz;
            final ClassReader reader = shrike.getReader();
            final byte[] rawbytes = reader.getBytes();
            final String sha1 = Fingerprints.sha1(new ByteArrayInputStream(rawbytes));
            return sha1;
        }
        return "";
    }
}

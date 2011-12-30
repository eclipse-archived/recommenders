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

import static org.eclipse.recommenders.utils.Throws.throwUnsupportedOperation;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class InheritanceUtils {
    public static List<IClass> getAllSuperclasses(final IClass clazz) {
        final List<IClass> res = Lists.newLinkedList();
        for (IClass superclass = clazz.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            res.add(superclass);
        }
        return res;
    }

    public static Iterable<IClass> iterateSuperclasses(final IClass clazz) {
        new Iterable<IClass>() {
            @Override
            public Iterator<IClass> iterator() {
                return new Iterator<IClass>() {
                    IClass cur = clazz;

                    @Override
                    public boolean hasNext() {
                        return cur != null && cur.getSuperclass() != null;
                    }

                    @Override
                    public IClass next() {
                        cur = cur.getSuperclass();
                        return cur;
                    }

                    @Override
                    public void remove() {
                        throwUnsupportedOperation();
                    }
                };
            }
        };
        final List<IClass> res = Lists.newLinkedList();
        for (IClass superclass = clazz.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            res.add(superclass);
        }
        return res;
    }

    /**
     * returns all subclasses (i.e., all classes that extend the given class
     * directly or indirectly.
     * 
     * @param clazz
     *            the base-class to search its sub-types for
     * @param subclasses
     *            the resulting container
     * @param cha
     *            cha used to lookup the subclasses
     * @param resultFilter
     *            the filter that finally decides whether a subclass should be
     *            added to the result set. This filter does not affect the
     *            traversal.
     */
    public static void findAllSubclasses(final IClass clazz, final List<IClass> subclasses) {
        IClassHierarchy cha = clazz.getClassHierarchy();
        for (final IClass subclazz : cha.getImmediateSubclasses(clazz)) {
            subclasses.add(subclazz);
            findAllSubclasses(subclazz, subclasses);
        }
    }
}

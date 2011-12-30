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
package org.eclipse.recommenders.tests.wala;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JavaLanguage;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

public final class ClassHierarchyDummyClassLoader implements IClassLoader {

    private final BundleClassloaderBasedClassHierarchy cha;
    private final ClassLoaderReference classLoaderReference;

    ClassHierarchyDummyClassLoader(final BundleClassloaderBasedClassHierarchy bundleClassloaderBasedClassHierarchy,
            final ClassLoaderReference classLoaderReference) {
        cha = bundleClassloaderBasedClassHierarchy;
        this.classLoaderReference = classLoaderReference;
    }

    @Override
    public IClass lookupClass(final TypeName className) {
        final TypeReference ref = TypeReference.findOrCreate(classLoaderReference, className);
        return cha.lookupClass(ref);
    }

    @Override
    public ClassLoaderReference getReference() {
        return classLoaderReference;
    }

    @Override
    public Language getLanguage() {
        return JavaLanguage.JAVA;
    }

    @Override
    public SSAInstructionFactory getInstructionFactory() {
        return JavaLanguage.JAVA.instructionFactory();
    }

    @Override
    public void removeAll(final Collection<IClass> toRemove) {
    }

    @Override
    public Iterator<IClass> iterateAllClasses() {
        return null;
    }

    @Override
    public void init(final List<Module> modules) throws IOException {
    }

    @Override
    public String getSourceFileName(final IMethod method, final int offset) {
        return null;
    }

    @Override
    public String getSourceFileName(final IClass klass) throws NoSuchElementException {
        return null;
    }

    @Override
    public InputStream getSource(final IMethod method, final int offset) {
        return null;
    }

    @Override
    public InputStream getSource(final IClass klass) throws NoSuchElementException {
        return null;
    }

    @Override
    public IClassLoader getParent() {
        return null;
    }

    @Override
    public int getNumberOfMethods() {
        return 0;
    }

    @Override
    public int getNumberOfClasses() {
        return 0;
    }

    @Override
    public Atom getName() {
        return null;
    }

}
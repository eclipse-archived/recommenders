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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnsupportedOperation;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultAnalysisScopeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Iterators;
import com.ibm.wala.classLoader.AbstractURLModule;
import com.ibm.wala.classLoader.ArrayClassLoader;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassSyntheticClassLoader;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.shrike.ShrikeClassReaderHandle;

public class BundleClassloaderBasedClassHierarchy implements IClassHierarchy {
	public static IClassHierarchy newInstance(Class<?> clazz) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);

		return new BundleClassloaderBasedClassHierarchy(bundle);
	}

	private final class BinaryUrlModule extends AbstractURLModule {
		private BinaryUrlModule(URL url) {
			super(url);
		}

		@Override
		public boolean isClassFile() {

			return true;
		}

		@Override
		public boolean isSourceFile() {
			return false;
		}
	}

	public static final ClassLoaderReference SYNTETIC = new ClassLoaderReference(
			AnalysisScope.SYNTHETIC, ClassLoaderReference.Java,
			ClassLoaderReference.Application);

	private final IClassLoader bypassLoader;
	private final IClassLoader appLoader;
	private final AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

	private final ArrayClassLoader arrayClassLoader = new ArrayClassLoader();
	private final HashMap<TypeName, IClass> clazzes = new HashMap<TypeName, IClass>();

	private ClassHierarchyDummyClassLoader primordialLoader;

	private final Bundle bundle;

	public BundleClassloaderBasedClassHierarchy(final Bundle bundle) {

		this.bundle = bundle;
		this.primordialLoader = new ClassHierarchyDummyClassLoader(this,
				ClassLoaderReference.Primordial);
		this.appLoader = new ClassHierarchyDummyClassLoader(this,
				ClassLoaderReference.Application);
		this.bypassLoader = new BypassSyntheticClassLoader(SYNTETIC, appLoader,
				null, this);

	}

	@Override
	public boolean addClass(final IClass clazz) {
		return false;
	}

	@Override
	public IClass lookupClass(final TypeReference typeRef) {
		if (isClassAlreadyLoaded(typeRef)) {
			return ensureIsNotNull(clazzes.get(typeRef.getName()));
		}
		if (typeRef.isArrayType()) {
			return ensureIsNotNull(arrayClassLoader.lookupClass(
					typeRef.getName(), appLoader, this));
		}
		if (typeRef.getClassLoader().equals(SYNTETIC)) {
			return ensureIsNotNull(loadSynteticType(typeRef));
		}

		String string = typeRef.getName().toString();
		if (string.startsWith("Ljava")) {
			return loadClass(typeRef.getName(), primordialLoader);
		} else
			return loadClass(typeRef.getName(), appLoader);
	}

	private IClass loadClass(TypeName className, IClassLoader cl) {

		try {
			String classNameString = className.toString();
			String name = classNameString.substring(1) + ".class";
			URL resource = bundle.getResource(name);
			if (resource == null)
				return null;
			ShrikeClassReaderHandle handle = new ShrikeClassReaderHandle(
					new BinaryUrlModule(resource));
			ShrikeClass shrikeClass = new ShrikeClass(handle, cl, this);
			clazzes.put(className, shrikeClass);
			return shrikeClass;
		} catch (InvalidClassFileException e) {
			throw new IllegalArgumentException();
		}

	}

	private IClass loadSynteticType(final TypeReference typeRef) {
		final IClass lookupClass = bypassLoader.lookupClass(typeRef.getName());
		clazzes.put(typeRef.getName(), lookupClass);
		return lookupClass;
	}

	private boolean isClassAlreadyLoaded(final TypeReference A) {
		return clazzes.containsKey(A.getName());
	}

	@Override
	public IClassLoader getLoader(final ClassLoaderReference loaderRef) {
		if ("Synthetic".equals(loaderRef.getName().toString())) {
			return bypassLoader;
		} else
			return appLoader;
	}

	@Override
	public IClass getRootClass() {
		return lookupClass(TypeReference.JavaLangObject);
	}

	@Override
	public AnalysisScope getScope() {
		return scope;
	}

	@Override
	public boolean isRootClass(final IClass c) {
		return TypeReference.JavaLangObject.equals(c.getReference());
	}

	@Override
	public boolean isSubclassOf(final IClass c, final IClass T) {
		IClass current = c;
		while (null != current
				&& !TypeReference.JavaLangObject.equals(current.getReference())) {
			if (current == T) {
				return true;
			}
			current = current.getSuperclass();
		}
		return false;
	}

	public boolean isSyntheticClass(final IClass c) {
		throw throwUnsupportedOperation();
	}

	@Override
	public IField resolveField(final FieldReference f) {
		final IClass clazz = lookupClass(f.getDeclaringClass());
		if (null == clazz) {
			return null;
		}
		return clazz.getField(f.getName());
	}

	@Override
	public IField resolveField(final IClass klass, final FieldReference f) {
		throw throwUnsupportedOperation();
	}

	@Override
	public IMethod resolveMethod(final MethodReference m) {
		final IClass clazz = lookupClass(m.getDeclaringClass());
		if (null == clazz) {
			return null;
		}
		return clazz.getMethod(m.getSelector());
	}

	@Override
	public IMethod resolveMethod(final IClass receiverClass,
			final Selector selector) {
		return receiverClass.getMethod(selector);
	}

	@Override
	public Iterator<IClass> iterator() {
		return Iterators.emptyIterator();
	}

	@Override
	public Collection<IClass> computeSubClasses(final TypeReference type) {
		throw throwUnsupportedOperation();
	}

	@Override
	public ClassLoaderFactory getFactory() {
		return null;
	}

	@Override
	public Collection<IClass> getImmediateSubclasses(final IClass klass) {
		throw throwUnsupportedOperation();
	}

	@Override
	public Set<IClass> getImplementors(final TypeReference type) {
		throw throwUnsupportedOperation();
	}

	@Override
	public Collection<TypeReference> getJavaLangErrorTypes() {
		return Collections.emptyList();
	}

	@Override
	public Collection<TypeReference> getJavaLangRuntimeExceptionTypes() {

		return Collections.emptyList();
	}

	@Override
	public IClass getLeastCommonSuperclass(final IClass A, final IClass B) {
		throw throwUnsupportedOperation();
	}

	@Override
	public TypeReference getLeastCommonSuperclass(final TypeReference A,
			final TypeReference B) {
		throw throwUnsupportedOperation();
	}

	@Override
	public IClassLoader[] getLoaders() {
		throw throwUnsupportedOperation();
	}

	@Override
	public int getNumber(final IClass c) {
		throw throwUnsupportedOperation();
	}

	@Override
	public int getNumberOfClasses() {
		return 0;
	}

	@Override
	public int getNumberOfImmediateSubclasses(final IClass klass) {
		throw throwUnsupportedOperation();
	}

	@Override
	public Collection<IMethod> getPossibleTargets(final MethodReference ref) {
		throw throwUnsupportedOperation();
	}

	@Override
	public Set<IMethod> getPossibleTargets(final IClass receiverClass,
			final MethodReference ref) {
		throw throwUnsupportedOperation();
	}

	@Override
	public boolean implementsInterface(final IClass c, final IClass i) {
		throw throwUnsupportedOperation();
	}

	@Override
	public boolean isAssignableFrom(final IClass c1, final IClass c2) {
		return isSubclassOf(c2, c1);
	}

	@Override
	public boolean isInterface(final TypeReference type) {
		throw throwUnsupportedOperation();
	}

}

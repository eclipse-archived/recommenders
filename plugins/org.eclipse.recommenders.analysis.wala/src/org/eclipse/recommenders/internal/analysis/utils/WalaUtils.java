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

import static org.eclipse.recommenders.internal.analysis.utils.ClassUtils.isPrimordial;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.recommenders.utils.IGenericFilter;
import org.eclipse.recommenders.utils.Throws;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.util.debug.UnimplementedError;

public class WalaUtils {
    public static class IClassComparator implements Comparator<IClass> {
        @Override
        public int compare(final IClass o1, final IClass o2) {
            return o1.getReference().toString().compareTo(o2.getReference().toString());
        }
    }

    public static com.ibm.wala.types.TypeName OBJECT = com.ibm.wala.types.TypeName.findOrCreate("Ljava/lang/Object");

    public static final IGenericFilter<IClass> F2_DENY_ABSTRACT_CLASSES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return elem.isAbstract() ? Decision.DENY : Decision.NEUTRAL;
        }
    };

    public static final IGenericFilter<IClass> F2_DENY_INTERFACES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return elem.isInterface() ? Decision.DENY : Decision.NEUTRAL;
        }
    };

    public static final IGenericFilter<IClass> F2_DENY_PRIMORDIAL_CLASSES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return isPrimordial(elem) ? Decision.DENY : Decision.NEUTRAL;
        }

        @Override
        public String toString() {
            return "F2_DENIES_PRIMORDIAL_CLASSES";
        }
    };

    public static final IGenericFilter<IClass> F2_DENY_NON_ECLIPSE_UI_CLASSES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return !elem.getReference().toString().contains("/ui/") ? Decision.DENY : Decision.NEUTRAL;
        }

        @Override
        public String toString() {
            return this.getClass().getName() + "F2_DENY_NON_ECLIPSE_UI_CLASSES";
        }
    };

    public static final IGenericFilter<IMethod> F2_DENIES_NON_OVERRIDING_METHODS = new IGenericFilter<IMethod>() {
        @Override
        public IGenericFilter.Decision decide(final IMethod elem) {
            if (MethodUtils.findSuperDeclaration(elem) != null) {
                return Decision.ACCEPT;
            }
            return Decision.DENY;
        }
    };

    public static final IGenericFilter<IMethod> F2_ACCEPT_OVERRIDING_METHODS = new IGenericFilter<IMethod>() {
        @Override
        public IGenericFilter.Decision decide(final IMethod elem) {
            if (MethodUtils.findSuperDeclaration(elem) != null) {
                return Decision.ACCEPT;
            }
            return Decision.NEUTRAL;
        }
    };

    public static final IGenericFilter<IMethod> F2_ACCEPT_SUBCLASS_CONSTRUCTORS = new IGenericFilter<IMethod>() {
        @Override
        public IGenericFilter.Decision decide(final IMethod elem) {
            if (!elem.isInit()) {
                return Decision.NEUTRAL;
            }
            IClass superclass;
            superclass = elem.getDeclaringClass().getSuperclass();
            if (superclass == null || superclass.getName() == OBJECT) {
                return Decision.DENY;
            } else {
                return Decision.ACCEPT;
            }
        }
    };

    public static final IGenericFilter<IMethod> F2_DENY_NON_PUBLIC_METHODS = new IGenericFilter<IMethod>() {
        @Override
        public IGenericFilter.Decision decide(final IMethod elem) {
            if (!elem.isPublic()) {
                return Decision.DENY;
            }
            return Decision.NEUTRAL;
        }
    };

    public static final IGenericFilter<IMethod> F2_DENY_ALL_METHODS = new IGenericFilter<IMethod>() {
        @Override
        public IGenericFilter.Decision decide(final IMethod elem) {
            return Decision.DENY;
        }
    };

    public static final IGenericFilter<IClass> F2_DENY_ALL_CLASSES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return Decision.DENY;
        }
    };

    public static final IGenericFilter<IClass> F2_DENY_FINAL_CLASSES = new IGenericFilter<IClass>() {
        @Override
        public IGenericFilter.Decision decide(final IClass elem) {
            return Modifier.isFinal(elem.getModifiers()) ? Decision.DENY : Decision.NEUTRAL;
        }
    };

    private final static class JarFileFileFilter implements IOFileFilter {
        @Override
        public boolean accept(final File file) {
            return file.getName().endsWith(".jar");
        }

        @Override
        public boolean accept(final File dir, final String name) {
            return false;
        }
    }

    private final static class AllDirsOnlyOnceFileFilter implements IOFileFilter {
        Set<File> history = Sets.newHashSet();

        @Override
        public boolean accept(final File file) {
            try {
                return history.add(file.getCanonicalFile());
            } catch (final IOException e) {
                throw Throws.throwUnhandledException(e);
            }
        }

        @Override
        public boolean accept(final File dir, final String name) {
            return accept(dir);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<File> getAllJarsInDirectoryRecursively(final File directory) {
        // not working with OSX circular directory links
        // return FileUtils.listFiles(directory, new String[] { "jar" },
        // recursively);
        return FileUtils.listFiles(directory, new JarFileFileFilter(), new AllDirsOnlyOnceFileFilter());
    }

    public static RuntimeException throwWalaFailedUnexpectedlyException(final UnimplementedError x) {
        throw new RuntimeException("Wala Bytecode Analysis Failed. Please report this issue.", x);
    }
}

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
package org.eclipse.recommenders.internal.rcp;

import java.util.List;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.utils.ImageImageDescriptor;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings({ "rawtypes", "restriction" })
@Singleton
@Clumsy
public class CodeElementsAdapterFactory implements IAdapterFactory {

    private final JavaElementResolver resolver;

    private final JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();

    @Inject
    public CodeElementsAdapterFactory(final JavaElementResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {
        if (adaptableObject instanceof CompilationUnit) {
            return handleCompilationUnit((CompilationUnit) adaptableObject);
        } else if (adaptableObject instanceof TypeDeclaration) {
            return handleTypeDeclaration((TypeDeclaration) adaptableObject);
        } else if (adaptableObject instanceof MethodDeclaration) {
            return handleMethodDeclaration((MethodDeclaration) adaptableObject);
        } else if (adaptableObject instanceof ObjectInstanceKey) {
            return handleObjectInstanceKey((ObjectInstanceKey) adaptableObject);
        } else if (adaptableObject instanceof Variable) {
            return handleVariable((Variable) adaptableObject);
        } else if (adaptableObject instanceof ReceiverCallSite) {
            return handleReceiverCallSite((ReceiverCallSite) adaptableObject);
        }
        return null;
    }

    private Object handleCompilationUnit(final CompilationUnit cu) {
        return new IWorkbenchAdapter() {
            final IType jdt = resolver.toJdtType(cu.primaryType.name);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                return new Object[] { cu.primaryType };
            }
        };
    }

    private Object handleTypeDeclaration(final TypeDeclaration type) {
        return new IWorkbenchAdapter() {
            final IType jdt = resolver.toJdtType(type.name);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                final List<Object> children = Lists.newLinkedList();
                children.addAll(type.memberTypes);
                children.addAll(type.methods);
                return children.toArray();
            }
        };
    }

    private Object handleMethodDeclaration(final MethodDeclaration method) {
        return new IWorkbenchAdapter() {
            final IMethod jdt = resolver.toJdtMethod(method.name);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                final List<Object> children = Lists.newLinkedList();
                children.addAll(method.objects);
                // children.addAll(method.getVariables());
                children.addAll(method.nestedTypes);
                return children.toArray();
            }
        };
    }

    private Object handleObjectInstanceKey(final ObjectInstanceKey key) {
        return new IWorkbenchAdapter() {
            final LocalVariable jdt = new LocalVariable(null, Iterables.getFirst(key.names, "unnamed"), -1, -1, -1, -1,
                    key.type.getIdentifier(), null);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                return key.receiverCallSites.toArray();
            }
        };
    }

    private Object handleVariable(final Variable key) {
        return new IWorkbenchAdapter() {
            final LocalVariable jdt = new LocalVariable(null, key.name, -1, -1, -1, -1, key.type.getIdentifier(), null);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                return new Object[0];
            }
        };
    }

    private Object handleReceiverCallSite(final ReceiverCallSite callsite) {
        return new IWorkbenchAdapter() {
            final IMethod jdt = resolver.toJdtMethod(callsite.targetMethod);

            @Override
            public Object getParent(final Object o) {
                return null;
            }

            @Override
            public String getLabel(final Object o) {
                return safeGetLabel(jdt, o);
            }

            @Override
            public ImageDescriptor getImageDescriptor(final Object object) {
                return safeGetImage(jdt);
            }

            @Override
            public Object[] getChildren(final Object o) {
                return new Object[0];
            }
        };
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }

    private ImageDescriptor safeGetImage(final IJavaElement jdt) {
        final Image image = labelProvider.getImage(jdt);
        return image == null ? null : new ImageImageDescriptor(image);
    }

    private String safeGetLabel(final IJavaElement jdt, final Object o) {
        if (jdt == null) {
            if (o instanceof TypeDeclaration) {
                return ((TypeDeclaration) o).name.getClassName();
            } else if (o instanceof MethodDeclaration) {
                final IMethodName name = ((MethodDeclaration) o).name;
                return Names.vm2srcSimpleMethod(name);
            }
            return o.toString();
        }
        return labelProvider.getText(jdt);
    }
}

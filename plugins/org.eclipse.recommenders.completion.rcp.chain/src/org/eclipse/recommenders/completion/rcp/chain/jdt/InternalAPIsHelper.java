/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.chain.jdt;

import static org.eclipse.jdt.internal.corext.util.JdtFlags.isPublic;
import static org.eclipse.jdt.internal.corext.util.JdtFlags.isStatic;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Optional;

public class InternalAPIsHelper {

    private static final Util.BindingsToNodesMap EmptyNodeMap = new Util.BindingsToNodesMap() {
        @Override
        public ASTNode get(final Binding binding) {
            return null;
        }
    };

    /**
     * Returns a list of all public instance methods and fields declared in the given type or any of its super-types
     */
    public static Collection<IMember> findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods(final IType returnType) {
        final LinkedHashMap<String, IMember> tmp = new LinkedHashMap<String, IMember>();

        try {
            final IType[] returnTypeAndSupertypes = findAllSupertypesIncludeingArgument(returnType);
            for (final IType type : returnTypeAndSupertypes) {
                for (final IMethod m : type.getMethods()) {
                    if (isVoid(m) || !isPublic(m) || m.isConstructor() || isStatic(m) || hasPrimitiveReturnType(m)) {
                        continue;
                    }
                    final String key = createMethodKey(m);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, m);
                    }
                }
                for (final IField field : type.getFields()) {
                    if (!isPublic(field) || isStatic(field)) {
                        continue;
                    }
                    final String key = createFieldKey(field);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, field);
                    }
                }
            }
        } catch (final JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmp.values();
    }

    private static IType[] findAllSupertypesIncludeingArgument(final IType returnType) throws JavaModelException {

        final ITypeHierarchy typeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(returnType);
        final IType[] allSupertypes = typeHierarchy.getAllSupertypes(returnType);

        // TODO: use ArrayUtils.add ... commons.lang3
        final IType[] returnTypeAndSupertypes = new IType[allSupertypes.length + 1];
        System.arraycopy(allSupertypes, 0, returnTypeAndSupertypes, 1, allSupertypes.length);
        returnTypeAndSupertypes[0] = returnType;
        return returnTypeAndSupertypes;
    }

    /**
     * Returns a list of all public static fields and methods declared in the given class (but not its super-classes)
     * TODO: superclasses not, should we add this?
     */
    public static List<IMember> findAllPublicStaticFieldsAndNonVoidNonPrimitiveMethods(final IType type) {
        final List<IMember> res = new LinkedList<IMember>();
        try {
            for (final IMethod m : type.getMethods()) {
                if (isStatic(m) && isPublic(m) && !isVoid(m) && !hasPrimitiveReturnType(m)) {
                    res.add(m);
                }
            }
            for (final IField f : type.getFields()) {
                if (isStatic(f) && isPublic(f)) {
                    res.add(f);
                }
            }
        } catch (final JavaModelException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean isVoid(final IMethod method) throws JavaModelException {
        return Signature.SIG_VOID.equals(method.getReturnType());
    }

    public static boolean hasPrimitiveReturnType(final IMethod method) throws JavaModelException {
        return !method.getReturnType().endsWith(";");
    }

    public static boolean isAssignable(final IType lhsType, final IType rhsType) {
        ensureIsNotNull(lhsType);
        ensureIsNotNull(rhsType);

        try {
            final IType[] supertypes = findAllSupertypesIncludeingArgument(rhsType);
            for (final IType supertype : supertypes) {
                if (supertype.equals(lhsType)) {
                    return true;
                }
            }
            return false;
        } catch (final JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private static String createMethodKey(final IMethod method) throws JavaModelException {
        final String signature = method.getSignature();
        final String signatureWithoutReturnType = substringBeforeLast(signature, ")");
        final String methodName = method.getElementName();
        return methodName + signatureWithoutReturnType;
    }

    private static String createFieldKey(final IField field) throws JavaModelException {
        return field.getElementName() + field.getTypeSignature();
    }

    public static TemplateProposal createTemplateProposal(final Template template,
            final JavaContentAssistInvocationContext contentAssistContext) {
        final DocumentTemplateContext javaTemplateContext = createJavaContext(contentAssistContext);
        final TemplateProposal proposal = new TemplateProposal(template, javaTemplateContext, new Region(
                javaTemplateContext.getCompletionOffset(), javaTemplateContext.getCompletionLength()),
                getChainCompletionIcon());
        return proposal;
    }

    public static JavaContext createJavaContext(final JavaContentAssistInvocationContext contentAssistContext) {
        final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
        final TemplateContextType templateContextType = templateContextRegistry.getContextType(JavaContextType.ID_ALL);
        final JavaContext javaTemplateContext = new JavaContext(templateContextType,
                contentAssistContext.getDocument(), contentAssistContext.getInvocationOffset(), contentAssistContext
                        .getCoreContext().getToken().length, contentAssistContext.getCompilationUnit());
        javaTemplateContext.setForceEvaluation(true);
        return javaTemplateContext;
    }

    public static Image getChainCompletionIcon() {
        return JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_MISC_PUBLIC);
    }

    public static Optional<IType> findTypeFromSignature(final String typeSignature, final IJavaElement parent) {
        try {
            final String resolvedTypeSignature = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(
                    typeSignature, parent);
            final IType res = parent.getJavaProject().findType(resolvedTypeSignature);
            return Optional.fromNullable(res);
        } catch (final JavaModelException e) {
            // TODO log that exception
            e.printStackTrace();
            return Optional.absent();
        }
    }

    public static Optional<IType> findTypeOfField(final IField field) {
        try {
            return findTypeFromSignature(field.getTypeSignature(), field);
        } catch (final JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Optional.absent();
        }
    }

    public static String resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(String typeSignature,
            final IJavaElement parent) throws JavaModelException {
        typeSignature = typeSignature.replace('/', '.');
        final IType type = (IType) parent.getAncestor(IJavaElement.TYPE);
        typeSignature = JavaModelUtil.getResolvedTypeName(typeSignature, type);
        // NOT needed. Done by getResolvedTypeName typeSignature = StringUtils.substringBefore(typeSignature, "[");
        typeSignature = substringBeforeLast(typeSignature, "<");
        return typeSignature;
    }

    private static String substringBeforeLast(String typeSignature, final String separator) {
        final int lastIndexOf = typeSignature.lastIndexOf(separator);
        if (lastIndexOf > -1) {
            typeSignature = typeSignature.substring(0, lastIndexOf);
        }
        return typeSignature;
    }

    public static IType createUnresolvedType(final TypeBinding b) {
        return (IType) Util.getUnresolvedJavaElement(b, null, EmptyNodeMap);
    }

    public static IMethod createUnresolvedMethod(final MethodBinding methodBinding) {
        ensureIsNotNull(methodBinding);
        return (IMethod) Util.getUnresolvedJavaElement(methodBinding, null, EmptyNodeMap);
    }

    public static IField createUnresolvedField(final FieldBinding field) {
        ensureIsNotNull(field);
        return (IField) Util.getUnresolvedJavaElement(field, null, EmptyNodeMap);
    }

    public static ILocalVariable createUnresolvedLocaVariable(final VariableBinding var, final JavaElement parent) {
        ensureIsNotNull(var);
        ensureIsNotNull(parent);

        final String name = new String(var.name);
        final String type = new String(var.type.signature());
        return new LocalVariable(parent, name, 0, 0, 0, 0, type, null, var.modifiers, var.isParameter());
    }

}

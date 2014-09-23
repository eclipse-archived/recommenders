/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static com.google.common.base.Optional.of;
import static org.eclipse.jdt.core.CompletionProposal.*;
import static org.eclipse.jdt.core.compiler.CharOperation.NO_CHAR;
import static org.eclipse.jdt.internal.codeassist.CompletionEngine.createTypeSignature;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

/**
 * EXPERIMENTAL. Not recommended for public API use.
 */
@SuppressWarnings("restriction")
@Beta
public class AccessibleCompletionProposals {

    public static AccessibleCompletionProposal newFieldRef(IField field, int completionOffset, int prefixLength,
            int relevance) {
        AccessibleCompletionProposal proposal = new AccessibleCompletionProposal(CompletionProposal.FIELD_REF,
                completionOffset);
        String typeSignature = "";
        try {
            typeSignature = field.getTypeSignature();
        } catch (JavaModelException e1) {
            // TODO log
        }
        String simpleName = Signature.getSignatureSimpleName(typeSignature);
        proposal.setTypeName(simpleName.toCharArray());

        proposal.setSignature(typeSignature.toCharArray());
        // proposal.setPackageName(local.type.qualifiedPackageName());

        proposal.setName(field.getElementName().toCharArray());
        proposal.setCompletion(field.getElementName().toCharArray());
        try {
            proposal.setFlags(field.getFlags());
        } catch (JavaModelException e) {
            // TODO log
        }
        int replaceStartIndex = completionOffset - prefixLength;
        int replaceEndIndex = completionOffset;
        proposal.setReplaceRange(replaceStartIndex, replaceEndIndex);
        // TODO we may need to respect ui settings here:
        int tokenStartIndex = replaceStartIndex;
        int tokenEndIndex = replaceEndIndex;
        proposal.setTokenRange(tokenStartIndex, tokenEndIndex);
        proposal.setRelevance(relevance);

        proposal.setData(IField.class, field);
        proposal.setData(IJavaProject.class, field.getJavaProject());
        return proposal;
    }

    public static AccessibleCompletionProposal newLocalRef(ILocalVariable local, int completionOffset,
            int prefixLength, int relevance) {
        AccessibleCompletionProposal proposal = new AccessibleCompletionProposal(CompletionProposal.LOCAL_VARIABLE_REF,
                completionOffset);
        String typeSignature = local.getTypeSignature();
        String simpleName = Signature.getSignatureSimpleName(typeSignature);
        proposal.setTypeName(simpleName.toCharArray());

        proposal.setSignature(typeSignature.toCharArray());
        // proposal.setPackageName(local.type.qualifiedPackageName());

        proposal.setName(local.getElementName().toCharArray());
        proposal.setCompletion(local.getElementName().toCharArray());
        proposal.setFlags(local.getFlags());
        int replaceStartIndex = completionOffset - prefixLength;
        int replaceEndIndex = completionOffset;
        proposal.setReplaceRange(replaceStartIndex, replaceEndIndex);
        // TODO we may need to respect ui settings here:
        int tokenStartIndex = replaceStartIndex;
        int tokenEndIndex = replaceEndIndex;
        proposal.setTokenRange(tokenStartIndex, tokenEndIndex);
        proposal.setRelevance(relevance);

        return proposal;
    }

    public static AccessibleCompletionProposal newLocalRef(LocalVariableBinding local, int completionOffset,
            int prefixLength, int relevance) {
        // JDT sets these fields as well.
        // proposal.nameLookup = this.nameEnvironment.nameLookup;
        // proposal.completionEngine = this;

        AccessibleCompletionProposal proposal = new AccessibleCompletionProposal(CompletionProposal.LOCAL_VARIABLE_REF,
                completionOffset);

        if (local.type == null) {
            proposal.setTypeName(local.declaration.type.toString().toCharArray());
            proposal.setSignature(createTypeSignature(NO_CHAR, local.declaration.type.toString().toCharArray()));
        } else {
            proposal.setTypeName(local.type.qualifiedSourceName());
            proposal.setPackageName(local.type.qualifiedPackageName());
            proposal.setSignature(CompletionEngine.getSignature(local.type));
        }

        proposal.setName(local.name);
        proposal.setCompletion(local.name);
        proposal.setFlags(local.modifiers);
        int replaceStartIndex = completionOffset - prefixLength;
        int replaceEndIndex = completionOffset;
        proposal.setReplaceRange(replaceStartIndex, replaceEndIndex);
        // TODO we may need to respect ui settings here:
        int tokenStartIndex = replaceStartIndex;
        int tokenEndIndex = replaceEndIndex;
        proposal.setTokenRange(tokenStartIndex, tokenEndIndex);
        proposal.setRelevance(relevance);

        return proposal;
    }

    public static AccessibleCompletionProposal newTypeImport(ITypeName type) {
        // we can't import array types. So, strip it off
        if (type.isArrayType()) {
            type = type.getArrayBaseType();
        }
        char[] signature = (Names.vm2srcQualifiedType(type) + ";").toCharArray();

        AccessibleCompletionProposal res = new AccessibleCompletionProposal(TYPE_IMPORT, 0);
        res.setSignature(signature);
        return res;
    }

    public static AccessibleCompletionProposal newQualifiedFieldRef(IFieldName field, int completionOffset,
            int prefixLength, int relevance) {

        String declaringType = field.getDeclaringType().getClassName();
        String fieldName = field.getFieldName();
        String completion = declaringType + "." + fieldName;

        char[] signature = (field.getDeclaringType().getIdentifier().replace('/', '.') + ";").toCharArray();
        CompletionProposal fieldRef = new AccessibleCompletionProposal(FIELD_REF, 0);
        fieldRef.setDeclarationSignature(signature);
        fieldRef.setName(fieldName.toCharArray());
        fieldRef.setReplaceRange(completionOffset - prefixLength, completionOffset);
        fieldRef.setRequiredProposals(new CompletionProposal[] { newTypeImport(field.getDeclaringType()) });
        AccessibleCompletionProposal res = new AccessibleCompletionProposal(TYPE_IMPORT, completionOffset);
        res.setCompletion(completion.toCharArray());
        res.setSignature(signature);
        res.setRelevance(relevance);
        return res;
    }

    public static Optional<AccessibleCompletionProposal> newMethodRef(IMethod method, int completionOffset,
            int prefixLength, int relevance) {

        String completion = method.getElementName() + "()";
        String[] params = method.getParameterTypes();
        String[] resolved = new String[params.length];
        IType declaringType = method.getDeclaringType();
        // boolean isVararg = Flags.isVarargs(method.getFlags());
        int lastParam = params.length - 1;
        for (int i = 0; i <= lastParam; i++) {
            String resolvedParam = params[i];
            String unresolvedParam = params[i];
            resolvedParam = resolveToBinary(unresolvedParam, declaringType).orNull();
            if (resolvedParam == null) {
                return Optional.absent();
            }
            resolved[i] = resolvedParam;
            // TODO no varargs support
        }

        String returnType;
        try {
            returnType = resolveToBinary(method.getReturnType(), declaringType).orNull();
        } catch (JavaModelException e) {
            e.printStackTrace();
            return Optional.absent();
        }
        if (returnType == null) { // e.g. a type parameter "QE;"
            return Optional.absent();
        }

        AccessibleCompletionProposal res = new AccessibleCompletionProposal(CompletionProposal.METHOD_REF, relevance);
        try {
            res.setFlags(method.getFlags());
        } catch (JavaModelException e1) {
            e1.printStackTrace();
        }
        res.setCompletion(completion.toCharArray());
        String declarationSignature = Signature.createTypeSignature(method.getDeclaringType().getFullyQualifiedName(),
                true);
        res.setDeclarationSignature(declarationSignature.toCharArray());
        String signature = Signature.createMethodSignature(resolved, returnType);
        res.setSignature(signature.toCharArray());
        res.setName(method.getElementName().toCharArray());
        res.setReplaceRange(completionOffset - prefixLength, completionOffset);

        char[][] paramNames = new char[params.length][];
        try {
            String[] parameterNames = method.getParameterNames();
            for (int i = 0; i < paramNames.length; i++) {
                paramNames[i] = parameterNames[i].toCharArray();
            }
        } catch (JavaModelException e) {
            e.printStackTrace();
            paramNames = CompletionEngine.createDefaultParameterNames(params.length);
        }
        res.setParameterNames(paramNames);

        res.setData(IMethod.class, method);
        res.setData(IJavaProject.class, method.getJavaProject());
        return of(res);
    }

    private static Optional<String> resolveToBinary(String unresolvedParam, IType declaringType) {
        if (unresolvedParam.length() == 1) {
            return of(unresolvedParam);
        }
        if (Signature.C_RESOLVED == unresolvedParam.charAt(0)) {
            return of(unresolvedParam);
        }

        String curr = Signature.getTypeErasure(unresolvedParam);

        try {
            curr = JavaModelUtil.getResolvedTypeName(curr, declaringType);
        } catch (JavaModelException e) {
            e.printStackTrace();
            return Optional.absent();
        }
        if (curr == null) { // e.g. a type parameter "QE;"
            return Optional.absent();
        }
        return of("L" + curr + ";");
    }

}

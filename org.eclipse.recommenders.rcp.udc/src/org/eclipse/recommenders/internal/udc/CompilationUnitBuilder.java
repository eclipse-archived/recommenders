/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc;

import java.util.Date;

import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmFieldName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeReference;

import com.google.common.collect.Sets;

public class CompilationUnitBuilder {
    public CompilationUnitBuilder() {
        unit.analysedOn = new Date(0);
        unit.creationTimestamp = new Date(0);
    }

    CompilationUnit unit = CompilationUnit.create();

    public TypeReference addImport(final String type, final String fingerPrint) {
        final TypeReference reference = createTypeReference(type, fingerPrint);
        unit.imports.add(reference);
        return reference;
    }

    public static TypeReference createTypeReference(final String type, final String fingerPrint) {
        return TypeReference.create(getTypeName(type), fingerPrint);
    }

    private static ITypeName getTypeName(final String type) {
        return VmTypeName.get(type);
    }

    public void setPrimaryType(final String typeName, String superClassTypeName) {
        if (superClassTypeName == null) {
            superClassTypeName = "Ljava/lang/Object";
        }
        unit.primaryType = TypeDeclaration.create(getTypeName(typeName), getTypeName(superClassTypeName));
    }

    public MethodDeclaration addMethod(final String methodName, final int line, final String superDeclaration,
            final String firstDeclaration, final int... modifiers) {
        final MethodDeclaration method = MethodDeclaration.create(getMethodName(getPrimaryTypeName(), methodName));
        method.objects = Sets.newLinkedHashSet();
        method.line = line;

        if (superDeclaration != null) {
            method.superDeclaration = getMethodName(superDeclaration);
        }
        if (firstDeclaration != null) {
            method.firstDeclaration = getMethodName(firstDeclaration);
        }

        for (final int modifier : modifiers) {
            method.modifiers = method.modifiers | modifier;
        }

        unit.primaryType.methods.add(method);
        return method;
    }

    private IMethodName getMethodName(final String methodName) {
        return VmMethodName.get(methodName);
    }

    private IMethodName getMethodName(final ITypeName type, final String method) {
        return VmMethodName.get(type.getIdentifier(), method);
    }

    private ITypeName getPrimaryTypeName() {
        return unit.primaryType.name;
    }

    public void setPrimaryTypeName(final String name) {
        unit.primaryType.name = getTypeName(name);
    }

    public ObjectInstanceKey addObject(final String targetMethod, final String objectType, final Kind kind,
            final String... names) {
        final MethodDeclaration method = unit.findMethod(getMethodName(getPrimaryTypeName(), targetMethod));
        if (method == null) {
            throw new IllegalArgumentException("the method " + targetMethod + " does not exist");
        }
        final ObjectInstanceKey objectInstanceKey = ObjectInstanceKey.create(getTypeName(objectType), kind);
        objectInstanceKey.names = Sets.newHashSet(names);
        objectInstanceKey.definitionSite = DefinitionSite.newSite(DefinitionSite.Kind.FIELD);
        method.objects.add(objectInstanceKey);
        return objectInstanceKey;
    }

    public void addFields(final String... fields) {
        for (final String field : fields) {
            unit.primaryType.fields.add(getTypeName(field));
        }
    }

    public void addInterfaces(final String... interfaces) {
        for (final String i : interfaces) {
            unit.primaryType.interfaces.add(getTypeName(i));
        }
    }

    public CompilationUnit getCompilationUnit() {
        return unit;
    }

    public ParameterCallSite addParameterCallSite(final ObjectInstanceKey object, final String targetTypeName,
            final String targetMethod, final String argumentName, final int argumentIndex, final String sourceMethod,
            final int line) {
        final IMethodName targetMethodName = getMethodName(getTypeName(targetTypeName), targetMethod);
        final IMethodName sourceMethodName = getMethodName(getPrimaryTypeName(), sourceMethod);
        final ParameterCallSite site = ParameterCallSite.create(argumentName, targetMethodName, argumentIndex,
                sourceMethodName, line);
        object.parameterCallSites.add(site);
        return site;
    }

    public ReceiverCallSite addReceiverCallSite(final ObjectInstanceKey object, final String reveiver,
            final String targetTypeName, final String targetMethod, final String sourceMethod, final int line) {
        final IMethodName targetMethodName = getMethodName(getTypeName(targetTypeName), targetMethod);
        final IMethodName sourceMethodName = getMethodName(getPrimaryTypeName(), sourceMethod);
        final ReceiverCallSite site = ReceiverCallSite.create(reveiver, targetMethodName, sourceMethodName, line);
        object.receiverCallSites.add(site);
        return site;
    }

    public DefinitionSite setDefinitionSite(final ObjectInstanceKey object, final DefinitionSite.Kind kind,
            final String fieldName, final String typeName, final int line) {
        final IFieldName field = createFildName(fieldName, typeName);
        final DefinitionSite site = DefinitionSite.create(field);
        site.kind = kind;
        site.lineNumber = line;
        return site;
    }

    private IFieldName createFildName(final String fieldName, final String fieldType) {
        final String field = unit.primaryType.name.getIdentifier() + "." + fieldName + ";" + fieldType;
        return VmFieldName.get(field);
    }
}

/**
 * Copyright (c) 2011 Andreas Frankenberger. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.depersonalizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeReference;
import org.eclipse.recommenders.internal.udc.CompilationUnitBuilder;

public class CompilationUnitProvider {
    public static final String MONITOR_OBJECT = "monitor";
    public static final String EXPORT_UNITS_METHOD = "exportUnits(Lorg/eclipse/core/resources/IProject;Ljava/util/List;Lorg/eclipse/core/runtime/IProgressMonitor;)V";
    public static final String THIS_OBJECT = "this";
    public static final String UNITS_OBJECT = "units";
    public static final String TRY_SEND_DATA_METHOD = "trySendData(Ljava/util/List;)V";
    public static final String OBJECT = "Ljava/lang/Object";
    public static final String LIST = "Ljava/util/List";
    public static final String IPROJECT = "Lorg/eclipse/core/resources/IProject";
    public static final String IPROGRESS_MONITOR = "Lorg/eclipse/core/runtime/IProgressMonitor";
    public static final String CLIENT_CONFIGURATION = "Lorg/eclipse/recommenders/commons/client/ClientConfiguration";
    public static final String WEB_SERVICE_CLIENT = "Lorg/eclipse/recommenders/commons/client/WebServiceClient";
    public static final String ICOMPILATION_UNIT_EXPORTER = "Lorg/eclipse/recommenders/udc/export/ICompilationUnitExporter";
    public static final String PRIMARY_TYPE_SUPERCLASS = OBJECT;
    public static final String PRIMARY_TYPE_NAME = "Lorg/eclipse/recommenders/udc/export/CompilationUnitServerExporter";
    String methodName = "addAll()V";
    public final Map<String, String> typeName2FingerpintMapping = new HashMap<String, String>();
    private Map<String, TypeReference> imports;
    private Map<String, IMethodName> methods;
    public static final String WSCLIENT_OBJECT = "wsClient";

    protected CompilationUnitProvider() {
        typeName2FingerpintMapping.put(ICOMPILATION_UNIT_EXPORTER, null);
        typeName2FingerpintMapping.put(WEB_SERVICE_CLIENT, null);
        typeName2FingerpintMapping.put(CLIENT_CONFIGURATION, null);
        typeName2FingerpintMapping.put(IPROGRESS_MONITOR, "13c4a5fde7a4b976fe4c5621964881108d23b297");
        typeName2FingerpintMapping.put(IPROJECT, "6deceea1ddb913cf997212853bdeb915f29eca0d");
        typeName2FingerpintMapping.put(LIST, "be2f64ce531dbd2c426a400d4f0c28075c9a6c2a");
        typeName2FingerpintMapping.put(OBJECT, "be2f64ce531dbd2c426a400d4f0c28075c9a6c2a");
    }

    public CompilationUnit createCompilationUnit() {
        final CompilationUnitBuilder builder = new CompilationUnitBuilder();
        builder.setPrimaryType(PRIMARY_TYPE_NAME, PRIMARY_TYPE_SUPERCLASS);
        builder.getCompilationUnit().primaryType.line = 10;

        setImports(builder);

        builder.addInterfaces(ICOMPILATION_UNIT_EXPORTER, IPROGRESS_MONITOR);

        builder.addFields(WEB_SERVICE_CLIENT, IPROGRESS_MONITOR);

        addMethods(builder);

        final CompilationUnit unit = builder.getCompilationUnit();

        return unit;
    }

    private void addMethods(final CompilationUnitBuilder builder) {
        methods = new HashMap<String, IMethodName>();
        addTrySendDataMethod(builder);
        addExportUnitsMethod(builder);
    }

    private void addExportUnitsMethod(final CompilationUnitBuilder builder) {
        final String superDeclaration = "Lorg/eclipse/recommenders/udc/export/ICompilationUnitExporter.exportUnits(Lorg/eclipse/core/resources/IProject;Ljava/util/List;Lorg/eclipse/core/runtime/IProgressMonitor;)V";
        final MethodDeclaration method = builder.addMethod(EXPORT_UNITS_METHOD, 39, superDeclaration, superDeclaration,
                1);
        methods.put(EXPORT_UNITS_METHOD, method.name);

        final ObjectInstanceKey object = builder.addObject(EXPORT_UNITS_METHOD, IPROGRESS_MONITOR, Kind.PARAMETER,
                MONITOR_OBJECT);
        builder.addReceiverCallSite(object, MONITOR_OBJECT, IPROGRESS_MONITOR, "subTask(Ljava/lang/String;)V",
                EXPORT_UNITS_METHOD, 40);
        builder.addReceiverCallSite(object, MONITOR_OBJECT, IPROGRESS_MONITOR, "beginTask(Ljava/lang/String;I)V",
                EXPORT_UNITS_METHOD, 39);
    }

    private void addTrySendDataMethod(final CompilationUnitBuilder builder) {
        final MethodDeclaration method = builder.addMethod(TRY_SEND_DATA_METHOD, 47, null, null, 2);
        methods.put(TRY_SEND_DATA_METHOD, method.name);
        ObjectInstanceKey object = builder.addObject(TRY_SEND_DATA_METHOD, LIST, Kind.PARAMETER, UNITS_OBJECT);
        builder.addParameterCallSite(object, WEB_SERVICE_CLIENT,
                "doPostRequest(Ljava/lang/String;Ljava/lang/Object;)V", UNITS_OBJECT, 1, TRY_SEND_DATA_METHOD, 47);
        builder.addObject(TRY_SEND_DATA_METHOD, OBJECT, Kind.PARAMETER, THIS_OBJECT);

        object = builder.addObject(TRY_SEND_DATA_METHOD, WEB_SERVICE_CLIENT, Kind.FIELD, WSCLIENT_OBJECT);
        builder.setDefinitionSite(object, DefinitionSite.Kind.FIELD, WSCLIENT_OBJECT, WEB_SERVICE_CLIENT, 0);
        builder.addReceiverCallSite(object, WSCLIENT_OBJECT, WEB_SERVICE_CLIENT,
                "doPostRequest(Ljava/lang/String;Ljava/lang/Object;)V", TRY_SEND_DATA_METHOD, 47);
    }

    private void setImports(final CompilationUnitBuilder builder) {
        imports = new HashMap<String, TypeReference>();
        for (final Entry<String, String> entry : typeName2FingerpintMapping.entrySet()) {
            final TypeReference ref = builder.addImport(entry.getKey(), entry.getValue());
            imports.put(entry.getKey(), ref);
        }
    }

    public TypeReference getImport(final String typeName) {
        return imports.get(typeName);
    }

    public IMethodName getMethodName(final String methodName) {
        return methods.get(methodName);
    }

    public ObjectInstanceKey getObject(final MethodDeclaration method, final String objectName) {
        ObjectInstanceKey result = null;
        for (final ObjectInstanceKey object : method.objects) {
            if (object.names.contains(objectName)) {
                if (result != null) {
                    throw new IllegalStateException(String.format("Found object \"%s\" more then once in method %s",
                            objectName, method.toString()));
                }
                result = object;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException(String.format("Object \"%s\" not available", objectName));
        }
        return result;
    }
}

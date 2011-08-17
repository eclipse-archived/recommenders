/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.codecompletion.templates.unit;

import java.util.HashSet;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.Mockito;

/**
 * All Unit tests to be executed are included here. Furthermore this class
 * provides access to several constructs, like defined MethodCalls, that are
 * used throughout the tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ CodeBuilderTest.class, CompletionProposalsBuilderTest.class, CompletionTargetVariableBuilderTest.class,
        MethodCallFormatterTest.class, MethodFormatterTest.class, PatternRecommenderTest.class,
        TemplatesCompletionModuleTest.class, TemplatesCompletionProposalComputerTest.class })
public final class UnitTestSuite {

    private static MethodCall methodCall;
    private static MethodCall returningMethodCall;
    private static MethodCall constructorCall;
    private static CodeBuilder codeBuilderMock;

    private UnitTestSuite() {
    }

    /**
     * @return the method which is used in most test cases.
     */
    static MethodCall getDefaultMethodCall() {
        if (methodCall == null) {
            methodCall = new MethodCall(getMockedTargetVariable("button123"),
                    createMethod("Lorg/eclipse/swt/widgets/Button.setText()V"));
        }
        return methodCall;
    }

    static MethodCall getDefaultReturningMethodCall() {
        if (returningMethodCall == null) {
            returningMethodCall = new MethodCall(getMockedTargetVariable("button456"),
                    createMethod("Lorg/eclipse/swt/widgets/Button.getText()Ljava/lang/String;"));
        }
        return returningMethodCall;
    }

    /**
     * @return the constructor call which is used in most test cases.
     */
    static MethodCall getDefaultConstructorCall() {
        if (constructorCall == null) {
            constructorCall = new MethodCall(getMockedTargetVariable(""),
                    createMethod("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;I)V"));
        }
        return constructorCall;
    }

    static CompletionTargetVariable getMockedTargetVariable(final String variableName) {
        final IIntelligentCompletionContext context = Mockito.mock(IIntelligentCompletionContext.class);
        return new CompletionTargetVariable(variableName, VmTypeName.get("Lorg/eclipse/swt/widgets/Button"),
                new HashSet<IMethodName>(), new Region(0, 0), false, false, context);
    }

    static CompletionTargetVariable getMockedTargetVariable(final String code, final String variableName,
            final String typeName, final boolean needsConstructor) {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(code,
                variableName, typeName);

        final ICompilationUnit compilationUnit = Mockito.mock(ICompilationUnit.class);
        Mockito.when(context.getCompilationUnit()).thenReturn(compilationUnit);
        return new CompletionTargetVariable(variableName, VmTypeName.get(typeName), new HashSet<IMethodName>(),
                new Region(0, 0), false, needsConstructor, context);
    }

    static CodeBuilder getCodeBuilderMock() {
        if (codeBuilderMock == null) {
            codeBuilderMock = new CodeBuilder(MethodCallFormatterTest.getMethodCallFormatterMock());
        }
        return codeBuilderMock;
    }

    static IMethodName createMethod(final String identifier) {
        return VmMethodName.get(identifier);
    }
}

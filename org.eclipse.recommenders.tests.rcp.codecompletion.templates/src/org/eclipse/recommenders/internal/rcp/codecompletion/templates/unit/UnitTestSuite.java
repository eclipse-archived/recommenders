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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import java.util.HashSet;

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

/**
 * All Unit tests to be executed are included here. Furthermore this class
 * provides access to several constructs, like defined {@link MethodCalls}s,
 * that are used troughout the tests.
 */
@RunWith(Suite.class)
@SuiteClasses({ CodeBuilderTest.class, CompletionProposalsBuilderTest.class, CompletionTargetVariableBuilderTest.class,
        MethodCallFormatterTest.class, MethodFormatterTest.class, PatternRecommenderTest.class,
        TemplatesCompletionModuleTest.class, TemplatesCompletionProposalComputerTest.class })
public final class UnitTestSuite {

    private static final IMethodName DEFAULTMETHOD = createMethod("Lorg/eclipse/swt/widgets/Button.setText()V");
    private static final IMethodName DEFAULTRETURNINGMETHOD = createMethod("Lorg/eclipse/swt/widgets/Button.getText()Ljava/lang/String;");
    private static final IMethodName CONSTRUCTORMETHOD = createMethod("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;I)V");

    private static final MethodCall METHODCALL = new MethodCall("button123", DEFAULTMETHOD);
    private static final MethodCall RETURNINGMETHODCALL = new MethodCall("button456", DEFAULTRETURNINGMETHOD);
    private static final MethodCall CONSTRUCTORCALL = new MethodCall("", CONSTRUCTORMETHOD);

    private static final CodeBuilder CODEBUILDERMOCK = new CodeBuilder(
            MethodCallFormatterTest.getMethodCallFormatterMock());

    private UnitTestSuite() {
    }

    /**
     * @return the method which is used in most test cases.
     */
    protected static MethodCall getDefaultMethodCall() {
        return METHODCALL;
    }

    protected static MethodCall getDefaultReturningMethodCall() {
        return RETURNINGMETHODCALL;
    }

    /**
     * @return the constructor call which is used in most test cases.
     */
    protected static MethodCall getDefaultConstructorCall() {
        return CONSTRUCTORCALL;
    }

    protected static CompletionTargetVariable getMockedTargetVariable(final String code, final String variableName,
            final String typeName, final boolean needsConstructor) {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getMockedContext(code,
                variableName, typeName);
        return new CompletionTargetVariable(variableName, VmTypeName.get(typeName), new HashSet<IMethodName>(),
                new Region(0, 0), needsConstructor, context);
    }

    protected static CodeBuilder getCodeBuilderMock() {
        return CODEBUILDERMOCK;
    }

    protected static IMethodName createMethod(final String identifier) {
        return VmMethodName.get(identifier);
    }
}

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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CompletionProposalsBuilderTest.class, CompletionTargetVariableBuilderTest.class,
        MethodCallFormatterTest.class, MethodFormatterTest.class, PatternRecommenderTest.class,
        TemplatesCompletionEngineTest.class })
final class AllTests {

    private static final IMethodName METHODNAME = VmMethodName
            .get("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;Lint;)V");

    private static final CompletionTargetVariable TARGETVARIABLE = new CompletionTargetVariable("someVariable",
            VmTypeName.get("Lorg/eclipse/swt/widgets/Button"), null, false);

    private static final MethodCall METHODCALL = new MethodCall(TARGETVARIABLE, METHODNAME);

    /**
     * @return the expression which is used in most test cases.
     */
    protected static MethodCall getDefaultMethodCall() {
        return AllTests.METHODCALL;
    }

}

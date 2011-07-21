/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.extdoc;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

public final class TestUtils {

    private static ITypeName defaultType;
    private static IMethodName defaultMethod;
    private static IMethodName defaultConstructor;

    static {
        defaultType = VmTypeName.get("Lorg/eclipse/swt/widgets/Button");
        defaultMethod = VmMethodName.get("Lorg/eclipse/swt/widgets/Button.getText()Ljava/lang/String;");
        defaultConstructor = VmMethodName
                .get("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;I)V");
    }

    private TestUtils() {
    }

    public static ITypeName getDefaultType() {
        return defaultType;
    }

    public static IMethodName getDefaultMethod() {
        return defaultMethod;
    }

    public static IMethodName getDefaultConstructor() {
        return defaultConstructor;
    }

}

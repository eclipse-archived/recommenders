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
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

public class AbstractServer {

    protected String createKey(final IMethod method) {
        final IMethodName methodName = JavaElementResolver.INSTANCE.toRecMethod(method);
        return methodName == null ? null : methodName.getIdentifier();
    }

    protected String createKey(final IType type) {
        final ITypeName typeName = JavaElementResolver.INSTANCE.toRecType(type);
        return typeName == null ? null : typeName.getIdentifier();
    }

}

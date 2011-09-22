package org.eclipse.recommenders.tests.server.udc.modelgeneration;

/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.common.collect.Sets;

public class ObjectUsageBuilder {

    private final ObjectUsage objectUsage;

    public ObjectUsageBuilder(final String type) {
        objectUsage = new ObjectUsage();
        objectUsage.calls = Sets.newHashSet();
        objectUsage.type = getTypeName(type);
    }

    public ObjectUsageBuilder addCalls(final String... methods) {
        for (final String method : methods) {
            objectUsage.calls.add(getMethodName(method));
        }
        return this;
    }

    public ObjectUsageBuilder addSameTypeCalls(final String... methods) {
        for (final String method : methods) {
            objectUsage.calls.add(getMethodName(objectUsage.type, method));
        }
        return this;
    }

    private ITypeName getTypeName(final String type) {
        return VmTypeName.get(type);
    }

    private IMethodName getMethodName(final ITypeName type, final String method) {
        return VmMethodName.get(type.getIdentifier(), method);
    }

    private IMethodName getMethodName(final String method) {
        return VmMethodName.get(method);
    }

    public ObjectUsage build() {
        return objectUsage;
    }

}

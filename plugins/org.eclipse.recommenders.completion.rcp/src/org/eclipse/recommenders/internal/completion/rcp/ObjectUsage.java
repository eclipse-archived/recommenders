/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import java.util.Date;
import java.util.Set;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.Sets;

public class ObjectUsage {

    public static ObjectUsage newObjectUsageWithDefaults() {
        final ObjectUsage res = new ObjectUsage();
        res.type = UNKNOWN_TYPE;
        res.contextFirst = UNKNOWN_METHOD;
        res.contextSuper = UNKNOWN_METHOD;
        res.definition = UNKNOWN_METHOD;
        res.kind = DefinitionSite.Kind.UNKNOWN;
        return res;
    }

    public static final ITypeName UNKNOWN_TYPE = VmTypeName.get("LNull");
    public static final IMethodName UNKNOWN_METHOD = VmMethodName.get("LNull.null()V");
    public static final DefinitionSite.Kind UNKNOWN_KIND = DefinitionSite.Kind.UNKNOWN;

    public static final IMethodName NO_METHOD = VmMethodName.get("LNone.none()V"); // param/fields
    public static final IMethodName DUMMY_METHOD = VmMethodName.get("LDummy.dummy()V"); // crash prevention

    public Date cuCreationTimestamp;
    public ITypeName type;

    public IMethodName contextSuper;
    public IMethodName contextFirst;
    public Set<IMethodName> calls = Sets.newHashSet();
    public DefinitionSite.Kind kind;
    public IMethodName definition;

    @Override
    public String toString() {
        return "ObjectUsage [type=" + type + ", contextSuper=" + contextSuper + ", contextFirst=" + contextFirst
                + ", calls=" + calls + ", kind=" + kind + ", def=" + definition + "]";
    }
}
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
package org.eclipse.recommenders.internal.utils.codestructs;

import java.util.Date;
import java.util.Set;

import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class ObjectUsage {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((calls == null) ? 0 : calls.hashCode());
        result = prime * result + ((contextFirst == null) ? 0 : contextFirst.hashCode());
        result = prime * result + ((contextSuper == null) ? 0 : contextSuper.hashCode());
        result = prime * result + ((cuCreationTimestamp == null) ? 0 : cuCreationTimestamp.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((kind == null) ? 0 : kind.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectUsage other = (ObjectUsage) obj;
        if (calls == null) {
            if (other.calls != null)
                return false;
        } else if (!calls.equals(other.calls))
            return false;
        if (contextFirst == null) {
            if (other.contextFirst != null)
                return false;
        } else if (!contextFirst.equals(other.contextFirst))
            return false;
        if (contextSuper == null) {
            if (other.contextSuper != null)
                return false;
        } else if (!contextSuper.equals(other.contextSuper))
            return false;
        if (cuCreationTimestamp == null) {
            if (other.cuCreationTimestamp != null)
                return false;
        } else if (!cuCreationTimestamp.equals(other.cuCreationTimestamp))
            return false;
        if (definition == null) {
            if (other.definition != null)
                return false;
        } else if (!definition.equals(other.definition))
            return false;
        if (kind != other.kind)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public static ObjectUsage newObjectUsageWithDefaults() {
        final ObjectUsage res = new ObjectUsage();
        res.type = Constants.UNKNOWN_TYPE;
        res.contextFirst = Constants.UNKNOWN_METHOD;
        res.contextSuper = Constants.UNKNOWN_METHOD;
        res.definition = Constants.UNKNOWN_METHOD;
        res.kind = Kind.UNKNOWN;
        return res;
    }

    // public static final ITypeName UNKNOWN_TYPE = VmTypeName.get("LNull");
    // public static final IMethodName UNKNOWN_METHOD = VmMethodName.get("LNull.null()V");
    // public static final DefinitionSite.Kind UNKNOWN_KIND = DefinitionSite.Kind.UNKNOWN;
    //
    // public static final IMethodName NO_METHOD = VmMethodName.get("LNone.none()V"); // param/fields
    // public static final IMethodName DUMMY_METHOD = VmMethodName.get("LDummy.dummy()V"); // crash prevention

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

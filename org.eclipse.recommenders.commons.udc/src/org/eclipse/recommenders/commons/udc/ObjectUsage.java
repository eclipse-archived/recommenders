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
package org.eclipse.recommenders.commons.udc;

import java.util.Date;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

public class ObjectUsage {

    public ITypeName type;
    public IMethodName contextSuper;
    public IMethodName contextFirst;
    public Set<IMethodName> calls;
    public Date cuCreationTimestamp;

    @Override
    public String toString() {
        return "ObjectUsage [type=" + type + ", contextSuper=" + contextSuper + ", contextFirst=" + contextFirst
                + ", calls=" + calls + "]";
    }

}

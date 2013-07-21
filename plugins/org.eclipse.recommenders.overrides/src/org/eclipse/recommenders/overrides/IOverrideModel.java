/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.overrides;

import java.util.List;

import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;

public interface IOverrideModel {

    void reset();

    void setObservedMethod(IMethodName method);

    ITypeName getType();

    ImmutableSet<IMethodName> getKnownMethods();

    ImmutableSet<String> getKnownPatterns();

    List<Recommendation<IMethodName>> recommendOverrides();

}

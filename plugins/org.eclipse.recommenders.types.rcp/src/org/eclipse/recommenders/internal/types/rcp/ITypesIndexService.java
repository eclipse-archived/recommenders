/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.ImmutableSet;

public interface ITypesIndexService {

    ImmutableSet<String> subtypes(IType expected, String prefix);

    ImmutableSet<String> subtypes(ITypeName expected, String prefix, IJavaProject project);

    ImmutableSet<String> subtypes(String type, String prefix, IJavaProject project);
}

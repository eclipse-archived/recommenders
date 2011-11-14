/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Darmstadt University of Technology - initial API and implementation.
 */
package org.eclipse.recommenders.internal.analysis.selectors;

import java.util.Collection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;

public interface IFieldsSelector {
    public Collection<IField> select(final IClass receiverType);
}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.wala;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public interface IClassHierarchyService {

    IClassHierarchy getClassHierachy(IJavaElement jdtElement);

    IClass getType(IType jdtType);

    IMethod getMethod(org.eclipse.jdt.core.IMethod jdtMethod);

}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.analysis.cp;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;

public interface IClasspathEntryAnalyzer {

    void analyze(IClasspathEntry jdtEntry, ClasspathEntry recEntry);

}

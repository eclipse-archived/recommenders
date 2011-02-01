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
package org.eclipse.recommenders.rcp;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;

/**
 * An {@link IEditorDashboard} serves as a pinboard for any kind of analysis or
 * recommendation results. The data table (see {@link #setData(String, Object)
 * and {@link #getData(String)}) may be used to store or read analysis artifacts
 * or recommendation results.
 * <p>
 * For instance, every {@link IEditorChangedListener} may add its analysis
 * results to the data table using the ( {@link #setData(String, Object)}
 * method. Also, every recommender might use any information already available
 * in the data table to create its recommendations. The recommendations then
 * should also be stored in the data table. Every {@link CompletionEngine} may
 * use the recommendations to provide the corresponding recommendations.
 * <p>
 * <b>Note: It's unclear whether a real dashboard is actually needed. Thus, this
 * API is subject to change after code recommenders has become a bit stable.</b>
 */
@SuppressWarnings("restriction")
@Provisional
public interface IEditorDashboard {
    public abstract ICompilationUnit getCompilationUnit();

    public abstract JavaEditor getEditor();

    public abstract <T> T getData(final String key);

    public abstract void setData(final String key, final Object value);

    public abstract boolean hasCompilationUnit();
}

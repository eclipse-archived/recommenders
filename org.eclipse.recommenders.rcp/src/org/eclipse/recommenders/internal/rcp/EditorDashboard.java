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
package org.eclipse.recommenders.internal.rcp;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.rcp.IEditorDashboard;

import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class EditorDashboard implements IEditorDashboard {
    public static IEditorDashboard create(final JavaEditor editor) {
        final EditorDashboard res = new EditorDashboard();
        res.editor = editor;
        return res;
    }

    private JavaEditor editor;

    private final Map<String, Object> data = Maps.newConcurrentMap();

    /**
     * Returns the java editor this dashboard belongs to.
     * 
     */
    @Override
    public JavaEditor getEditor() {
        return editor;
    }

    /**
     * Returns the compilation unit underlying the editor or {@code null} if the
     * editor shows a compiled class.
     */
    @Override
    public @Nullable
    ICompilationUnit getCompilationUnit() {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        return (ICompilationUnit) (root instanceof ICompilationUnit ? root : null);
    }

    /**
     * In a dash board clients may put their temporary data into it and share
     * their data with other clients. Although the previous recommenders version
     * was build on this API it's not certain that this API will continue to
     * exist.
     */
    @Override
    public void setData(final String key, final Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getData(final String key) {
        return (T) data.get(key);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean hasCompilationUnit() {
        return getCompilationUnit() != null;
    }
}

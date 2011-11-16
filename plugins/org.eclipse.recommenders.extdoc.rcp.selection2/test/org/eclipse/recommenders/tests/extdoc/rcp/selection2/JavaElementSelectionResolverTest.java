/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Optional;

public class JavaElementSelectionResolverTest {

    /**
     * 
     */
    private static final int SOME_OFFSET = 111;
    static final IJavaElement[] EMPTY = new IJavaElement[0];
    static final IJavaElement SOME_ELEMENT = mock(IJavaElement.class);
    /**
     * 
     */
    private static final IJavaElement[] IJAVA_ELEMENTS = new IJavaElement[] { SOME_ELEMENT };

    @Test
    public void testElementAtReturn() throws JavaModelException {
        final ITypeRoot root = mock(ITypeRoot.class);
        when(root.codeSelect(anyInt(), anyInt())).thenReturn(EMPTY);
        when(root.getElementAt(anyInt())).thenReturn(SOME_ELEMENT);
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(root, SOME_OFFSET);
        assertEquals(of(SOME_ELEMENT), actual);
    }

    @Test
    public void testCodeSelectAndElementAtFail() throws JavaModelException {
        final ITypeRoot root = mock(ITypeRoot.class);
        when(root.codeSelect(anyInt(), anyInt())).thenReturn(EMPTY);
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(root, SOME_OFFSET);
        assertEquals(absent(), actual);
    }

    @Test
    public void testCodeSelectNonNull() throws JavaModelException {
        final ITypeRoot root = mock(ITypeRoot.class);
        when(root.codeSelect(anyInt(), anyInt())).thenReturn(IJAVA_ELEMENTS);
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(root, SOME_OFFSET);
        assertEquals(of(SOME_ELEMENT), actual);
    }

    @Test
    @Ignore("does not work due to dependency to a runtime instance of plugin for logging...")
    public void testCodeSelectThrowsException() throws JavaModelException {
        final ITypeRoot root = mock(ITypeRoot.class);
        final JavaModelException e = mock(JavaModelException.class);
        when(root.codeSelect(anyInt(), anyInt())).thenThrow(e);
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(root, SOME_OFFSET);
        assertEquals(absent(), actual);
    }

    @Test
    public void testResolveFromStructuredSelection() {
        final IStructuredSelection s = new StructuredSelection(SOME_ELEMENT);
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromViewer(s);
        assertEquals(of(SOME_ELEMENT), actual);
    }

    @Test
    public void testResolveFromStructuredSelectionWithNonJavaElement() {
        final IStructuredSelection s = new StructuredSelection(new Object());
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromViewer(s);
        assertEquals(absent(), actual);
    }

    @Test
    public void testResolveFromStructuredSelectionWithNull() {
        final IStructuredSelection s = new StructuredSelection(newArrayList());
        final Optional<IJavaElement> actual = JavaSelectionUtils.resolveJavaElementFromViewer(s);
        assertEquals(absent(), actual);
    }
}

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
package org.eclipse.recommenders.extdoc.rcp.selection2;

import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.METHOD_BODY;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION;
import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION_EXTENDS;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;

public class Provider {

    @JavaSelectionListener
    public void handleAllTypeSelections(final IType type, final JavaSelection selection) {

    }

    @JavaSelectionListener(TYPE_DECLARATION)
    public void handleTypeDeclarationOnly(final IType type, final JavaSelection selection) {

    }

    @JavaSelectionListener(TYPE_DECLARATION_EXTENDS)
    public void handleExtendsSelectionOnly(final IType type, final JavaSelection selection) {

    }

    @JavaSelectionListener(METHOD_BODY)
    public void handleAllSelectionsInMethodBody(final IJavaElement element, final JavaSelection selection) {

    }

    @JavaSelectionListener({ METHOD_BODY, FIELD_DECLARATION_INITIALIZER })
    public void handleAllMethodCalls(final IMethod method, final JavaSelection selection) {

    }

    //
    //
    //
    // @JavaSelectionLocation
    // public void doit(final IMethod method , final IJavaElementSelection c) {
    // }
    //

    //
    //
    //
    // @JavaSelectionLocation
    // public void doit(final IMethod method , final IJavaElementSelection c) {
    // }
    //
    //
    //
    // @JavaSelectionLocation({EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION })
    // public void doit(final IType type, final IJavaElementSelection c) {
    // }
    //
    // @JavaSelectionLocation({ EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION })
    // public void doit(final IJavaElement type, final IJavaElementSelection c) {
    // }
    //
    //
    //
    //
    //
    //
    //
    // @JavaSelectionLocation({ EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION })
    // public void doit(final Context c) {
    // final IMethod m = c.getElement();
    // }
    //
    // @JavaSelectionLocation()
    // public void doit(final IMethod m, final Context c) {
    // }
    // @JavaSelectionLocation()
    // public void doit(IType, Context c) {
    // }
    //
    // @JavaSelectionLocation(locations = { EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION })
    // public void doit(final IMethod m, final Context editorContext) {
    // sdfsdf(m, editorContext);
    // }
    //
    // @JavaSelectionLocation(locations = { EXTENDS_DECLARATION, IMPLEMENTS_DECLARATION })
    // public void doit(final IType m, final Context editorContext) {
    // sdfsdf(m, editorContext);
    // }
    //
    // private void sdfsdf(final IJavaElement m, final Context editorContext) {
    //
    // }
}

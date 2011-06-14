/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public final class ExamplesProvider extends AbstractProviderComposite {

    private Text text;

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        final Composite container = SwtFactory.createGridComposite(parent, 1, 0, 0, 0, 0);
        text = SwtFactory.createText(parent, "", 10, 100);
        return container;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayContentForMethod((IMethod) element);
        }
        return false;
    }

    private boolean displayContentForMethod(final IMethod element) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean displayContentForType(final IType element) {
        // TODO Auto-generated method stub
        return false;
    }

}

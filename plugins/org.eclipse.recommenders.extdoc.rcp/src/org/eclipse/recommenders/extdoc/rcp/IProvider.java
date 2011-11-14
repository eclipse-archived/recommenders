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
package org.eclipse.recommenders.extdoc.rcp;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A provider contributes selection-specific information to the ExtDoc framework
 * and will be displayed in the ExtDoc view and editor hovers.
 */
public interface IProvider extends IExecutableExtension {

    /**
     * @return The provider's short name for locations where space is rare, e.g.
     *         the view's table.
     */
    String getProviderName();

    /**
     * @return The provider's full name, e.g. to be displayed in a provider
     *         headline.
     */
    String getProviderFullName();

    /**
     * @return An 16x15 icon representing the provider.
     */
    Image getIcon();

    /**
     * @param location
     * @return True, if the provider generally offers information for this
     *         location, i.e. a further request for an update will be issued.
     */
    boolean isAvailableForLocation(JavaElementLocation location);

    /**
     * @param parent
     *            The composite hosting the provider.
     * @param workbenchWindow
     *            The workbench window in which the provider will be displayed.
     * @return The composite which will be filled with the provider's content.
     */
    Composite createComposite(Composite parent, IWorkbenchWindow workbenchWindow);

    Composite resolveContentComposite(Composite mainProviderComposite);

    /**
     * @param selection
     *            The current user selection.
     * @param composite
     *            The composite which has been created by the provider earlier
     *            in shall now be filled.
     * @return True, if provider was able to display content for the selection.
     */
    boolean selectionChanged(IJavaElementSelection selection, Composite composite);

    boolean hideOnTimeout();

}

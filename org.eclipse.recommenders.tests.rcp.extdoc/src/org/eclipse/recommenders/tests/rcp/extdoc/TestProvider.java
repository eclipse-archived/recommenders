package org.eclipse.recommenders.tests.rcp.extdoc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.rcp.extdoc.IDeletionProvider;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

public final class TestProvider implements IProvider, IDeletionProvider {

    @Override
    public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data)
            throws CoreException {
    }

    @Override
    public String getProviderName() {
        return null;
    }

    @Override
    public String getProviderFullName() {
        return null;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return false;
    }

    @Override
    public Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        return null;
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection context) {
        return false;
    }

    @Override
    public Shell getShell() {
        return null;
    }

    @Override
    public void requestDeletion(final Object object) {
    }

}

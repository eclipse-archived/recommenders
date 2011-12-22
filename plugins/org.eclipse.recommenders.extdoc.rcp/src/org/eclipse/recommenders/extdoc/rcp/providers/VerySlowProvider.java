package org.eclipse.recommenders.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ProviderDescription;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.inject.Inject;

public final class VerySlowProvider extends Provider {

    private ProviderDescription description;

    @Inject
    public VerySlowProvider(ExtdocIconLoader iconLoader) {
        description = new ProviderDescription("VerySlowProvider", iconLoader.getImage("provider.subclassing.gif"));
    }

    @Override
    public ProviderDescription getDescription() {
        return description;
    }

    @JavaSelectionListener(JavaSelectionLocation.METHOD_BODY)
    public void displayProposalsForType(IJavaElement element, JavaSelectionEvent selection, final Composite parent)
            throws InterruptedException {
        Thread.sleep(3000);
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                Label l = new Label(parent, SWT.NONE);
                l.setText("Veeeery slow provider was here! :>");
            }
        });
    }
}
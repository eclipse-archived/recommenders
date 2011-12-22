package org.eclipse.recommenders.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ProviderDescription;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Composite;

import com.google.inject.Inject;

public final class SlowAndFailingProvider extends Provider {

    private ProviderDescription description;

    @Inject
    public SlowAndFailingProvider(ExtdocIconLoader iconLoader) {
        description = new ProviderDescription("SlowAndFailingProvider", iconLoader.getImage("provider.subclassing.gif"));
    }

    @Override
    public ProviderDescription getDescription() {
        return description;
    }

    @JavaSelectionListener
    public void displayProposalsForType(IJavaElement element, JavaSelectionEvent selection, final Composite parent)
            throws InterruptedException {
        Thread.sleep(1500);
        Throws.throwNotImplemented();
    }
}
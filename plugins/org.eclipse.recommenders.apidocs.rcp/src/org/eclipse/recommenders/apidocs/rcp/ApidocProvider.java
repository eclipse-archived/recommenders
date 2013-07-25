/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 *     Patrick Gottschaemmer, Olav Lenz - added getId() method
 */
package org.eclipse.recommenders.apidocs.rcp;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Display;

/**
 * Base class to use when implementing your own extended Javadoc provider. Subclasses should provide one of more public
 * methods annotated with {@link JavaSelectionSubscriber} and a signature of
 * 
 * <pre>
 *     @JavaSelectionSubsriber
 *     public void anyMethodName(AnySubtypeOfIJavaElement selectedElement, JavaSelectionEvent selection, Composite parent) {
 *         ..
 *     }
 * </pre>
 * 
 * to respond to selection events in the IDE.
 * 
 * <p>
 * Note that it these call back methods are always called on a background thread. It's up to the provider to call any UI
 * operation in the UI thread. Providers may use {@link #runSyncInUiThread(Runnable)} for that purpose as convenient
 * shortcut.
 * </p>
 * 
 * @see org.eclipse.recommenders.extdoc.rcp.provider extension point for details on how to register a new provider
 */
public abstract class ApidocProvider {

    @Deprecated
    /**
     * Used to indicate whether an extdoc provider has some content to display. This is not needed anymore since v1.1. 
     */
    public enum Status {
        OK, NOT_AVAILABLE
    }

    private boolean isEnabled = true;
    private ApidocProviderDescription description;

    public final void setDescription(final ApidocProviderDescription description) {
        this.description = description;
    }

    public ApidocProviderDescription getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(final boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    protected final void runSyncInUiThread(final Runnable runnable) {
        final ExceptionHandler handler = new ExceptionHandler();
        final CountDownLatch latch = new CountDownLatch(1);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                    latch.countDown();
                } catch (final Exception e) {
                    handler.setException(e);
                }
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
        }
        handler.throwExceptionIfExistent();
    }

    private static class ExceptionHandler {
        private Exception e;

        private void setException(final Exception e) {
            this.e = e;
        }

        private void throwExceptionIfExistent() {
            if (e != null) {
                Throws.throwUnhandledException(e);
            }
        }
    }

    public String getId() {
        return getClass().getName();
    }
}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.extdoc.rcp.providers;

import java.util.concurrent.CountDownLatch;

import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Display;

public abstract class ExtdocProvider {

    public enum Status {
        OK, NOT_AVAILABLE
    }

    private boolean isEnabled = true;
    private ExtdocProviderDescription description;

    public final void setDescription(final ExtdocProviderDescription description) {
        this.description = description;
    }

    public ExtdocProviderDescription getDescription() {
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
        // try {
        // latch.await(5, TimeUnit.SECONDS);
        // } catch (final InterruptedException e) {
        // }
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
}
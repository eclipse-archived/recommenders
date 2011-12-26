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
package org.eclipse.recommenders.internal.extdoc.rcp.scheduling;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;

public class Events {
    public static class NewSelectionEvent {

        public final JavaSelectionEvent selection;

        public NewSelectionEvent(final JavaSelectionEvent selection) {
            this.selection = selection;
        }
    }

    public static class ProviderActivationEvent {

        public final ExtdocProvider provider;

        public ProviderActivationEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDeactivationEvent {

        public final ExtdocProvider provider;

        public ProviderDeactivationEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDelayedEvent {

        public final ExtdocProvider provider;

        public ProviderDelayedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFailedEvent {

        public final ExtdocProvider provider;
        public final Exception exception;

        public ProviderFailedEvent(final ExtdocProvider provider, final Exception e) {
            this.provider = provider;
            this.exception = e;
        }
    }

    public static class ProviderFinishedEvent {

        public final ExtdocProvider provider;

        public ProviderFinishedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFinishedLateEvent {

        public final ExtdocProvider provider;

        public ProviderFinishedLateEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderNotAvailableEvent {
        public final ExtdocProvider provider;

        public ProviderNotAvailableEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderOrderChangedEvent {

        public final ExtdocProvider provider;
        public final ExtdocProvider reference;
        public final int oldIndex;
        public final int newIndex;

        public ProviderOrderChangedEvent(final ExtdocProvider provider, final ExtdocProvider reference,
                final int oldIndex, final int newIndex) {
            this.provider = provider;
            this.reference = reference;
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
    }

    public static class ProviderSelectionEvent {

        public final ExtdocProvider provider;

        public ProviderSelectionEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderStartedEvent {

        public final ExtdocProvider provider;

        public ProviderStartedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class RenderNowEvent {
    }
}
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
package org.eclipse.recommenders.extdoc.rcp.scheduling;

import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;

public class Events {
    public static class NewSelectionEvent {

        public final JavaSelectionEvent selection;

        public NewSelectionEvent(JavaSelectionEvent selection) {
            this.selection = selection;
        }
    }

    public static class ProviderActivationEvent {

        public final Provider provider;

        public ProviderActivationEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDeactivationEvent {

        public final Provider provider;

        public ProviderDeactivationEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDelayedEvent {

        public final Provider provider;

        public ProviderDelayedEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFailedEvent {

        public final Provider provider;
        public final Exception exception;

        public ProviderFailedEvent(Provider provider, Exception e) {
            this.provider = provider;
            this.exception = e;
        }
    }

    public static class ProviderFinishedEvent {

        public final Provider provider;

        public ProviderFinishedEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFinishedLateEvent {

        public final Provider provider;

        public ProviderFinishedLateEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderNotAvailableEvent {
        public final Provider provider;

        public ProviderNotAvailableEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderOrderChangedEvent {

        public final Provider provider;
        public final Provider reference;
        public final int oldIndex;
        public final int newIndex;

        public ProviderOrderChangedEvent(Provider provider, Provider reference, int oldIndex, int newIndex) {
            this.provider = provider;
            this.reference = reference;
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
    }

    public static class ProviderSelectionEvent {

        public final Provider provider;

        public ProviderSelectionEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderStartedEvent {

        public final Provider provider;

        public ProviderStartedEvent(Provider provider) {
            this.provider = provider;
        }
    }

    public static class RenderNowEvent {
    }
}
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

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;

public class Events {

    public static abstract class ExtdocEvent {
        @Override
        public boolean equals(Object obj) {
            return reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return reflectionHashCode(this);
        }
    }

    public static class NewSelectionEvent extends ExtdocEvent {

        public final JavaSelectionEvent selection;

        public NewSelectionEvent(final JavaSelectionEvent selection) {
            this.selection = selection;
        }
    }

    public static class ProviderActivationEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderActivationEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDeactivationEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderDeactivationEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderDelayedEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderDelayedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFailedEvent extends ExtdocEvent {

        public final ExtdocProvider provider;
        public final Throwable throwable;

        public ProviderFailedEvent(final ExtdocProvider provider, final Throwable throwable) {
            this.provider = provider;
            this.throwable = throwable;
        }
    }

    public static class ProviderFinishedEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderFinishedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderFinishedLateEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderFinishedLateEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderNotAvailableEvent extends ExtdocEvent {
        public final ExtdocProvider provider;
        public final boolean hasFinishedLate;

        public ProviderNotAvailableEvent(final ExtdocProvider provider) {
            this.provider = provider;
            hasFinishedLate = false;
        }

        public ProviderNotAvailableEvent(final ExtdocProvider provider, final boolean hasFinishedLate) {
            this.provider = provider;
            this.hasFinishedLate = hasFinishedLate;
        }
    }

    public static class ProviderOrderChangedEvent extends ExtdocEvent {

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

    public static class ProviderSelectionEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderSelectionEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class ProviderStartedEvent extends ExtdocEvent {

        public final ExtdocProvider provider;

        public ProviderStartedEvent(final ExtdocProvider provider) {
            this.provider = provider;
        }
    }

    public static class RenderNowEvent extends ExtdocEvent {
    }
}
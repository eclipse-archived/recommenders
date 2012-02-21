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

import static com.google.common.collect.Lists.newLinkedList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderSelectionEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.ProviderStartedEvent;
import org.eclipse.recommenders.internal.extdoc.rcp.scheduling.Events.RenderNowEvent;
import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.Subscribe;

public class BusSpyThatAlsoChecksForUiThread {

    private final List<Object> _events = newLinkedList();

    public synchronized List<Object> getEvents() {
        return _events;
    }

    private void add(final Object event) {
        // ensureCurrentThreadIsUiThread();
        getEvents().add(event);
    }

    private void ensureCurrentThreadIsUiThread() {
        final Thread actual = Thread.currentThread();
        final Thread uiThread = Display.getCurrent().getThread();
        assertSame(uiThread, actual);
    }

    /**
     * asserts that the expected event was posted
     */
    public void assertEvent(final Object expected) {
        assertFalse(getEvents().isEmpty());
        assertTrue("no matching event found", getEvents().remove(expected));
    }

    /**
     * asserts that the expected event was posted as the next event
     */
    public void assertNextEvent(final Object expected) {
        assertFalse(getEvents().isEmpty());
        final Object actual = getEvents().remove(0);
        assertEquals(expected, actual);
    }

    public void assertNoMoreEvents() {
        assertTrue(getEvents().isEmpty());
    }

    @Subscribe
    public void on(final NewSelectionEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderActivationEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderDeactivationEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderDelayedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderFailedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderFinishedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderFinishedLateEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderNotAvailableEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderOrderChangedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderSelectionEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final ProviderStartedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(final RenderNowEvent e) {
        add(e);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
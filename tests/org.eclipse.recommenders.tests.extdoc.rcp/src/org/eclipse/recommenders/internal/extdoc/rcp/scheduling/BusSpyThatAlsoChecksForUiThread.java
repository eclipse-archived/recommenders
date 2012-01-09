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

    private List<Object> events = newLinkedList();

    public List<?> getEvents() {
        return events;
    }

    private void add(Object event) {
        ensureCurrentThreadIsUiThread();
        events.add(event);
    }

    private void ensureCurrentThreadIsUiThread() {
        Thread actual = Thread.currentThread();
        Thread uiThread = Display.getCurrent().getThread();
        assertSame(uiThread, actual);
    }

    /**
     * asserts that the expected event was posted
     */
    public void assertEvent(Object expected) {
        assertFalse(events.isEmpty());
        assertTrue("no matching event found", events.remove(expected));
    }

    /**
     * asserts that the expected event was posted as the next event
     */
    public void assertNextEvent(Object expected) {
        assertFalse(events.isEmpty());
        Object actual = events.remove(0);
        assertEquals(expected, actual);
    }

    public void assertNoMoreEvents() {
        assertTrue(events.isEmpty());
    }

    @Subscribe
    public void on(NewSelectionEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderActivationEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderDeactivationEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderDelayedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderFailedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderFinishedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderFinishedLateEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderNotAvailableEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderOrderChangedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderSelectionEvent e) {
        add(e);
    }

    @Subscribe
    public void on(ProviderStartedEvent e) {
        add(e);
    }

    @Subscribe
    public void on(RenderNowEvent e) {
        add(e);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
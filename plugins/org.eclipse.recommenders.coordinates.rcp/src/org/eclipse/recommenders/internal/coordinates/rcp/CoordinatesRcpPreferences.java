/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - initial API and implementation
 */
package org.eclipse.recommenders.internal.coordinates.rcp;

import static org.apache.commons.lang3.ArrayUtils.isEquals;

import javax.inject.Inject;

import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.recommenders.coordinates.rcp.CoordinateEvents.AdvisorConfigurationChangedEvent;

import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class CoordinatesRcpPreferences {

    public String advisorConfiguration;

    private final EventBus bus;

    @Inject
    public CoordinatesRcpPreferences(EventBus bus) {
        this.bus = bus;
    }

    @Inject
    public void setAdvisorConfiguration(@Preference(Constants.PREF_SORTED_ADVISOR_LIST) String newAdvisorConfiguration)
            throws Exception {
        String old = advisorConfiguration;
        advisorConfiguration = newAdvisorConfiguration;
        if (!isEquals(advisorConfiguration, old)) {
            bus.post(new AdvisorConfigurationChangedEvent());
        }
    }
}

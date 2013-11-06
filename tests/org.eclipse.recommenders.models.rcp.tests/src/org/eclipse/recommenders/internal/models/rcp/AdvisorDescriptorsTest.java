/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class AdvisorDescriptorsTest {

    @Mock
    public IConfigurationElement first;

    @Mock
    public IConfigurationElement second;

    @Mock
    public IConfigurationElement third;

    @Mock
    public IConfigurationElement fourth;

    @Before
    public void setUp() {
        Mockito.when(first.getAttribute("id")).thenReturn("first");
        Mockito.when(second.getAttribute("id")).thenReturn("second");
        Mockito.when(third.getAttribute("id")).thenReturn("third");
        Mockito.when(fourth.getAttribute("id")).thenReturn("fourth");
    }

    @Test
    public void testLoadedSettingsIgnoresDefaultEnablement() {
        List<AdvisorDescriptor> result = AdvisorDescriptors.load("first;!second;third;!fourth",
                ImmutableList.of(enabled(first), enabled(second), disabled(third), disabled(fourth)));

        assertThat(result.size(), is(4));
        assertThat(result.get(0).getId(), is(equalTo("first")));
        assertThat(result.get(0).isEnabled(), is(true));
        assertThat(result.get(1).getId(), is(equalTo("second")));
        assertThat(result.get(1).isEnabled(), is(false));
        assertThat(result.get(2).getId(), is(equalTo("third")));
        assertThat(result.get(2).isEnabled(), is(true));
        assertThat(result.get(3).getId(), is(equalTo("fourth")));
        assertThat(result.get(3).isEnabled(), is(false));
    }

    @Test
    public void testLoadIgnoresUnknownAdvisors() {
        List<AdvisorDescriptor> result = AdvisorDescriptors.load("first;unknown;second;!uninstalled",
                ImmutableList.of(enabled(first), enabled(second)));

        assertThat(result.size(), is(2));
        assertThat(result.get(0).getId(), is(equalTo("first")));
        assertThat(result.get(0).isEnabled(), is(true));
        assertThat(result.get(1).getId(), is(equalTo("second")));
        assertThat(result.get(1).isEnabled(), is(true));
    }

    @Test
    public void testLoadAppendsNewAdvisors() {
        List<AdvisorDescriptor> result = AdvisorDescriptors.load("third",
                ImmutableList.of(enabled(first), disabled(second), enabled(third)));

        assertThat(result.size(), is(3));
        assertThat(result.get(0).getId(), is(equalTo("third")));
        assertThat(result.get(0).isEnabled(), is(true));
        assertThat(result.get(1).getId(), is(equalTo("first")));
        assertThat(result.get(1).isEnabled(), is(true));
        assertThat(result.get(2).getId(), is(equalTo("second")));
        assertThat(result.get(2).isEnabled(), is(false));
    }

    @Test
    public void testStore() {
        String result = AdvisorDescriptors.store(ImmutableList.of(enabled(first), disabled(second), enabled(third)));

        assertThat(result, is(equalTo("first;!second;third")));
    }

    private AdvisorDescriptor enabled(IConfigurationElement config) {
        return new AdvisorDescriptor(config, true);
    }

    private AdvisorDescriptor disabled(IConfigurationElement config) {
        return new AdvisorDescriptor(config, false);
    }
}

/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Checks;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;

public class WizardDescriptorsTest {

    @Test
    public void testEmptyDescriptorList() {
        SnippetRepositoryConfiguration config = mock(SnippetRepositoryConfiguration.class);

        List<WizardDescriptor> descriptors = Lists.newArrayList();

        List<WizardDescriptor> filteredDescriptors = WizardDescriptors.filterApplicableWizardDescriptors(descriptors,
                config);

        assertThat(filteredDescriptors.isEmpty(), is(true));
    }

    @Test
    public void testNoApplicableWizardDescriptorAvailable() {
        SnippetRepositoryConfiguration config = mock(SnippetRepositoryConfiguration.class);

        List<WizardDescriptor> descriptors = Lists.newArrayList(createMock(config));

        SnippetRepositoryConfiguration otherConfig = mock(SnippetRepositoryConfiguration.class);
        List<WizardDescriptor> filteredDescriptors = WizardDescriptors.filterApplicableWizardDescriptors(descriptors,
                otherConfig);

        assertThat(filteredDescriptors.isEmpty(), is(true));
    }

    @Test
    public void testOneApplicableWizardDescriptorAvailable() {
        SnippetRepositoryConfiguration config = mock(SnippetRepositoryConfiguration.class);
        SnippetRepositoryConfiguration otherConfig = mock(SnippetRepositoryConfiguration.class);

        WizardDescriptor wizardMock = createMock(config);
        List<WizardDescriptor> descriptors = Lists.newArrayList(wizardMock, createMock(otherConfig));

        List<WizardDescriptor> filteredDescriptors = WizardDescriptors.filterApplicableWizardDescriptors(descriptors,
                config);

        assertThat(filteredDescriptors.size(), is(1));
        assertThat(filteredDescriptors, hasItem(wizardMock));
    }

    @Test
    public void testMoreApplicableWizardDescriptorAvailable() {
        SnippetRepositoryConfiguration config = mock(SnippetRepositoryConfiguration.class);
        SnippetRepositoryConfiguration otherConfig = mock(SnippetRepositoryConfiguration.class);

        WizardDescriptor wizardMock = createMock(config);
        WizardDescriptor otherWizardMock = createMock(config);
        List<WizardDescriptor> descriptors = Lists.newArrayList(wizardMock, otherWizardMock, createMock(otherConfig));

        List<WizardDescriptor> filteredDescriptors = WizardDescriptors.filterApplicableWizardDescriptors(descriptors,
                config);

        assertThat(filteredDescriptors.size(), is(2));
        assertThat(filteredDescriptors, hasItems(wizardMock, otherWizardMock));
    }

    private static WizardDescriptor createMock(final SnippetRepositoryConfiguration applicableConfiguration) {
        AbstractSnippetRepositoryWizard wizard = mock(AbstractSnippetRepositoryWizard.class);
        when(wizard.isApplicable(Mockito.<SnippetRepositoryConfiguration>anyObject())).thenAnswer(
                new Answer<Boolean>() {

                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                        SnippetRepositoryConfiguration config = Checks.cast(invocation.getArguments()[0]);
                        return config.equals(applicableConfiguration);
                    }
                });
        return new WizardDescriptor("", wizard);
    }

}

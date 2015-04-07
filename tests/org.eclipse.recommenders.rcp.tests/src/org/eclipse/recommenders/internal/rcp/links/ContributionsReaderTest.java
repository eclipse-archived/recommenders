/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasser Aziza - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.links;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ContributionsReaderTest {

    private static final String CONTRIBUTION_ELEMENT = "linkContribution";

    private static final String PREF_PAGE_ID_ATTRIBUTE = "preferencePageId";
    private static final String LABEL_ATTRIBUTE = "label";
    private static final String COMMAND_ID_ATTRIBUTE = "commandId";
    private static final String PRIORITY_ATTRIBUTE = "priority";
    private static final String ICON_ELEMENT = "icon";

    /**
     * Bundle-SymbolicName required in order to load the {@link DEFAULT_LINK_ICON}.
     */
    private static final String CONTRIBUTOR_ID = "org.eclipse.recommenders.rcp.tests";

    private static final String DEFAULT_LINK_ICON = "icons/obj16/eclipse.png";

    private IConfigurationElement mockConfigElement(String name, Map<String, String> map) {
        IConfigurationElement element = mock(IConfigurationElement.class, RETURNS_DEEP_STUBS);

        when(element.getName()).thenReturn(name);
        when(element.getContributor().getName()).thenReturn(CONTRIBUTOR_ID);

        for (String key : map.keySet()) {
            when(element.getAttribute(key)).thenReturn(map.get(key));
        }

        return element;
    }

    @Test
    public void testReadContributionLinksIsNullSafe() {
        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                (IConfigurationElement[]) null);

        assertThat(links.size(), is(equalTo(0)));
    }

    @Test
    public void testNoExtensionsFound() {
        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                new IConfigurationElement[] {});

        assertThat(links.size(), is(equalTo(0)));
    }

    @Test
    public void testNoExtensionsFoundForPreferencePageId() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", PRIORITY_ATTRIBUTE, "10", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.other",
                extension);

        assertThat(links.size(), is(equalTo(0)));
    }

    @Test
    public void testFieldsArePopulatedFromExtension() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", PRIORITY_ATTRIBUTE, "10", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                extension);

        ContributionLink link = getOnlyElement(links);
        assertThat(link.getText(), is(equalTo("<a>some label</a>")));
        assertThat(link.getCommandId(), is(equalTo("org.example.commands.id")));
        assertThat(link.getPriority(), is(equalTo(10)));
        assertThat(link.getIcon(), is(notNullValue()));
    }

    @Test
    public void testContributionLinkWithoutIcon() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", PRIORITY_ATTRIBUTE, "10"));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                extension);

        ContributionLink link = getOnlyElement(links);
        assertThat(link.getText(), is(equalTo("<a>some label</a>")));
        assertThat(link.getCommandId(), is(equalTo("org.example.commands.id")));
        assertThat(link.getPriority(), is(equalTo(10)));
        assertThat(link.getIcon(), is(nullValue()));
    }

    @Test
    public void testContributionLinkWithMissingIcon() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", PRIORITY_ATTRIBUTE, "10", ICON_ELEMENT,
                "invalid"));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                extension);

        assertThat(links.size(), is(equalTo(0)));
    }

    @Test
    public void testContributionLinkWithoutPriorityFallsBackToDefault() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                extension);

        ContributionLink link = getOnlyElement(links);
        assertThat(link.getText(), is(equalTo("<a>some label</a>")));
        assertThat(link.getCommandId(), is(equalTo("org.example.commands.id")));
        assertThat(link.getPriority(), is(equalTo(Integer.MAX_VALUE)));
        assertThat(link.getIcon(), is(not(nullValue())));
    }

    @Test
    public void testContributionReaderWithInvalidPriority() {
        IConfigurationElement extension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>some label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.id", PRIORITY_ATTRIBUTE, "invalid", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                extension);

        assertThat(links.size(), is(equalTo(0)));
    }

    @Test
    public void testContributionReaderPreservesValidExtensions() {
        IConfigurationElement firstExtension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>first label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.first", PRIORITY_ATTRIBUTE, "10", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        IConfigurationElement invalidExtension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id"));

        IConfigurationElement thirdExtension = mockConfigElement(CONTRIBUTION_ELEMENT, ImmutableMap.of(
                PREF_PAGE_ID_ATTRIBUTE, "org.example.preferencePages.id", LABEL_ATTRIBUTE, "<a>third label</a>",
                COMMAND_ID_ATTRIBUTE, "org.example.commands.third", PRIORITY_ATTRIBUTE, "20", ICON_ELEMENT,
                DEFAULT_LINK_ICON));

        List<ContributionLink> links = ContributionsReader.readContributionLinks("org.example.preferencePages.id",
                firstExtension, invalidExtension, thirdExtension);

        assertThat(links.size(), is(equalTo(2)));
    }
}

/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.rcp;

import static com.google.common.base.Optional.absent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.internal.models.rcp.Advisors;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.advisors.AbstractProjectCoordinateAdvisor;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AdvisorsTest {

    public static final IProjectCoordinateAdvisor A1 = new Advisor1();
    public static final IProjectCoordinateAdvisor A2 = new Advisor2();
    public static final IProjectCoordinateAdvisor A3 = new Advisor3();

    @Test
    public void testPrefStringCreationWithEmptyAdvisorList() {

        String prefString = Advisors.createPreferenceString(Collections.<IProjectCoordinateAdvisor>emptyList(),
                Collections.<IProjectCoordinateAdvisor>emptySet());
        assertEquals("", prefString);
    }

    @Test
    public void testPrefStringCreationAllAdvisorDisabled() {
        String actual = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A1, A2, A3));
        String expected = "!" + A1.getClass().getName() + ";!" + A2.getClass().getName() + ";!"
                + A3.getClass().getName() + ";";
        assertEquals(expected, actual);
    }

    @Test
    public void testPrefStringCreationAllAdvisorEnabled() {
        String actual = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Collections.<IProjectCoordinateAdvisor>emptySet());
        String expected = A1.getClass().getName() + ";" + A2.getClass().getName() + ";" + A3.getClass().getName() + ";";
        assertEquals(expected, actual);
    }

    @Test
    public void testPrefStringCreationOneAdvisorEnabled() {
        String actual = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A1, A3));
        String expected = "!" + A1.getClass().getName() + ";" + A2.getClass().getName() + ";!"
                + A3.getClass().getName() + ";";
        assertEquals(expected, actual);
    }

    @Test
    public void testPrefStringCreationOrderOfAdvisorsMatters() {
        String actual = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A3, A1, A2),
                Collections.<IProjectCoordinateAdvisor>emptySet());
        String expected = A3.getClass().getName() + ";" + A1.getClass().getName() + ";" + A2.getClass().getName() + ";";
        assertEquals(expected, actual);
    }

    @Test
    public void testPrefStringCreationTwoAdvisorEnabled() {
        String actual = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A1));
        String expected = "!" + A1.getClass().getName() + ";" + A2.getClass().getName() + ";" + A3.getClass().getName()
                + ";";
        assertEquals(expected, actual);
    }

    @Test
    public void testAdvisorListCreationWithEmptyParameters() {
        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(
                Collections.<IProjectCoordinateAdvisor>emptyList(), "");
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testAdvisorListCreationWithAllAdvisorsEnabled() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        String prefString = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Collections.<IProjectCoordinateAdvisor>emptySet());

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, prefString);
        assertEquals(availableAdvisors, actual);
    }

    @Test
    public void testAdvisorListCreationWithAllAdvisorsDisabled() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        String prefString = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A1, A2, A3));

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, prefString);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testAdvisorListCreationWithOneAdvisorsDisabled() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        String prefString = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A2, A3));

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, prefString);
        assertEquals(Lists.newArrayList(A1), actual);
    }

    @Test
    public void testAdvisorListCreationWithTwoAdvisorsDisabled() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        String prefString = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A1, A2, A3),
                Sets.<IProjectCoordinateAdvisor>newHashSet(A3));

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, prefString);
        assertEquals(Lists.newArrayList(A1, A2), actual);
    }

    @Test
    public void testAdvisorListCreationWithAdvisorOrderMatters() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        String prefString = Advisors.createPreferenceString(Lists.<IProjectCoordinateAdvisor>newArrayList(A3, A1, A2),
                Collections.<IProjectCoordinateAdvisor>emptySet());

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, prefString);
        assertEquals(Lists.newArrayList(A3, A1, A2), actual);
    }

    @Test
    public void testAdvisorListCreationWithEmptyPrefString() {
        List<IProjectCoordinateAdvisor> availableAdvisors = Lists.newArrayList(A1, A2, A3);

        List<IProjectCoordinateAdvisor> actual = Advisors.createAdvisorList(availableAdvisors, "");
        assertEquals(Lists.newArrayList(), actual);
    }
}

class Advisor1 extends DefaultAdvisor {
}

class Advisor2 extends DefaultAdvisor {
}

class Advisor3 extends DefaultAdvisor {
}

class DefaultAdvisor extends AbstractProjectCoordinateAdvisor {

    @Override
    protected boolean isApplicable(DependencyType type) {
        return false;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        return absent();
    }
}

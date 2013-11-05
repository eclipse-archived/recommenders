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
package org.eclipse.recommenders.internal.models.rcp;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;

import com.google.common.collect.Lists;

public class Advisors {

    private static final String DISABLED_FLAG = "!";
    private static final String SEPARATOR = ";";

    public static List<IProjectCoordinateAdvisor> createAdvisorList(List<IProjectCoordinateAdvisor> arrayList,
            String advisors) {
        String[] split = advisors.split(SEPARATOR);
        List<IProjectCoordinateAdvisor> advisorList = Lists.newArrayList();
        for (String name : split) {
            if (name.startsWith(DISABLED_FLAG)) {
                continue;
            }
            for (IProjectCoordinateAdvisor advisor : arrayList) {
                if (name.equals(advisor.getClass().getName())) {
                    advisorList.add(advisor);
                    break;
                }
            }
        }
        return advisorList;
    }

    public static String createPreferenceString(List<IProjectCoordinateAdvisor> orderedAdvisors,
            Set<IProjectCoordinateAdvisor> disabledAdvisors) {
        StringBuilder sb = new StringBuilder();

        for (IProjectCoordinateAdvisor advisor : orderedAdvisors) {
            if (disabledAdvisors.contains(advisor)) {
                sb.append(DISABLED_FLAG);
            }
            sb.append(advisor.getClass().getName());
            sb.append(SEPARATOR);
        }

        return sb.toString();
    }

}

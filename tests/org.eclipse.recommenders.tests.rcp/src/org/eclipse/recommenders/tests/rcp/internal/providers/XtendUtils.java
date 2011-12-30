/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.internal.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.xbase.lib.Pair;

import com.google.common.collect.Lists;

public class XtendUtils {
    public static final <K> List<K> newListWithFrequency(final Pair<K, Integer>... initial) {
        final ArrayList<K> result = Lists.newArrayList();
        for (final Pair<K, Integer> p : initial) {
            for (int i = p.getValue(); i-- > 0;) {
                result.add(p.getKey());
            }
        }
        return result;
    }
}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import smile.Network;

public class AvailabilityNode extends AbstractNode {
    static String ID = "availability";

    private static int TRUE = 0;

    private static int FALSE = 1;

    protected AvailabilityNode(final Network network) {
        super(network, ID);
    }

    public double getProbability() {
        final double p = getValues()[TRUE];
        return p;
    }

    public void setEvidence(final boolean value) {
        final int outcomeIndex = value ? TRUE : FALSE;
        setEvidence(outcomeIndex);
    }

}

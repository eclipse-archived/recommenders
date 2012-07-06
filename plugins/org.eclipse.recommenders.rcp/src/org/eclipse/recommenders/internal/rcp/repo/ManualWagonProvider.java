/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.repo;

import org.apache.maven.wagon.Wagon;
import org.sonatype.maven.wagon.AhcWagon;

/**
 * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
 */
public class ManualWagonProvider implements org.sonatype.aether.connector.wagon.WagonProvider {

    @Override
    public Wagon lookup(String roleHint) throws Exception {
        if ("http".equals(roleHint) || "https".equals(roleHint)) {
            AhcWagon ahcWagon = new AhcWagon();
            // TODO set timeout to 300s instead of 60s to solve timeouts. experimental.
            ahcWagon.setTimeout(300*1000);
            return ahcWagon;
            // return new WebDavWagon();
        }
        return null;
    }

    @Override
    public void release(Wagon wagon) {
    }

}

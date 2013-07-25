/**
 * Copyright (c) 2013 Timur Achmetow.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Timur Achmetow - initial API and implementation
 */
package org.eclipse.recommenders.sandbox.rcp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.osgi.util.NLS;

public class DateFormatter {

    public final String formatUnit(final Date past, final Date now) {
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
        final long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
        final long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

        if (days > 0) {
            return NLS.bind("{0} days ago", days);
        } else if (hours > 0) {
            return NLS.bind("{0} hours ago", hours);
        } else if (minutes >= 0) {
            return NLS.bind("{0} minutes ago", minutes);
        }
        return null;
    }
}

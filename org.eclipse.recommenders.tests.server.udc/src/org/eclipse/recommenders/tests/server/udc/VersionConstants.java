/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.server.udc;

import org.eclipse.recommenders.commons.utils.Version;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.eclipse.recommenders.commons.utils.VersionRange.VersionRangeBuilder;

public class VersionConstants {
    public static Version v23 = Version.create(2, 3);
    public static Version v24 = Version.create(2, 4);
    public static Version v35 = Version.create(3, 5);
    public static Version v36 = Version.create(3, 6);
    public static Version v362 = Version.create(3, 6, 2);
    public static Version v37 = Version.create(3, 7);
    public static Version v38 = Version.create(3, 8);
    public static Version v39 = Version.create(3, 9);

    public static VersionRange vi23_e24 = new VersionRangeBuilder().minInclusive(v23).maxExclusive(v24).build();
    public static VersionRange vi35_e36 = new VersionRangeBuilder().minInclusive(v35).maxExclusive(v36).build();
    public static VersionRange vi36_e37 = new VersionRangeBuilder().minInclusive(v36).maxExclusive(v37).build();
    public static VersionRange vi37_e38 = new VersionRangeBuilder().minInclusive(v37).maxExclusive(v38).build();
    public static VersionRange vi35_e37 = new VersionRangeBuilder().minInclusive(v35).maxExclusive(v37).build();
    public static VersionRange vi38_e39 = new VersionRangeBuilder().minInclusive(v38).maxExclusive(v39).build();
}

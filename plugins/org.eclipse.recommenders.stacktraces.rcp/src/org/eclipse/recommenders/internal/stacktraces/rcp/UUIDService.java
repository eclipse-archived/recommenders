/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.apache.commons.lang3.SystemUtils.getUserHome;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

class UUIDService {

    private static UUID userId;

    public static UUID getAnonmyousId() {
        if (userId == null) {
            try {
                userId = readOrCreateUUID();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        return userId;
    }

    private static UUID readOrCreateUUID() throws IOException {
        File f = new File(getUserHome(), ".eclipse/org.eclipse.recommenders.stats/anonymousId");
        if (f.exists()) {
            String uuid = Files.readFirstLine(f, Charsets.UTF_8);
            return UUID.fromString(uuid);
        } else {
            f.getParentFile().mkdirs();
            UUID uuid = UUID.randomUUID();
            Files.write(uuid.toString(), f, Charsets.UTF_8);
            return uuid;
        }
    }
}

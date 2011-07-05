/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.server.extdoc.types.IServerType;

final class CouchDbImport {

    private CouchDbImport() {
    }

    public static void main(final String[] args) throws IOException {
        // class-overrides-directives - ClassOverrideDirective
        // class-selfcalls - ClassSelfcallDirective
        // method-selfcalls - MethodSelfcallDirective
        // importZip(new
        // ZipFile("C:/Users/henss/Desktop/json/class-selfcalls.zip"),
        // ClassSelfcallDirective.class);
    }

    private static <T extends IServerType> void importZip(final ZipFile zipFile, final Class<T> type)
            throws IOException {
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final IServerType serverType = GsonUtil.deserialize(zipFile.getInputStream(entries.nextElement()), type);
            serverType.validate();
            Server.post(serverType);
        }
    }

}

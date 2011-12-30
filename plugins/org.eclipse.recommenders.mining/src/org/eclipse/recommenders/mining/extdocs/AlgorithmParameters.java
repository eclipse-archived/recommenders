/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.extdocs;

import static org.eclipse.recommenders.utils.Checks.ensureExists;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.Throws;
import org.kohsuke.args4j.Option;

public class AlgorithmParameters {

    @Option(name = "--in", usage = "Either an Apache CouchDB URL (such as 'http://localhost:5984/database') or a path to a local zip file containing compilation units in JSON form (as created by Eclipse API usage data export). Defaults to 'http://localhost:5984/udc'.")
    private String in = "http://localhost:5984/udc";

    @Option(name = "--out", usage = "An Apache CouchDB URL (such as 'http://localhost:5984/database') pointing to an initialized Extdoc database. Defaults to 'http://localhost:5984/extdoc'.")
    private String out = "http://localhost:5984/extdoc";

    public AlgorithmParameters(final String in, final String out) {
        super();
        this.in = in;
        this.out = out;
    }

    public AlgorithmParameters() {
    }

    public ZipFile getZip() {
        try {
            final File file = new File(in);
            ensureExists(file);
            return new ZipFile(file);
        } catch (final IOException e) {
            throw Throws.throwIllegalArgumentException("--in %s is not a valid zip file", in);
        }
    }

    public boolean startsInWithHttp() {
        return in.startsWith("http://");
    }

    public URL getInputHost() {
        try {
            return new URL(in);
        } catch (final MalformedURLException e) {
            throw Throws.throwIllegalArgumentException("--in %s is not an URL", in);
        }
    }

    public URL getOutputHost() {
        try {
            return new URL(out);
        } catch (final MalformedURLException e) {
            throw Throws.throwIllegalArgumentException("--out %s is not an URL", in);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

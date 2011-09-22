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
package org.eclipse.recommenders.mining.calls;

import static org.eclipse.recommenders.commons.utils.Checks.ensureExists;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.VersionRange;
import org.kohsuke.args4j.Option;

public class AlgorithmParameters {

    @Option(name = "--in", usage = "Either an Apache CouchDB URL (such as 'http://localhost:5984/database') or a path to a local zip file containing compilation units in JSON form (as created by Eclipse API usage data export). Defaults to 'http://localhost:5984/udc'.")
    private String in = "http://localhost:5984/udc";

    @Option(name = "--out", usage = "The destination directory for generated model archives. Defaults to './out/'.")
    private File out = new File("out");

    @Option(name = "--symbolic-name", usage = "The symbolic name of the model's manifest. Only used if --in points to a zip file. Defaults to 'custom'.")
    private String symbolicName = "custom";

    @Option(name = "--version-range", usage = "The version range of the model's manifest (such as '[3.6.0,4.0.0)'). Only used if -in points to a zip file. Defaults to '(0.0.0,0.0.0)'.")
    private String versionRange = "(0.0.0,0.0.0)";

    @Option(name = "--update-specs", usage = "Generates the missing model specifications. Only used if --in points to a CouchDB. Defaults to 'false'.")
    private boolean updateSpecs = false;

    @Option(name = "--force", usage = "Forces model generation - even if no new object usages could be found. Only used if --in points to a CouchDB. Defaults to 'false'.")
    private boolean force = false;

    public AlgorithmParameters(final String in, final File out, final String symbolicName, final String versionRange,
            final boolean updateSpecs, final boolean force) {
        super();
        this.in = in;
        this.out = out;
        this.symbolicName = symbolicName;
        this.versionRange = versionRange;
        this.updateSpecs = updateSpecs;
        this.force = force;
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

    public URL getHost() {
        try {
            return new URL(in);
        } catch (final MalformedURLException e) {
            throw Throws.throwIllegalArgumentException("--in %s is not an URL", in);
        }
    }

    public File getOut() {
        return out;
    }

    public void setOut(final File out) {
        this.out = out;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public VersionRange getVersionRange() {
        return VersionRange.create(versionRange);
    }

    public boolean isUpdateSpecs() {
        return updateSpecs;
    }

    public boolean isForce() {
        return force;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public void setForce(final boolean newValue) {
        this.force = newValue;

    }
}

package org.eclipse.recommenders.internal.rcp.analysis;

import static org.eclipse.recommenders.commons.utils.Checks.ensureExists;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.ClasspathEntry;
import static org.eclipse.recommenders.internal.rcp.analysis.RcpAnalysisModule.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ClasspathEntryStore {

    private final File basedir;

    @Inject
    public ClasspathEntryStore(@Named(CLASSPATH_ENTRY_STORE_BASEDIR) final File basedir) {
        this.basedir = basedir;
    }

    public boolean hasModel(final String fingerprint) {
        final File f = computeModelFileHandle(fingerprint);
        return f.exists();
    }

    public ClasspathEntry get(final String fingerprint) {
        final File f = computeModelFileHandle(fingerprint);
        ensureExists(f);
        try {
            return GsonUtil.deserialize(f, ClasspathEntry.class);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    private File computeModelFileHandle(final String fingerprint) {
        return new File(basedir, fingerprint + ".json");
    }

    public void register(final ClasspathEntry entry) {
        ensureIsNotNull(entry.fingerprint);
        final File f = computeModelFileHandle(entry.fingerprint);
        GsonUtil.serialize(entry, f);
    }

    public void deregister(final ClasspathEntry entry) {
        ensureIsNotNull(entry.fingerprint);
        final File f = computeModelFileHandle(entry.fingerprint);
        f.delete();
    }
}

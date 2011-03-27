package org.eclipse.recommenders.internal.rcp.analysis.cp;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.recommenders.commons.utils.Version;
import org.osgi.framework.Constants;

public class BundleManifestVersionFinder implements IVersionFinder {

    private static final Name ATTRIBUTE_NAME_BUNDLE_VERSION = new Attributes.Name(Constants.BUNDLE_VERSION_ATTRIBUTE);

    @Override
    public Version find(final File file) {
        try {
            return doFindVersion(file);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    private Version doFindVersion(final File file) throws IOException {
        final JarFile jarFile = new JarFile(file);
        final Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            return UNKNOWN;
        }
        final Attributes mainAttributes = manifest.getMainAttributes();
        final String version = (String) mainAttributes.get(ATTRIBUTE_NAME_BUNDLE_VERSION);
        return version == null ? UNKNOWN : Version.valueOf(version);
    }

}

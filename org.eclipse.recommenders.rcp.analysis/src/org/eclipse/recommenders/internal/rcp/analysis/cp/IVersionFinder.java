package org.eclipse.recommenders.internal.rcp.analysis.cp;

import java.io.File;

import org.eclipse.recommenders.commons.utils.Version;

public interface IVersionFinder {

    Version UNKNOWN = Version.create(0, 0, 0);

    Version find(File file);
}

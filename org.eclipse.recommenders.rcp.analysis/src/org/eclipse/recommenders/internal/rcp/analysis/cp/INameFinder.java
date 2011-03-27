package org.eclipse.recommenders.internal.rcp.analysis.cp;

import java.io.File;

public interface INameFinder {

    String UNKNOWN = "unknown";

    String find(final File file) throws Exception;
}

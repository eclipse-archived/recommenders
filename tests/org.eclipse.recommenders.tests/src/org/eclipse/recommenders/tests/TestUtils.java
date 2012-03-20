package org.eclipse.recommenders.tests;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class TestUtils {

    public static boolean isEclipse4() {
        IProduct product = Platform.getProduct();
        if (product == null)
            return false;
        Bundle definingBundle = product.getDefiningBundle();
        Version version = definingBundle.getVersion();
        return version.getMajor() > 3;
    }
}

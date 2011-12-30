package org.eclipse.recommenders.internal.extdoc.rcp.wiring;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ExtdocPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.recommenders.extdoc.rcp";
    private static ExtdocPlugin INSTANCE;

    public static ExtdocPlugin getDefault() {
        return INSTANCE;
    }

    @Override
    public void start(final org.osgi.framework.BundleContext context) throws Exception {
        super.start(context);
        INSTANCE = this;
    };
}

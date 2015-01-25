package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.hamcrest.Matchers.*;

import java.util.Collection;

import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.junit.Assert;
import org.junit.Test;

public class WhitelistTest {

    Settings s = PreferenceInitializer.readSettings();

    @Test
    public void testWhitelistPackages() throws Exception {
        Collection<String> sut = s.getWhitelistedPackages();
        Assert.assertThat(sut, not(hasItems("")));
        Assert.assertThat(
                sut,
                hasItems("org.eclipse.", "org.apache.", "ch.qos.", "org.slf4j.", "java.", "javax.", "javafx.", "sun.",
                        "com.sun.", "com.codetrails.", "org.osgi.", "com.google."));

    }

    @Test
    public void testWhitelistPlugins() throws Exception {
        Collection<String> sut = s.getWhitelistedPluginIds();
        Assert.assertThat(sut, not(hasItems("")));
        Assert.assertThat(sut, hasItems("org.eclipse.", "org.apache.log4j", "com.codetrails"));

    }

}

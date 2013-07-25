package org.eclipse.recommenders.tests.rcp.repo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.BayesNetWrapper;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

public class CallModelReadabilityTests {

    private static final File LOCAL_CR_REPO = new File("/Volumes/usb/juno-m6/models");

    @Test
    public void test() throws IOException {
        for (File zip : FileUtils.listFiles(LOCAL_CR_REPO, new SuffixFileFilter("-call.zip"),
                DirectoryFileFilter.DIRECTORY)) {
            System.out.println("checking " + zip);
            ZipFile zipFile = new ZipFile(zip);
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
                ZipEntry next = entries.nextElement();
                try {
                    InputStream is = zipFile.getInputStream(next);
                    BayesianNetwork net = BayesianNetwork.read(is);
                    new BayesNetWrapper(VmTypeName.BOOLEAN, net).getMethodCalls();
                    IOUtils.closeQuietly(is);
                } catch (Exception e) {
                    fail("Failed to read " + next + " from " + zip);
                }
            }
            System.out.println(zip + " passed read in check.");
        }
    }
}

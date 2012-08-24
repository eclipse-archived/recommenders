/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rdk.index;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.filefilter.HiddenFileFilter.VISIBLE;
import static org.eclipse.recommenders.rdk.utils.Artifacts.toArtifactFileName;
import static org.eclipse.recommenders.utils.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.rdk.index.ModelDocuments.AdditionalMetadata;
import org.eclipse.recommenders.rdk.index.ModelDocuments.ModelDocument;
import org.eclipse.recommenders.rdk.utils.Artifacts;
import org.eclipse.recommenders.utils.Fingerprints;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.annotations.Provisional;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.parser.MavenVersionParser;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * Creates a model index file from the given repository, i.e., it scans the whole maven repository for model archives
 * and adds them to the index file.
 */
@Provisional
public class ModelDocumentsWriter {
    private static Logger log = LoggerFactory.getLogger(ModelDocumentsWriter.class);

    private String[] classifiers = { CLASS_CALL_MODELS, CLASS_OVRM_MODEL, CLASS_OVRD_MODEL, CLASS_OVRP_MODEL,
            CLASS_SELFM_MODEL, CLASS_SELFC_MODEL, CLASS_CHAIN_MODEL };

    private File basedir;
    private File dest;
    private Map<Artifact, ModelDocument> modelDocs = Maps.newHashMap();

    public static void main(String[] args) {
        new ModelDocumentsWriter(new File("/Volumes/usb/juno-m6/m2/"),
                new File("/Volumes/usb/juno-m6/m2/metadata.json")).run();
    }

    public ModelDocumentsWriter(final File mavenRepository, final File dest) {
        this.basedir = mavenRepository;
        this.dest = dest;
    }

    public void run() {
        createAndWriteModelDocuments();
        write();

    }

    private void createAndWriteModelDocuments() {
        for (final File fPom : listFiles(basedir, new SuffixFileFilter(".pom"), VISIBLE)) {
            try {
                analyze(fPom);
            } catch (Exception e) {
                log.error("Exception while analyzing {}: {}", fPom, Throwables.getRootCause(e).getMessage());
            }
        }
    }

    private void analyze(File fPom) throws Exception {
        Artifact pom = Artifacts.extractCoordinateFromPom(fPom);
        if (pom.getArtifactId().endsWith(".source")) {
            log.debug("skipping source artifact {}", pom);
            return;
        }
        Optional<Artifact> aggregator = computeAggregator(pom);
        if (!aggregator.isPresent()) {
            log.warn("Could not determine aggregator for {}. Skipping.", pom);
            return;
        }
        log.info("Indexing {}.", pom);

        Artifact jar = Artifacts.newClassifierAndExtension(pom, null, "jar");

        ModelDocument doc = findOrCreateAggregationModelDocument(aggregator.get());

        File fJar = new File(fPom.getParentFile(), toArtifactFileName(jar));
        if (fJar.exists()) {
            String fingerprint = getFingerprint(fJar);
            Optional<String> symbolicName = findSymbolicName(fJar);

            doc.fingerprints.add(fingerprint);
            if (symbolicName.isPresent())
                doc.symbolicNames.add(symbolicName.get());
        }
        // if no jar found, guess the symbolic name:
        else {
            doc.symbolicNames.add(jar.getArtifactId());
        }

        checkAndAddAdditionalModelsMetadata(fPom, doc);

        for (String classifier : classifiers)
            checkAndAddModelArchive(pom, classifier, fPom, doc);
        return;
    }

    private void checkAndAddAdditionalModelsMetadata(File fPom, ModelDocument doc) {
        File fMetadata = new File(fPom.getParentFile(), "recommenders-metadata.json");
        if (!fMetadata.exists())
            return;
        AdditionalMetadata metadata = GsonUtil.deserialize(fMetadata, AdditionalMetadata.class);
        log.info("adding manually specified metadata to {}", doc.coordinate);
        doc.models.addAll(metadata.models);
    }

    private void checkAndAddModelArchive(Artifact pom, String classifier, File fPom, ModelDocument doc) {
        Artifact model = Artifacts.newClassifierAndExtension(pom, classifier, "zip");
        File fModel = new File(fPom.getParentFile(), toArtifactFileName(model));
        if (fModel.exists()) {
            String coord = Artifacts.asCoordinate(model);
            doc.models.add(coord);
        }
    }

    private Optional<Artifact> computeAggregator(Artifact pom) {
        Optional<Version> version = parseVersion(pom);
        Artifact aggregator = null;
        if (version.isPresent()) {
            String aggVersion = version.get().major + ".0.0";
            aggregator = new DefaultArtifact(pom.getGroupId(), pom.getArtifactId(), "", aggVersion);
        }
        return fromNullable(aggregator);
    }

    private Optional<Version> parseVersion(Artifact pom) {
        try {
            MavenVersionParser parser = new MavenVersionParser();
            return of(parser.parse(pom.getVersion()));
        } catch (IllegalArgumentException e) {
            return absent();
        }
    }

    private ModelDocument findOrCreateAggregationModelDocument(Artifact aggregator) {
        ModelDocument doc = modelDocs.get(aggregator);
        if (doc == null) {
            doc = new ModelDocument();
            doc.coordinate = String.format("%s:%s:%s", aggregator.getGroupId(), aggregator.getArtifactId(),
                    aggregator.getVersion());
            modelDocs.put(aggregator, doc);
        }
        return doc;
    }

    private void write() {
        ModelDocuments c = new ModelDocuments();
        c.entries.addAll(modelDocs.values());
        try {
            System.out.println("writing index contents to file " + dest.getAbsoluteFile());
            Files.createParentDirs(dest);
            final FileWriter writer = new FileWriter(dest);
            GsonUtil.serialize(c, writer);
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<String> findSymbolicName(final File file) {
        try {
            final JarFile jarFile = new JarFile(file);
            final Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                final Attributes attributes = manifest.getMainAttributes();
                String symbolicName = attributes.getValue(new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME));
                symbolicName = StringUtils.substringBefore(symbolicName, ";");
                return Optional.fromNullable(symbolicName);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return Optional.absent();
    }

    private static String getFingerprint(final File jarFile) {
        final File fingerprintFile = getFingerprintFile(jarFile);
        if (fingerprintFile.exists()) {
            try {
                return readFingerprint(fingerprintFile);
            } catch (final IOException e) {
            }
        }
        final String fingerprint = Fingerprints.sha1(jarFile);
        write(fingerprint, fingerprintFile);
        return fingerprint;
    }

    private static String readFingerprint(final File fingerprintFile) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(fingerprintFile));
        final String line = reader.readLine();
        IOUtils.closeQuietly(reader);
        return StringUtils.substringBefore(line, " ");

    }

    private static void write(final String fingerprint, final File fingerprintFile) {
        try {
            final FileWriter fileWriter = new FileWriter(fingerprintFile);
            fileWriter.append(fingerprint);
            fileWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static File getFingerprintFile(final File jarFile) {
        return new File(jarFile.getAbsolutePath() + ".sha1");
    }
}

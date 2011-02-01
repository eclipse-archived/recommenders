/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.recommenders.commons.utils.FixedSizeLinkedHashMap;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net.ClassOverridesNetwork;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net.ClassOverridesNetworkBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net.ClassOverridesObservation;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

@Clumsy
public class OverridesModelStore {
    private static final String FILENAME_PREFIX = "class-overrides-";

    private Set<ITypeName> supportedTypes;

    private final Map<ITypeName, ClassOverridesNetwork> loadedNetworks = FixedSizeLinkedHashMap.create(30);

    private void init() {
        if (supportedTypes == null) {
            supportedTypes = computeSupportedTypes();
        }
    }

    protected Set<ITypeName> computeSupportedTypes() {
        final Set<ITypeName> types = Sets.newTreeSet();
        final ZipFile modelsZip = getModelsFile();
        final List<? extends ZipEntry> entries = Collections.list(modelsZip.entries());
        for (final ZipEntry entry : entries) {
            final String fileName = entry.getName();
            if (!fileName.endsWith("json")) {
                continue;
            }
            final String typeNameAsString = StringUtils.substringBeforeLast(fileName, ".").replace('.', '/')
                    .substring(FILENAME_PREFIX.length());
            final VmTypeName typeName = VmTypeName.get(typeNameAsString);
            types.add(typeName);
        }
        return types;
    }

    public boolean hasModel(@Nullable final ITypeName name) {
        init();
        return name == null ? false : supportedTypes.contains(name);
    }

    public ClassOverridesNetwork getModel(final ITypeName name) {
        ensureIsTrue(hasModel(name));
        //
        ClassOverridesNetwork network = loadedNetworks.get(name);
        if (network == null) {
            network = loadNetworkFromDisk(name);
            loadedNetworks.put(name, network);
        }
        return network;
    }

    private JarFile getModelsFile() {
        try {
            final Bundle bundle = FrameworkUtil.getBundle(this.getClass());
            final Path basedir = new Path("/data/models.zip");
            final URL path = FileLocator.resolve(FileLocator.find(bundle, basedir, null));
            final String file = path.getFile();
            return new JarFile(file);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    private ClassOverridesNetwork loadNetworkFromDisk(final ITypeName name) {
        final StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        final Type listType = new TypeToken<List<ClassOverridesObservation>>() {
        }.getType();
        try {
            stopwatch.split();
            final InputStream is = getModelInputStream(name);
            final List<ClassOverridesObservation> observations = GsonUtil.deserialize(is, listType);
            System.out.printf("deserialization of '%s' took %s\n", name, stopwatch);
            if (observations.size() == 0) {
                // XXX sanitize bad models! need to ensure minimum quality for models.
                observations.add(new ClassOverridesObservation());
            }
            stopwatch.unsplit();
            final ClassOverridesNetworkBuilder b = new ClassOverridesNetworkBuilder(name, observations);
            b.createPatternsNode();
            b.createMethodNodes();
            final ClassOverridesNetwork network = b.build();
            return network;
        } catch (final IOException x) {
            throw throwUnhandledException(x);
        } finally {
            stopwatch.stop();
            System.out.printf("loading model for '%s' took %s\n", name, stopwatch);
        }
    }

    protected InputStream getModelInputStream(final ITypeName name) throws IOException {
        final JarFile modelsFile = getModelsFile();
        final String entryName = FILENAME_PREFIX + name.getIdentifier().replace('/', '.').concat(".json");
        final ZipEntry entry = modelsFile.getEntry(entryName);
        final InputStream is = modelsFile.getInputStream(entry);
        return is;
    }
}

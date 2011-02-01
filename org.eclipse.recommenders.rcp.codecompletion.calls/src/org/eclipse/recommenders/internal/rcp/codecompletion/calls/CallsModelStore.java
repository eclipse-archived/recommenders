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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
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
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.InstanceUsage;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.NetworkBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.ObjectMethodCallsNet;
import org.osgi.framework.Bundle;

import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

@Clumsy
public class CallsModelStore {

    private Set<ITypeName> supportedTypes;

    private final Map<ITypeName, ObjectMethodCallsNet> loadedNetworks = FixedSizeLinkedHashMap.create(100);

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
            final String typeNameAsString = StringUtils.substringBeforeLast(fileName, ".").replace('.', '/');
            final VmTypeName typeName = VmTypeName.get(typeNameAsString);
            // XXX workaround for errors like this one: failed to compute method call probability for
            // 'Lorg/eclipse/swt/internal/cocoa/NSImage.<init>(I)V'; node definition: [0.0, 1.0, 1.0E-4, 0.9999, 0.9999,
            // 1.0E-4]
            if (typeName.getPackage().getIdentifier().startsWith("Lorg/eclipse/swt/internal/cocoa/")) {
                continue;
            }
            types.add(typeName);
        }
        return types;
    }

    public boolean hasModel(@Nullable final ITypeName name) {
        init();
        return name == null ? false : supportedTypes.contains(name);
    }

    public ObjectMethodCallsNet getModel(final ITypeName name) {
        ensureIsTrue(hasModel(name));
        //
        ObjectMethodCallsNet network = loadedNetworks.get(name);
        if (network == null) {
            network = loadNetworkFromDisk(name);
            loadedNetworks.put(name, network);
        }
        return network;
    }

    private JarFile getModelsFile() {
        try {
            final Bundle bundle = CallsCompletionPlugin.getDefault().getBundle();
            final Path basedir = new Path("/data/models.zip");
            final URL path = FileLocator.resolve(FileLocator.find(bundle, basedir, null));
            final String file = path.getFile();
            return new JarFile(file);
        } catch (final IOException e) {
            throw throwUnhandledException(e);
        }
    }

    private ObjectMethodCallsNet loadNetworkFromDisk(final ITypeName name) {
        final StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        final Type listType = new TypeToken<List<InstanceUsage>>() {
        }.getType();
        try {
            stopwatch.split();
            final InputStream is = getModelInputStream(name);
            final List<InstanceUsage> usages = GsonUtil.deserialize(is, listType);
            for (final Iterator<InstanceUsage> it = usages.iterator(); it.hasNext();) {
                final InstanceUsage next = it.next();
                if (next.invokedMethods.isEmpty()) {
                    it.remove();
                    break;
                }
            }
            System.out.printf("deserialization of '%s' took %s\n", name, stopwatch);
            stopwatch.unsplit();
            final NetworkBuilder b = new NetworkBuilder(name, usages);
            b.createContextNode();
            b.createAvailabilityNode();
            b.createPatternsNode();
            b.createMethodNodes();
            final ObjectMethodCallsNet network = b.build();
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
        final String entryName = name.getIdentifier().replace('/', '.').concat(".json");
        final ZipEntry entry = modelsFile.getEntry(entryName);
        final InputStream is = modelsFile.getInputStream(entry);
        return is;
    }
}

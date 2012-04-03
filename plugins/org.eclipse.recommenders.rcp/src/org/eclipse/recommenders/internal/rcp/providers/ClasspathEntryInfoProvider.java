/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.providers;

import static com.google.common.base.Optional.fromNullable;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.eclipse.recommenders.utils.Executors.coreThreadsTimoutExecutor;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.AutoCloseOnWorkbenchShutdown;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JarPackageFragmentRootAdded;
import org.eclipse.recommenders.rcp.events.JavaModelEvents.JavaProjectOpened;
import org.eclipse.recommenders.rcp.events.NewClasspathEntryFound;
import org.eclipse.recommenders.utils.Version;
import org.eclipse.recommenders.utils.archive.ArchiveDetailsExtractor;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.parser.OsgiVersionParser;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
@Singleton
@AutoCloseOnWorkbenchShutdown
public class ClasspathEntryInfoProvider implements IClasspathEntryInfoProvider {

    /**
     * Single-threaded executor used to compute fingerprints etc. for package fragment root. Single-threaded because
     * it's an disk-IO heavy computation. More than one thread will probably not give any performance gains here.
     */
    private final ExecutorService pool = coreThreadsTimoutExecutor(1, Thread.MIN_PRIORITY,
            "Recommenders-Dependency-Info-Service-");

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<File, ClasspathEntryInfo> cpeInfos = Maps.newHashMap();
    private final File state;
    private final EventBus bus;

    @Inject
    public ClasspathEntryInfoProvider(File state, IWorkspaceRoot workspace, EventBus bus) {
        this.state = state;
        this.bus = bus;
        initialize();
        scanOpenProjects(workspace);
    }

    private void initialize() {
        if (!state.exists()) {
            return;
        }
        TypeToken token = new TypeToken<List<ClasspathEntryInfo>>() {
        };
        try {
            List<ClasspathEntryInfo> data = GsonUtil.deserialize(state, token.getType());
            for (ClasspathEntryInfo info : data) {
                cpeInfos.put(info.getLocation(), info);
                ensureFileInfosStillConsistent(info);
            }
        } catch (Exception e) {
            log.warn("Exception during deserialization of cached classpath infos. Discaring old state.", e);
        }
    }

    private void scanOpenProjects(IWorkspaceRoot workspace) {
        for (IProject p : workspace.getProjects()) {
            if (JavaProject.hasJavaNature(p)) {
                IJavaProject javaProject = JavaCore.create(p);
                onEvent(new JavaProjectOpened(javaProject));
            }
        }
    }

    @Override
    public Optional<ClasspathEntryInfo> getInfo(final File file) {
        ClasspathEntryInfo res = cpeInfos.get(file);
        ensureFileInfosStillConsistent(res);
        return fromNullable(res);
    }

    @Override
    public Set<File> getFiles() {
        return new HashSet<File>(cpeInfos.keySet());
    }

    private void ensureFileInfosStillConsistent(final ClasspathEntryInfo info) {
        if (info == null)
            return;

        File location = info.getLocation();
        if (location == null)
            return;
        if (location.lastModified() != info.getModificationDate().getTime())
            cpeInfos.remove(location);
    }

    @Override
    public void close() {
        try {
            Files.createParentDirs(state);
            GsonUtil.serialize(cpeInfos.values(), state);
        } catch (Exception e) {
            log.error("Failed to store classpath info state.", e);
        }
    }

    @Subscribe
    public void onEvent(final JavaProjectOpened e) {
        try {
            // XXX flight hack...
            IResource mf = e.project.getProject().findMember(new Path("META-INF/MANIFEST.MF"));
            if (mf != null && mf.exists() && mf.getType() == IResource.FILE) {
                Manifest mf_ = new Manifest(((IFile) mf).getContents());
                final Attributes attributes = mf_.getMainAttributes();
                // names may look like this: "symbolic.name;singleton=true":
                String symbolicName = substringBefore(attributes.getValue(Constants.BUNDLE_SYMBOLICNAME), ";");
                final String version = attributes.getValue(Constants.BUNDLE_VERSION);
                Version osgiversion = null;
                if (version != null) {
                    osgiversion = new OsgiVersionParser().parse(version);
                }
                for (IPackageFragmentRoot root : e.project.getPackageFragmentRoots()) {
                    if (root.isArchive())
                        continue;
                    ClasspathEntryInfo res = new ClasspathEntryInfo();
                    res.setSymbolicName(symbolicName);
                    res.setVersion(osgiversion);
                    File file = JdtUtils.getLocation(root).get();
                    res.setModificationDate(new Date(file.lastModified()));
                    res.setLocation(file);
                    cpeInfos.put(file, res);
                    bus.post(new NewClasspathEntryFound(root, file, res));
                }
            }
        } catch (Exception e1) {
            log.warn("failed to read bundle manifest for project " + e.project.getElementName(), e1);
        }

        try {
            for (final IPackageFragmentRoot r : e.project.getAllPackageFragmentRoots()) {
                final Optional<File> location = JdtUtils.getLocation(r);
                if (isInterestingPackageFragmentRoot(r, location)) {
                    resolve(r, location.get());
                }
            }
        } catch (final JavaModelException x) {
            log.error("Exception occurred while resolving project dependencies for " + e.project, x);
        }
    }

    private void resolve(final IPackageFragmentRoot r, final File file) {
        pool.submit(new Runnable() {

            @Override
            public void run() {
                // if it has been resolved in the meanwhile, skip
                if (cpeInfos.containsKey(file)) {
                    return;
                }
                if (r.isArchive()) {
                    try {
                        final ArchiveDetailsExtractor extractor = new ArchiveDetailsExtractor(file);
                        ClasspathEntryInfo res = new ClasspathEntryInfo();
                        res.setSymbolicName(extractor.extractName());
                        res.setVersion(extractor.extractVersion());
                        res.setFingerprint(extractor.createFingerprint());
                        res.setModificationDate(new Date(file.lastModified()));
                        res.setLocation(file);
                        cpeInfos.put(file, res);
                        bus.post(new NewClasspathEntryFound(r, file, res));
                    } catch (final Exception e) {
                        log.error("Extracing jar information failed with exception.", e);
                    }
                }
            }
        });
    }

    @Subscribe
    public void onEvent(final JarPackageFragmentRootAdded e) {
        final JarPackageFragmentRoot r = e.root;
        final Optional<File> location = JdtUtils.getLocation(r);
        if (isInterestingPackageFragmentRoot(r, location)) {
            resolve(r, location.get());
        }
    }

    private boolean isInterestingPackageFragmentRoot(final IPackageFragmentRoot r, final Optional<File> location) {
        return location.isPresent() && r.isArchive() && isNewLocation(location);
    }

    private boolean isNewLocation(final Optional<File> location) {
        return !cpeInfos.containsKey(location.get());
    }

}

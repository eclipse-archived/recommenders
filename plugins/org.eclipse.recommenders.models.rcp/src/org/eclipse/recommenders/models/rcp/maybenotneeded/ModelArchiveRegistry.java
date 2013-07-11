/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models.rcp.maybenotneeded;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.models.rcp.maybenotneeded.ModelArchiveRegistry.ArchiveDownloadStatus.DOWNLOADING;
import static org.eclipse.recommenders.models.rcp.maybenotneeded.ModelArchiveRegistry.ArchiveDownloadStatus.FAILED;
import static org.eclipse.recommenders.models.rcp.maybenotneeded.ModelArchiveRegistry.ArchiveDownloadStatus.RESOLVED;
import static org.eclipse.recommenders.models.rcp.maybenotneeded.ModelArchiveRegistry.ArchiveDownloadStatus.UNRESOLVED;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.models.ModelArchiveCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Openable;
import org.eclipse.recommenders.utils.gson.GsonUtil;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;

public class ModelArchiveRegistry implements Closeable, Openable {

    private Table<ProjectCoordinate, String/* model classifier */, ModelArchiveStatus> coords;
    private final File store;

    public ModelArchiveRegistry(File store) {
        this.store = store;
    }

    public void set(ProjectCoordinate coord, String type, ModelArchiveStatus summary) {
        coords.put(coord, type, summary);
    }

    public Optional<ModelArchiveStatus> get(ProjectCoordinate coord, String type) {
        return fromNullable(coords.get(coord, type));
    }

    public boolean contains(ProjectCoordinate coord, String type) {
        return get(coord, type).isPresent();
    }

    @Override
    public void open() throws IOException {
        Type type = new TypeToken<Set<ModelArchiveStatus>>() {
        }.getType();
        Set<ModelArchiveStatus> values = GsonUtil.deserialize(store, type);
        for (ModelArchiveStatus s : values) {
            coords.put(s.project, s.modelArchive.getClassifier(), s);
        }
    }

    @Override
    public void close() throws IOException {
        GsonUtil.serialize(coords.values(), store);
    }

    public enum ArchiveDownloadStatus {
        UNRESOLVED, DOWNLOADING, RESOLVED, FAILED
    }

    public static class ModelArchiveStatus {

        public final ProjectCoordinate project;
        public final ModelArchiveCoordinate modelArchive;
        private final String modelType;
        public transient List<String> errors = Lists.newLinkedList();
        public ArchiveDownloadStatus downloadStatus = ArchiveDownloadStatus.UNRESOLVED;

        public ModelArchiveStatus(ProjectCoordinate project, String modelType, ModelArchiveCoordinate modelArchive) {
            this.project = project;
            this.modelType = modelType;
            this.modelArchive = modelArchive;
        }

        public ProjectCoordinate getProject() {
            return project;
        }

        public String getModelType() {
            return modelType;
        }

        public ModelArchiveCoordinate getModelArchive() {
            return modelArchive;
        }

        public ArchiveDownloadStatus getDownloadStatus() {
            return downloadStatus;
        }

        public void setDownloadStatus(ArchiveDownloadStatus downloadStatus) {
            this.downloadStatus = downloadStatus;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public void addError(String error) {
            errors.add(error);
        }

        public boolean unresolved() {
            return compareStatus(UNRESOLVED);
        }

        public boolean downloading() {
            return compareStatus(DOWNLOADING);
        }

        public boolean resolved() {
            return compareStatus(RESOLVED);
        }

        public boolean failed() {
            return compareStatus(FAILED);
        }

        public boolean compareStatus(ArchiveDownloadStatus status) {
            return status == downloadStatus;
        }
    }
}

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
package org.eclipse.recommenders.rcp.analysis;

import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.remove;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.analysis.IDs;
import org.eclipse.recommenders.internal.rcp.analysis.RecommendersProjectLifeCycleService;

public class RecommendersNature implements IProjectNature {
    public static void addNature(final IProject project) {
        try {
            final IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            final int index = ArrayUtils.indexOf(natures, IDs.NATURE_ID);
            if (!hasRecommendersNature(index)) {
                natures = add(natures, IDs.NATURE_ID);
                description.setNatureIds(natures);
                project.setDescription(description, null);
            }
        } catch (final CoreException x) {
            throwUnhandledException(x);
        }
    }

    public static void removeNature(final IProject project) {
        try {
            final IProjectDescription description = project.getDescription();
            String[] natures = description.getNatureIds();
            final int index = ArrayUtils.indexOf(natures, IDs.NATURE_ID);
            if (hasRecommendersNature(index)) {
                natures = remove(natures, index);
                description.setNatureIds(natures);
                project.setDescription(description, null);
            }
        } catch (final CoreException x) {
            throwUnhandledException(x);
        }
    }

    public static boolean hasNature(final IProject project) {
        try {
            final IProjectDescription description = project.getDescription();
            final String[] natures = description.getNatureIds();
            final int index = ArrayUtils.indexOf(natures, IDs.NATURE_ID);
            return hasRecommendersNature(index);
        } catch (final CoreException x) {
            throw throwUnhandledException(x);
        }
    }

    private static boolean hasRecommendersNature(final int indexOf) {
        return indexOf > -1;
    }

    private IProject project;
    @Inject
    private RecommendersProjectLifeCycleService lifeCycleService;

    public RecommendersNature() {
        InjectionService.getInstance().injectMembers(this);
    }

    @Override
    public void configure() throws CoreException {
        if (!natureContainsRecommendersBuilder()) {
            addRecommendersBuilder();
            fireEventNatureAdded();
        }
    }

    private boolean natureContainsRecommendersBuilder() throws CoreException {
        final IProjectDescription desc = project.getDescription();
        final ICommand[] commands = desc.getBuildSpec();
        for (final ICommand command : commands) {
            final String builderName = command.getBuilderName();
            if (builderName.equals(IDs.BUILDER_ID)) {
                return true;
            }
        }
        return false;
    }

    private void addRecommendersBuilder() throws CoreException {
        final IProjectDescription desc = project.getDescription();
        final ICommand[] commands = desc.getBuildSpec();
        final ICommand recommendersBuilderCommand = desc.newCommand();
        recommendersBuilderCommand.setBuilderName(IDs.BUILDER_ID);
        final ICommand[] newCommands = ArrayUtils.add(commands, recommendersBuilderCommand);
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
    }

    @Override
    public void deconfigure() throws CoreException {
        removeRecommendersBuilder();
        fireEventNatureRemoved();
    }

    private void removeRecommendersBuilder() throws CoreException {
        final IProjectDescription description = project.getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(IDs.BUILDER_ID)) {
                commands = ArrayUtils.remove(commands, i);
                description.setBuildSpec(commands);
                project.setDescription(description, null);
                return;
            }
        }
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public void setProject(final IProject project) {
        this.project = project;
    }

    private void fireEventNatureAdded() {
        lifeCycleService.fireOpenEvent(lifeCycleService.toJavaProject(getProject()));
    }

    private void fireEventNatureRemoved() {
        lifeCycleService.fireCloseEvent(lifeCycleService.toJavaProject(getProject()));
    }
}

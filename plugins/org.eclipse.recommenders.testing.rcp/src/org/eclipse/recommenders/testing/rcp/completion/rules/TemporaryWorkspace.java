/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.testing.rcp.completion.rules;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.junit.rules.ExternalResource;

import com.google.common.collect.Lists;

/**
 * <p>Example of usage:</p>
 * <pre>
 * 
 * public static class UsingTemporaryWorkspace {
 * 
 *   &#064;ClassRule
 *   public static TemporaryWorkspace WORKSPACE = new TemporaryWorkspace();
 *
 *   &#064;Test
 *   public void test() throws Exception {
 *     IRecommendersCompletionContext context = WORKSPACE.createProject().createFile(code).triggerContentAssist();
 *     // ...
 *   }
 * }
 * </pre>
 */
public class TemporaryWorkspace extends ExternalResource {

    private List<TemporaryProject> tempProjects;
    private IWorkspace workspace;

    public TemporaryWorkspace() {
    }

    public TemporaryProject createProject() {
        TemporaryProject project = new TemporaryProject(this, getNewRandomName());
        tempProjects.add(project);
        return project;
    }

    IWorkspace getWorkspace() {
        return workspace;
    }

    private String getNewRandomName() {
        String name = RandomStringUtils.randomAlphanumeric(16);

        while (isNameAlreadyUsed(name)) {
            name = RandomStringUtils.randomAlphanumeric(16);
        }

        return name;
    }

    private boolean isNameAlreadyUsed(String name) {
        for (TemporaryProject tempProject : tempProjects) {
            if (tempProject.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void before() throws Throwable {
        this.workspace = ResourcesPlugin.getWorkspace();
        this.tempProjects = Lists.newArrayList();
    }

    @Override
    protected void after() {
        IProject[] projects = workspace.getRoot().getProjects();

        for (IProject project : projects) {
            try {
                project.delete(true, null);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        this.workspace = null;
    }
}

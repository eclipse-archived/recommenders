/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.selection.ui;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.Assert;

@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
public final class PackageExplorerTest extends AbstractUiTest {

    private IJavaElementSelection testCommons(final SWTBotTreeItem item, final Class<?> expectedElement) {
        item.expand().select();
        final IJavaElementSelection selection = getLastSelection();

        if (expectedElement != null) {
            final IJavaElement element = selection.getJavaElement();
            Assert.assertTrue(item + " / " + element.getClass() + " / " + expectedElement, element != null
                    && expectedElement.isAssignableFrom(element.getClass()));
        }

        return selection;
    }

    @Test
    public void testPackageExplorer() {
        final SWTBotTreeItem node = getProjectNode();
        final IJavaElementSelection selection = testCommons(node, JavaProject.class);

        Assert.assertEquals(getProjectName(), ((JavaProject) selection.getJavaElement()).getProject().getName());

        for (final SWTBotTreeItem item : node.getItems()) {
            testFolder(item);
        }
    }

    private void testFolder(final SWTBotTreeItem folder) {
        final Class<?> expectedJavaElement = "src".equals(folder.getText()) ? PackageFragmentRoot.class : null;
        final IJavaElementSelection selection = testCommons(folder, expectedJavaElement);

        // TODO: ...

        for (final SWTBotTreeItem srcPackage : folder.getItems()) {
            testPackage(srcPackage);
        }
    }

    private void testPackage(final SWTBotTreeItem srcPackage) {
        final IJavaElementSelection selection = testCommons(srcPackage, Openable.class);
        // TODO: ...

        for (final SWTBotTreeItem file : srcPackage.getItems()) {
            testFile(file);
        }
    }

    private void testFile(final SWTBotTreeItem file) {
        // final IJavaElementSelection selection = testCommons(file,
        // CompilationUnit.class);
        // TODO: ...

        for (final SWTBotTreeItem javaClass : file.getItems()) {
            testClass(javaClass);
        }
    }

    private void testClass(final SWTBotTreeItem javaClass) {
        // final IJavaElementSelection selection = testCommons(javaClass,
        // SourceType.class);
        // TODO: ...

        for (final SWTBotTreeItem classElement : javaClass.getItems()) {
            testClassElement(classElement);
        }
    }

    private void testClassElement(final SWTBotTreeItem javaClass) {
        final IJavaElementSelection selection = testCommons(javaClass, NamedMember.class);
        // TODO: ...
    }

}

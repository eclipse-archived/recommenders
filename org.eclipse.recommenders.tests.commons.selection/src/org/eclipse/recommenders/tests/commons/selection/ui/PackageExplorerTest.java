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

import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.Assert;

@SuppressWarnings({ "restriction", "rawtypes" })
@RunWith(SWTBotJunit4ClassRunner.class)
public final class PackageExplorerTest extends AbstractUiTest {

    private static TestSelectionListener observer;

    @BeforeClass
    public static void initialize() {
        observer = getListener();
    }

    private IJavaElementSelection testCommons(final SWTBotTreeItem item, final Class expectedElement) {
        item.expand();
        item.select();
        final IJavaElementSelection context = observer.getLastContext();

        Assert.assertNull(context.getInvocationContext());

        if (expectedElement != null) {
            Assert.assertTrue(expectedElement.isInstance(context.getJavaElement()));
        }

        return context;
    }

    @Test
    public void testPackageExplorer() {
        final SWTBotTreeItem node = getProjectNode();
        final IJavaElementSelection context = testCommons(node, JavaProject.class);

        Assert.assertEquals(getProjectName(), ((JavaProject) context.getJavaElement()).getProject().getName());

        for (final SWTBotTreeItem item : node.getItems()) {
            testFolder(item);
        }
    }

    private void testFolder(final SWTBotTreeItem folder) {
        final Class expectedJavaElement = "src".equals(folder.getText()) ? PackageFragmentRoot.class : null;
        final IJavaElementSelection context = testCommons(folder, expectedJavaElement);

        if (context.getJavaElement() != null) {
            Assert.assertEquals("src", context.getJavaElement().getElementName());

            for (final SWTBotTreeItem srcPackage : folder.getItems()) {
                testPackage(srcPackage);
            }
        }
    }

    private void testPackage(final SWTBotTreeItem srcPackage) {
        final IJavaElementSelection context = testCommons(srcPackage, PackageFragment.class);
        // TODO ...

        for (final SWTBotTreeItem file : srcPackage.getItems()) {
            testFile(file);
        }
    }

    private void testFile(final SWTBotTreeItem file) {
        final IJavaElementSelection context = testCommons(file, CompilationUnit.class);
        // TODO ...

        for (final SWTBotTreeItem javaClass : file.getItems()) {
            testClass(javaClass);
        }
    }

    private void testClass(final SWTBotTreeItem javaClass) {
        final IJavaElementSelection context = testCommons(javaClass, SourceType.class);
        // TODO ...

        for (final SWTBotTreeItem classElement : javaClass.getItems()) {
            testClassElement(classElement);
        }
    }

    private void testClassElement(final SWTBotTreeItem javaClass) {
        final IJavaElementSelection context = testCommons(javaClass, NamedMember.class);
        // TODO ...
    }

}

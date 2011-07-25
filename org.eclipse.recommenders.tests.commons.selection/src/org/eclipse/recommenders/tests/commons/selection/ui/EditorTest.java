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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.recommenders.commons.internal.selection.SelectionPlugin;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import junit.framework.Assert;

@SuppressWarnings({ "restriction", "rawtypes" })
@RunWith(SWTBotJunit4ClassRunner.class)
public final class EditorTest extends AbstractUiTest {

    private static final Map<String, Class> TYPES = new HashMap<String, Class>();
    static {
        TYPES.put("LocalVariable", LocalVariable.class);
        TYPES.put("PackageFragment", PackageFragment.class);
        TYPES.put("ResolvedBinaryType", ResolvedBinaryType.class);
        TYPES.put("ResolvedBinaryMethod", ResolvedBinaryMethod.class);
        TYPES.put("ResolvedSourceField", ResolvedSourceField.class);
        TYPES.put("SourceField", SourceField.class);
        TYPES.put("SourceMethod", SourceMethod.class);
        TYPES.put("SourceType", SourceType.class);
    }

    @Test
    public void testEditor() throws BadLocationException {
        for (final SWTBotTreeItem srcPackage : getSourceNode().getItems()) {
            srcPackage.expand();
            for (final SWTBotTreeItem javaFile : srcPackage.getItems()) {
                javaFile.doubleClick();
                testJavaFile(bot.editorByTitle(javaFile.getText()).toTextEditor());
            }
        }
    }

    private void testJavaFile(final SWTBotEclipseEditor editor) throws BadLocationException {
        findAndTestAnnotations(editor);
    }

    private void findAndTestAnnotations(final SWTBotEclipseEditor editor) throws BadLocationException {
        final Pattern pattern = Pattern.compile("/\\* (.*?) \\*/");

        int offset = 0;
        int assertions = 0;
        for (int line = 0; line < editor.getLineCount(); ++line) {
            final String lineText = editor.getLines().get(line);
            final Matcher matcher = pattern.matcher(lineText);
            while (matcher.find()) {
                testAnnotation(matcher.group(), offset, lineText);
                ++assertions;
            }
            offset += lineText.length() + 2;
        }
        Assert.assertTrue(assertions + "", assertions > 8);
    }

    private void testAnnotation(final String annotation, final int offset, final String lineText)
            throws BadLocationException {
        final String[] parts = annotation.substring(3, annotation.length() - 3).split(" \\| ");

        SelectionPlugin.triggerUpdate(new TextSelection(offset + lineText.indexOf(annotation), 0));
        final IJavaElementSelection selection = getLastSelection();
        final IJavaElement javaElement = selection.getJavaElement();
        final JavaElementLocation location = selection.getElementLocation();

        final Class expectedType = TYPES.get(parts[0]);
        final String expectedLocation = parts[1];
        final String expectedAstParent = parts[2];

        Assert.assertEquals(annotation, expectedType, javaElement == null ? null : javaElement.getClass());
        Assert.assertEquals(annotation, expectedLocation, location.getDisplayName());
        Assert.assertEquals(annotation, expectedAstParent,
                ASTNode.nodeClassForType(selection.getAstNode().getParent().getNodeType()).getSimpleName());
        Assert.assertTrue(!("Type Declaration".equals(expectedLocation)
                || "Extends Declaration".equals(expectedLocation) || "Implements Declaration".equals(expectedLocation))
                || JavaElementLocation.isInTypeDeclaration(location));
        Assert.assertNotNull(selection.getCompilationUnit());

        // TODO ...
    }
}

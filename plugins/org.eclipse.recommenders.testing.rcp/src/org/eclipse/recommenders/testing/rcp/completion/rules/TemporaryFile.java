/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.testing.rcp.completion.rules;

import static java.io.File.separator;
import static org.eclipse.recommenders.testing.jdt.AstUtils.MARKER;
import static org.eclipse.recommenders.testing.rcp.completion.rules.TemporaryProject.SRC_FOLDER_NAME;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Pair.newPair;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.RecommendersCompletionContext;
import org.eclipse.recommenders.internal.rcp.CachingAstProvider;
import org.eclipse.recommenders.testing.rcp.jdt.JavaContentAssistContextMock;
import org.eclipse.recommenders.utils.Constants;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class TemporaryFile {

    private static final String JAVA_IDENTIFIER_REGEX = "([a-zA-Z_$\\p{Lu}\\p{Ll}]{1}"
            + "[a-zA-Z_$0-9\\p{Lu}\\p{Ll}\\p{Nl}]*)";

    private final ICompilationUnit cu;
    private final Set<Integer> markers;
    private final TemporaryProject tempProject;

    TemporaryFile(TemporaryProject parentProject, CharSequence code) throws CoreException {
        this.tempProject = parentProject;

        Pair<ICompilationUnit, Set<Integer>> struct = createFileAndParseWithMarkers(code);
        this.cu = struct.getFirst();
        this.markers = struct.getSecond();
    }

    public CompilationUnit getAst() {
        return SharedASTProvider.getAST(cu, SharedASTProvider.WAIT_YES, null);
    }

    public IRecommendersCompletionContext triggerContentAssist() throws JavaModelException {
        JavaContentAssistInvocationContext javaContext = new JavaContentAssistContextMock(cu,
                markers.iterator().next());
        return new RecommendersCompletionContext(javaContext, new CachingAstProvider());
    }

    private Pair<ICompilationUnit, Set<Integer>> createFileAndParseWithMarkers(final CharSequence contentWithMarkers)
            throws CoreException {
        final Pair<String, Set<Integer>> content = findMarkers(contentWithMarkers, MARKER);

        final ICompilationUnit cu = createFile(content.getFirst());
        tempProject.refreshAndBuildProject();

        return Pair.newPair(cu, content.getSecond());
    }

    private Pair<String, Set<Integer>> findMarkers(final CharSequence content, String marker) {
        final Set<Integer> markers = Sets.newTreeSet();
        int pos = 0;
        final StringBuilder sb = new StringBuilder(content);
        while ((pos = sb.indexOf(marker, pos)) != -1) {
            sb.deleteCharAt(pos);
            markers.add(pos);
            ensureIsTrue(pos <= sb.length());
            pos--;
        }
        return newPair(sb.toString(), markers);
    }

    private ICompilationUnit createFile(final String content) throws CoreException {
        // get filename
        final String fileName = findClassName(content) + Constants.DOT_JAVA;

        // add the file name and get the file
        StringBuilder projectRelativeFilePath = new StringBuilder();
        projectRelativeFilePath.append(SRC_FOLDER_NAME);
        projectRelativeFilePath.append(separator);

        projectRelativeFilePath.append(fileName);
        final IPath projectRelativePath = new Path(projectRelativeFilePath.toString());

        final IFile file = tempProject.getProject().getFile(projectRelativePath);

        // delete file
        if (file.exists()) {
            file.delete(true, null);
        }

        // create file
        final ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
        file.create(is, true, null);
        int attempts = 0;
        while (!file.exists()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempts++;
            if (attempts > 10) {
                throw new IllegalStateException("Failed to create file");
            }
        }

        IJavaProject javaProject = tempProject.getJavaProject();
        if (javaProject == null) {
            throw new IllegalStateException("The temporaryProject does not yet have an associated IJavaProject.");
        }

        // get the compilation unit
        Path srcRelativePath = new Path(fileName);
        ICompilationUnit cu = (ICompilationUnit) javaProject.findElement(srcRelativePath);
        while (cu == null) {
            cu = (ICompilationUnit) javaProject.findElement(srcRelativePath);
        }
        return cu;
    }

    public void openFileInEditor() throws CoreException {
        JavaUI.openInEditor(cu);
    }

    private static String findClassName(final CharSequence source) {
        Pattern p = Pattern.compile(".*?class\\s+" + JAVA_IDENTIFIER_REGEX + ".*", Pattern.DOTALL);
        Matcher matcher = p.matcher(source);
        if (!matcher.matches()) {
            p = Pattern.compile(".*interface\\s+" + JAVA_IDENTIFIER_REGEX + ".*", Pattern.DOTALL);
            matcher = p.matcher(source);
        }
        assertTrue(matcher.matches());
        return matcher.group(1);
    }
}

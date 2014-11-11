/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

/**
 * Constant definitions for plug-in preferences, IDs and other constants.
 * <p>
 * Note that UI strings should go into a messages files rather than into this file.
 */
public final class Constants {

    private Constants() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static final String BUNDLE_ID = "org.eclipse.recommenders.snipmatch.rcp"; //$NON-NLS-1$
    public static final String EDITOR_ID = "org.eclipse.recommenders.snipmatch.rcp.editors.snippet"; //$NON-NLS-1$

    public static final String HELP_URL = "http://www.eclipse.org/recommenders/manual/#snippet-editing-sharing";

    public static final String PREF_SEARCH_RESULTS_BACKGROUND = "org.eclipse.recommenders.snipmatch.rcp.searchResultBackgroundColor"; //$NON-NLS-1$
    public static final String PREF_SEARCH_BOX_BACKGROUND = "org.eclipse.recommenders.snipmatch.rcp.searchboxbackground"; //$NON-NLS-1$
    public static final String PREF_DISABLED_REPOSITORY_CONFIGURATIONS = "org.eclipse.recommenders.snipmatch.rcp.disabledrepositoryconfigurations"; //$NON-NLS-1$

    public static final String PREF_SNIPPET_EDITOR_DISCOVERY = "org.eclipse.recommenders.snipmatch.rcp.editor.discovery"; //$NON-NLS-1$

    public static final String SNIPMATCH_CONTEXT_ID = "Snipmatch-Java-Context"; //$NON-NLS-1$

    public static final String EXT_POINT_PAGE_FACTORIES = "org.eclipse.recommenders.snipmatch.rcp.pageFactories"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_CONFIGURATIONS = "org.eclipse.recommenders.snipmatch.rcp.defaultConfigurations"; //$NON-NLS-1$

    public static final String WIZBAN_ADD_REPOSITORY = "icons/wizban/add_repository.png"; //$NON-NLS-1$
    public static final String WIZBAN_ADD_GIT_REPOSITORY = "icons/wizban/add_git_repository.png"; //$NON-NLS-1$
    public static final String WIZBAN_EDIT_REPOSITORY = "icons/wizban/edit_repository.png"; //$NON-NLS-1$
    public static final String WIZBAN_EDIT_GIT_REPOSITORY = "icons/wizban/edit_git_repository.png"; //$NON-NLS-1$

    public static final String EXT_POINT_REGISTERED_EMF_PACKAGE = "org.eclipse.recommenders.snipmatch.rcp.registeredEmfPackages"; //$NON-NLS-1$
    public static final String EXT_POINT_REGISTERED_EMF_PACKAGE_URI = "uri"; //$NON-NLS-1$

    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS = "org.eclipse.recommenders.snipmatch.rcp.defaultGitSnippetRepositoryConfigurations"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_ID = "id"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_NAME = "name"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_PUSH_URL = "pushUrl"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_PUSH_BRANCH_PREFIX = "pushBranchPrefix"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_URL = "url"; //$NON-NLS-1$
    public static final String EXT_POINT_DEFAULT_GIT_SNIPPET_REPOSITORY_CONFIGURATIONS_DEFAULT_PRIORITY = "defaultPriority"; //$NON-NLS-1$
}

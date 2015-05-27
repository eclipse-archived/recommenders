/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public final class SelectRepositoryDialog {

    public static Optional<ISnippetRepository> openSelectRepositoryDialog(final Shell shell, Repositories repos,
            SnippetRepositoryConfigurations configs) {
        return openSelectRepositoryDialog(shell, repos, configs, null);
    }

    public static Optional<ISnippetRepository> openSelectRepositoryDialog(final Shell shell, Repositories repos,
            SnippetRepositoryConfigurations configs, SnippetRepositoryConfiguration preSelectedConfiguration) {

        List<SnippetRepositoryConfiguration> filteredConfigurations = filterImportSupportingConfigurations(repos,
                configs);
        if (filteredConfigurations.isEmpty()) {
            return absent();
        }

        ListDialog selectRepositoryDialog = createSelectRepositoryDialog(shell, filteredConfigurations,
                preSelectedConfiguration, repos);

        int status = selectRepositoryDialog.open();
        if (status == Status.OK) {
            SnippetRepositoryConfiguration config = cast(selectRepositoryDialog.getResult()[0]);
            return repos.getRepository(config.getId());
        }
        return absent();
    }

    private static ListDialog createSelectRepositoryDialog(final Shell shell,
            List<SnippetRepositoryConfiguration> configurations,
            SnippetRepositoryConfiguration preSelectedConfiguration, Repositories repos) {

        ListDialog selectRepositoryDialog = new ListDialog(shell);
        selectRepositoryDialog.setTitle(Messages.SELECT_REPOSITORY_DIALOG_TITLE);
        selectRepositoryDialog.setMessage(Messages.SELECT_REPOSITORY_DIALOG_MESSAGE);
        selectRepositoryDialog.setContentProvider(new ArrayContentProvider());
        selectRepositoryDialog.setInput(configurations);

        SnippetRepositoryConfiguration selectedElement = null;
        if (preSelectedConfiguration != null && isImportSupported(preSelectedConfiguration, repos)) {
            selectedElement = preSelectedConfiguration;
        } else if (!configurations.isEmpty()) {
            selectedElement = configurations.get(0);
        }

        if (selectedElement != null) {
            selectRepositoryDialog.setInitialSelections(new SnippetRepositoryConfiguration[] { selectedElement });
        }

        selectRepositoryDialog.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                SnippetRepositoryConfiguration config = cast(element);
                return config.getName();
            }
        });
        return selectRepositoryDialog;
    }

    private static boolean isImportSupported(SnippetRepositoryConfiguration configuration, Repositories repos) {
        ISnippetRepository repo = repos.getRepository(configuration.getId()).orNull();
        return repo != null && repo.isImportSupported();
    }

    private static List<SnippetRepositoryConfiguration> filterImportSupportingConfigurations(Repositories repos,
            SnippetRepositoryConfigurations configs) {

        List<SnippetRepositoryConfiguration> filteredConfigurations = Lists.newArrayList();
        for (SnippetRepositoryConfiguration config : configs.getRepos()) {
            if (isImportSupported(config, repos)) {
                filteredConfigurations.add(config);
            }
        }

        return filteredConfigurations;
    }

}

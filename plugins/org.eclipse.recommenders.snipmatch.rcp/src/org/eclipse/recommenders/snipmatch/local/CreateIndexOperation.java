/**
 * Copyright (c) 2012 Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Cheng Chen - initial API and implementation and/or initial documentation
*/

package org.eclipse.recommenders.snipmatch.local;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.SummaryFileMap;
import org.eclipse.recommenders.snipmatch.preferences.PreferenceConstants;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.recommenders.utils.gson.GsonUtil;

public class CreateIndexOperation implements IRunnableWithProgress {
    private String indexFilePath = SnipMatchPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
    private String dirPath = SnipMatchPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.SNIPPETS_STORE_DIR);
    private int indexNumber = 0;
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("Updating index file ...", 3);
        LinkedList<File> list = new LinkedList<File>();
        List<SummaryFileMap> mapList = new ArrayList<SummaryFileMap>();
        File dir = new File(dirPath);
        list.add(dir);
        File file[] = null;
        File tmp = null;
        monitor.worked(1);
        while (!list.isEmpty()) {
            tmp = list.removeFirst();
            if (tmp.isDirectory()) {
                file = tmp.listFiles();
                if (file == null)
                    continue;
                for (int i = 0; i < file.length; i++)
                    list.add(file[i]);
            } else if (tmp.getAbsolutePath().endsWith(".json")) {
                File jsonFile = new File(tmp.getAbsolutePath());
                if (jsonFile.exists()) {
                    Effect parent = GsonUtil.deserialize(jsonFile, Effect.class);
                    String summary = parent.getSummary();
                    for (String pattern : parent.getPatterns())
                        summary += ";" + pattern;
                    SummaryFileMap map = new SummaryFileMap(summary, tmp.getAbsolutePath());
                    mapList.add(map);
                }
            }
        }
        monitor.worked(1);
        GsonUtil.serialize(mapList, new File(indexFilePath));
        indexNumber = mapList.size();
        monitor.worked(1);
        monitor.done();
    }
    public int getIndexNumber() {
        return indexNumber;
    }
}

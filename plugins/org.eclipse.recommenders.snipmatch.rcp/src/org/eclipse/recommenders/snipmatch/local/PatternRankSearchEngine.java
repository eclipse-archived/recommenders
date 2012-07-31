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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.recommenders.snipmatch.core.ArgumentMatchNode;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.EffectMatchNode;
import org.eclipse.recommenders.snipmatch.core.EffectParameter;
import org.eclipse.recommenders.snipmatch.core.MatchNode;
import org.eclipse.recommenders.snipmatch.core.SummaryFileMap;
import org.eclipse.recommenders.snipmatch.preferences.PreferenceConstants;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.recommenders.snipmatch.search.SnipMatchSearchEngine;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.gson.reflect.TypeToken;

public class PatternRankSearchEngine implements SnipMatchSearchEngine {
    private boolean initialized = false;
    private static SnipMatchSearchEngine instance = null;

    private String snippetsDir;
    private String indexFilePath;
    private String commonSnippetsDir = "common";
    private String anonymouseSnippetsDir = "local";
    private List<SummaryFileMap> sfMapList = null;
    private Map<String, String> values = new HashMap<String, String>();

    public static SnipMatchSearchEngine getInstance() {
        if (instance == null) {
            synchronized (SnipMatchSearchEngine.class) {
                SnipMatchSearchEngine inst = instance;
                if (inst == null) {
                    synchronized (SnipMatchSearchEngine.class) {
                        inst = new PatternRankSearchEngine();
                    }
                    instance = inst;
                }
            }
        }
        return instance;
    }

    private PatternRankSearchEngine() {
    }

    @Override
    public boolean isInitialized(String currentSnippetsDir, String currentIndexDir) {

        return initialized && this.snippetsDir.equals(currentSnippetsDir) && currentIndexDir.equals(this.indexFilePath);
    }

    @Override
    public void initialize(String snippetsDir, String indexDir) throws IOException {
        File cDirFile = new File(snippetsDir, this.commonSnippetsDir);
        if (!(cDirFile.exists()) && !(cDirFile.isDirectory()))
            cDirFile.mkdirs();

        File aDirFile = new File(snippetsDir, this.anonymouseSnippetsDir);
        if (!(aDirFile.exists()) && !(aDirFile.isDirectory()))
            aDirFile.mkdirs();

        String indexFilePath = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
        File indexFile = new File(indexFilePath);
        if (!indexFile.exists()) {
            indexFile.createNewFile();
            // updateIndex();
        }

        loadIndexFile(indexFile);
        initialized = true;
        this.snippetsDir = snippetsDir;
        this.indexFilePath = indexFilePath;
    }

    private void loadIndexFile(File indexFile) {
        Type listType = new TypeToken<List<SummaryFileMap>>() {
        }.getType();
        sfMapList = GsonUtil.deserialize(indexFile, listType);
    }

    @Override
    public List<MatchNode> search(String query) {
        List<MatchNode> result = new ArrayList<MatchNode>();
        List<PatternRankResult> rankResult = new ArrayList<PatternRankResult>();
        if (query.trim().equals(""))
            return result;
        if (sfMapList != null && sfMapList.size() > 0)
            for (SummaryFileMap map : sfMapList) {
                String summary = map.summary;
                rankQuery(summary.toLowerCase(), query.toLowerCase(), map.filePath, rankResult);
            }
            sortRankResult(rankResult);
            for(int k=0; k<Math.min(15, rankResult.size()); k++){
                File jsonFile = new File(rankResult.get(k).getFilePath());
                if (jsonFile.exists()) {
                    Effect parent = GsonUtil.deserialize(jsonFile, Effect.class);
                    parent.setId(System.currentTimeMillis() + String.valueOf(Math.random()).substring(5));

                    MatchNode[] children = new MatchNode[parent.getParameters().length];
                    this.values.clear();
                    if(rankResult.get(k).isInOrder()){
                        parseParameterValues(rankResult.get(k).getPattern(), query);
                    }
                    for (int i = 0; i < children.length; i++) {
                        EffectParameter param = parent.getParameters()[i];
                        String keyName = param.getName().toLowerCase();
                        if(values.get(keyName) != null)
                            param.setValue(values.get(keyName));
                        ArgumentMatchNode childNode = new ArgumentMatchNode(param.getName(), param);
                        children[i] = childNode;
                    }

                    result.add(new EffectMatchNode(parent, rankResult.get(k).getPattern(), children));
                }
            }
        return result;
    }
    
    private void parseParameterValues(String pattern, String query){
        String[] ps = pattern.split("\\s+");
        String[] qs = query.split("\\s+");
        for(int i=0; i<Math.min(ps.length, qs.length); i++){
            if(ps[i].startsWith("$")){
                this.values.put(ps[i].substring(1), qs[i]);
            }
        }
    }
    
    private void sortRankResult(List<PatternRankResult> rankResult){
        for(int i=0; i<rankResult.size(); i++)
            for(int j=i+1; j<rankResult.size(); j++){
                PatternRankResult ri = rankResult.get(i);
                PatternRankResult rj = rankResult.get(j);
                if(inBetterMatch(ri.isInOrder(), ri.getMissParam(), ri.getRankNumber(), rj.isInOrder(), rj.getMissParam(), rj.getRankNumber())){
                    PatternRankResult temp = ri;
                    rankResult.set(i, rj);
                    rankResult.set(j, temp);
                }
            }
    }
    
    private void rankQuery(String summary, String query, String file, List<PatternRankResult> rankResult){
        String[] patterns = summary.split(";");
        if(patterns.length > 1){
            boolean maxInOrder = false;
            int minMissParam = Integer.MAX_VALUE;
            int maxRankNumber = Integer.MIN_VALUE;
            String matchPattern = "";
            for(int t=1; t<patterns.length; t++){
                String pattern = patterns[t];
                boolean inOrder = false;
                int missParam = 0;
                int rankNumber = 0;
                String[] patternWords = pattern.split("\\s+");
                String[] queryWords = query.split("\\s+");
                int i;
                for(i=0; i<Math.min(patternWords.length, queryWords.length); i++){
                    if(!patternWords[i].equals(queryWords[i])){
                        if(!patternWords[i].startsWith("$")){
                            inOrder = false;
                            break;
                        }
                    }else
                        inOrder = true;
                }
                if(inOrder)
                    rankNumber = i;
                if(inOrder && patternWords.length > i){
                    for(; i<patternWords.length; i++)
                        if(!patternWords[i].startsWith("$")){
                            continue;
                        }else{
                            missParam++;
                        }
                }
                //Rank match string number in _not-in-order_ condition
                if(!inOrder){
                    missParam = 0;
                    rankNumber = 0;
                    for(String word : queryWords){
                        for(String pword : patternWords)
                            if(word.equals(pword)){
                                rankNumber++;
                                break;
                            }
                    }
                }
                
                //Special situation when user is inputing characters of the first query word
                if(queryWords.length == 1){
                    if(!inOrder && rankNumber == 0){
                        if(pattern.startsWith(query))
                            rankNumber = 2;
                        else if(startsWith(patternWords, query))
                            rankNumber = 1;
                    }else if(rankNumber > 0)
                        rankNumber++;
                }
                
                if(inBetterMatch(maxInOrder, minMissParam, maxRankNumber, inOrder, missParam, rankNumber)){
                    maxInOrder = inOrder;
                    minMissParam = missParam;
                    maxRankNumber = rankNumber;
                    matchPattern = pattern;
                }
                
                //Already the best match result, break
                if(maxInOrder && minMissParam == 0)
                    break;
            }
            if(maxInOrder || maxRankNumber>0){
                rankResult.add(new PatternRankResult(maxInOrder, minMissParam, maxRankNumber, file, matchPattern));
            }
        }
    }
    
    private boolean startsWith(String[] patterns, String query){
        for(String pattern : patterns){
            if(pattern.startsWith(query)) return true;
        }
        return false;
    }

    private boolean inBetterMatch(boolean maxInOrder, int minMissParam, int maxRankNumber,boolean inOrder, int missParam, int rankNumber){
        if(inOrder == maxInOrder){
            if(inOrder){
                if(missParam < minMissParam)
                    return true;
                else if(missParam == minMissParam){
                    if(rankNumber > maxRankNumber)
                        return true;
                }
            }else{
                //Not in order
                if(rankNumber > maxRankNumber)
                    return true;
                else if(rankNumber == maxRankNumber){
                    if(missParam < minMissParam)
                        return true;
                }
            }
        }else{
            if(inOrder)
                return true;
        }
        return false;
    }
    
    @Override
    public void updateIndex() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            Shell shell = new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.NO_TRIM
                    | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
            try {
                new ProgressMonitorDialog(shell).run(true, true, new CreateIndexOperation());
                initialized = false;
                MessageDialog.openInformation(shell, "Recommenders tips", "Update search engine index file success!");
            } catch (Exception e) {
                e.printStackTrace();
                MessageDialog.openError(shell, "Recommenders tips",
                        "Fail to upate index, check your snippet store directory!");
            }
        }
    }

}

class PatternRankResult{
    private boolean inOrder;
    private int rankNumber;
    private int missParam;
    private String filePath;
    private String pattern;
    
    public PatternRankResult(boolean maxInOrder, int minMissParam, int maxRankNumber, String file, String patt){
        inOrder = maxInOrder;
        rankNumber = maxRankNumber;
        missParam = minMissParam;
        filePath = file;
        pattern = patt;
    }
    
    public boolean isInOrder() {
        return inOrder;
    }
    public void setInOrder(boolean inOrder) {
        this.inOrder = inOrder;
    }
    public int getRankNumber() {
        return rankNumber;
    }
    public void setRankNumber(int rankNumber) {
        this.rankNumber = rankNumber;
    }
    public int getMissParam() {
        return missParam;
    }

    public void setMissParam(int missParam) {
        this.missParam = missParam;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
}
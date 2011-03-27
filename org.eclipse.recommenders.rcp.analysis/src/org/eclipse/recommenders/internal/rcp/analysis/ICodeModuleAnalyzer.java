package org.eclipse.recommenders.internal.rcp.analysis;

import java.io.File;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.CodeModuleDescriptor;

public interface ICodeModuleAnalyzer {

    public void analyze(File moduleToAnalyze, CodeModuleDescriptor moduleDescriptorToComplete);

}

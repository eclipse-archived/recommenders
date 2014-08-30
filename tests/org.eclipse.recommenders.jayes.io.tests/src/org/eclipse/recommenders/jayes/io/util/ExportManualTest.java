/*******************************************************************************
 * Copyright (c) 2015 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.jayes.io.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.recommenders.internal.jayes.io.util.XMLUtil;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.io.xdsl.XDSLWriter;

public class ExportManualTest {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        ModelLoader loader = new ModelLoader("jre:jr1:1.7");
        for (BayesNet bayesNet : loader.getNetworks()) {
            XDSLWriter writer = new XDSLWriter(new FileOutputStream(new File("out/"
                    + XMLUtil.escape(bayesNet.getNode(2).getName()) + ".xdsl")));
            writer.write(bayesNet);
            writer.close();
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.commons.bayesnet;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.recommenders.internal.jayes.io.util.BayesNetConverter;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.io.IBayesNetReader;

public class CommonsReader implements IBayesNetReader {

    private InputStream in;
    BayesNetConverter conv = new BayesNetConverter();

    public CommonsReader(InputStream in) throws IOException {
        this.in = in;

    }

    @Override
    public void close() throws IOException {
        in.close();

    }

    @SuppressWarnings("deprecation")
    @Override
    public BayesNet read() throws IOException {
        try {
            return conv.transform(BayesianNetwork.read(in));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}

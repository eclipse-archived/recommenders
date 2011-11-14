/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.internal.rcp.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketThread extends Thread {
    private Socket socket = null;

    public SocketThread(final Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            final String outputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
            }
            out.close();
            in.close();
            socket.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
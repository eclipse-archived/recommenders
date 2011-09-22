/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.commons.logging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.ui.IMemento;

public class SimpleSocketServer implements Runnable {

    private static final String TAG_SERVER = "lbserver"; //$NON-NLS-1$
    private static final String TAG_PORT = "port"; //$NON-NLS-1$

    private int port = 4444;
    private ServerSocket serverSocket;
    private boolean stop = false;

    public SimpleSocketServer() {
    }

    @Override
    public void run() {
        startAndLoop();
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void restart() {
        try {
            serverSocket.close();
            stop = false;
            startAndLoop();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void startAndLoop() {
        stop = false;
        try {
            serverSocket = new ServerSocket(port);
            while (!stop) {
                final Socket socket = serverSocket.accept();
                new Thread(new SocketNode(socket)).start();
            }
            serverSocket.close();
        } catch (final java.net.BindException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void saveState(final IMemento memento) {
        final IMemento mem = memento.createChild(TAG_SERVER);
        mem.putInteger(TAG_PORT, port);
    }

    public void init(final IMemento memento) {
        final IMemento mem = memento.getChild(TAG_SERVER);
        if (mem == null) {
            return;
        }

        final int newPort = mem.getInteger(TAG_PORT);

        if (newPort != 0) {
            this.port = newPort;
        }
    }

    public static boolean isPortFree(final Integer port) {
        try {
            final Socket s = new Socket("localhost", port); //$NON-NLS-1$
            return !s.isConnected();
        } catch (final Exception e) {
            return true;
        }
    }

    public void stop() {
        stop = true;
    }

}

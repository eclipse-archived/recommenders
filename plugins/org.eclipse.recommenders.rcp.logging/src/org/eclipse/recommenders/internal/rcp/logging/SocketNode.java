/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.logging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.eclipse.recommenders.internal.rcp.logging.model.LoggingEventManager;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class SocketNode implements Runnable {

    Socket socket;
    ObjectInputStream ois;
    boolean oisInError = false;

    public SocketNode(final Socket socket) {
        this.socket = socket;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (final Exception e) {
            oisInError = true;
        }
    }

    @Override
    public void run() {
        if (oisInError) {
            return;
        }

        ILoggingEvent event;

        try {
            while (true) {
                // read an event from the wire
                event = (ILoggingEvent) ois.readObject();
                // trick to keep the original thread name
                event.getThreadName();
                // add it to the manager's LoggingEvent list
                try {
                    LoggingEventManager.getManager().addLoggingEvent(event);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (final ClassNotFoundException e) {
            printVersionError(e);
        } catch (final InvalidClassException e) {
            printVersionError(e);
        } catch (final java.io.EOFException e) {
            e.printStackTrace();
        } catch (final java.net.SocketException e) {
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            ois.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void printVersionError(final Exception e) {
        e.printStackTrace();
    }

}

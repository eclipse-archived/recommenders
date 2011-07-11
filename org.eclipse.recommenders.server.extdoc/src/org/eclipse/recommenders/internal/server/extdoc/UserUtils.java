/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UserUtils {

    final static String getUserMacAddress() {
        try {
            final InetAddress ip = InetAddress.getLocalHost();
            final NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                return null;
            }

            final byte[] mac = network.getHardwareAddress();

            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], i < mac.length - 1 ? "-" : ""));
            }
            return sb.toString();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException(e);
        } catch (final SocketException e) {
            throw new IllegalStateException(e);
        }
    }

}

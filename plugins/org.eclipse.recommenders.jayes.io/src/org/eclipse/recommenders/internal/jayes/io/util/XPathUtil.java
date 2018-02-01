/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.internal.jayes.io.util;

import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XPathUtil {

    private XPathUtil() {
        // Not meant to be instantiated
    }

    public static Iterator<Node> evalXPath(XPath xpath, String expression, Node context) {

        final NodeList result;
        try {
            result = (NodeList) xpath.evaluate(expression, context, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(expression);
        }

        return new Iterator<Node>() {

            int next;

            @Override
            public boolean hasNext() {
                return next < result.getLength();
            }

            @Override
            public Node next() {
                return result.item(next++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

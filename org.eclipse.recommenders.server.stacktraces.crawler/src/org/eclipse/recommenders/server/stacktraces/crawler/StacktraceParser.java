/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.stacktraces.crawler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StacktraceParser {

    public static Stacktrace parse(final String text) throws ParseException {
        final List<Stacktrace> result = parseAll(text);
        if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new ParseException("Can't match exactly one stacktrace in given String. Matches: " + result.size()
                    + " stacktraces.");
        }
    }

    public static List<Stacktrace> parseAll(final String text) throws ParseException {
        final LinkedList<Stacktrace> result = new LinkedList<Stacktrace>();
        final Matcher stacktraceMatcher = PatternConfiguration.stacktracePattern.matcher(preprocessString(text));
        while (stacktraceMatcher.find()) {
            final Stacktrace stacktrace = createStacktrace(stacktraceMatcher);
            if (stacktrace != null) {
                result.add(stacktrace);
            }
        }

        return result;
    }

    private static Stacktrace createStacktrace(final Matcher stacktraceMatcher) throws ParseException {
        final ArrayList<StackFrame> frames = createStackFrames(stacktraceMatcher.group(5));

        if (frames.size() > 0) {
            return new Stacktrace(stacktraceMatcher.group(1), stacktraceMatcher.group(4), frames);
        } else {
            return null;
        }
    }

    private static ArrayList<StackFrame> createStackFrames(final String framesGroup) throws ParseException {
        final ArrayList<StackFrame> frames = new ArrayList<StackFrame>();
        int frameIndex = 0;
        final Matcher frameMatcher = PatternConfiguration.framePattern.matcher(framesGroup);
        while (frameMatcher.find()) {
            final String className = frameMatcher.group(5);
            if (className == null) { // "Caused by:" match
                frameIndex = 0;
                continue;
            }

            frames.add(frameIndex, createStackFrame(frameMatcher));
            frameIndex++;
        }
        return frames;
    }

    private static StackFrame createStackFrame(final Matcher frameMatcher) throws ParseException {
        final String className = frameMatcher.group(5);
        final String method = frameMatcher.group(8);
        String source = null;
        String sourceFile = null;
        int lineNumber = 0;
        if (frameMatcher.group(11) == null || frameMatcher.group(12) == null) {
            source = frameMatcher.group(9);
        } else {
            sourceFile = frameMatcher.group(11);
            try {
                lineNumber = Integer.parseInt(frameMatcher.group(12));
            } catch (final NumberFormatException e) {
                throw new ParseException("Error while parsing line number: " + frameMatcher.group(12), e);
            }
        }
        return new StackFrame(className, method, source, sourceFile, lineNumber);
    }

    private static String preprocessString(final String text) {
        final String[] lines = text.split("\\n");
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            builder.append(" ");
            builder.append(lines[i]);
        }
        return builder.toString();
    }

    public static class ParseException extends Exception {
        public ParseException(final String message) {
            super(message);
        }

        public ParseException(final String message, final Throwable e) {
            super(message, e);
        }
    }

    public static class PatternConfiguration {
        public static final String clazz = "(\\w(?:\\$+|\\.)?)+";
        public static final String method = "\\.([\\w|_|\\$]+|<init>|<clinit>)";
        public static final String exception = "(" + clazz + "(?:Exception|Error))";
        public static final String message = "(:\\s+(.*?))?";
        public static final String frame = "(?:at\\s+)?" + clazz + method;
        public static final String fileChars = "[^\\(\\)]+";
        public static final String file = "\\((((" + fileChars + "):(\\d+))|(" + fileChars + "(\\([^\\)]*\\))?))\\)";
        public static final String cause = "\\s*Caused\\s+by:\\s+" + exception + message + "";
        public static final String more = "\\s*\\.\\.\\.\\s+\\d+\\s+more";
        public static final String trace = "(((?:" + cause + ")?\\s+" + frame + "\\s*" + file + "|" + more + ")+)";

        public static final String stacktrace = exception + message + trace;
        public static final String nestedFrame = "(?:" + cause + "|at\\s+(" + clazz + ")(" + method + ")\\s*" + file
                + ")";
        public static final Pattern stacktracePattern = Pattern.compile(stacktrace);
        public static final Pattern framePattern = Pattern.compile(nestedFrame);
    }
}

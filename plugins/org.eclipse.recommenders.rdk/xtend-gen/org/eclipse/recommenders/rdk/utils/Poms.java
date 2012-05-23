package org.eclipse.recommenders.rdk.utils;

import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class Poms {
  public static CharSequence newPom(final String groupId, final String artifactId, final String version) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    _builder.newLine();
    _builder.append("<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<modelVersion>4.0.0</modelVersion>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<groupId>");
    _builder.append(groupId, "	");
    _builder.append("</groupId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<artifactId>");
    _builder.append(artifactId, "	");
    _builder.append("</artifactId>");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("<version>");
    _builder.append(version, "	");
    _builder.append("</version>");
    _builder.newLineIfNotEmpty();
    _builder.append("</project>");
    _builder.newLine();
    return _builder;
  }
}

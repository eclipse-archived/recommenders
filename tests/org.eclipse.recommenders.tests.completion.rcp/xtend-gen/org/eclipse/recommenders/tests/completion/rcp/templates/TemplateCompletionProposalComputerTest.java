package org.eclipse.recommenders.tests.completion.rcp.templates;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
@Ignore
public class TemplateCompletionProposalComputerTest {
  private static AtomicInteger classId = new Function0<AtomicInteger>() {
    public AtomicInteger apply() {
      AtomicInteger _atomicInteger = new AtomicInteger();
      return _atomicInteger;
    }
  }.apply();
  
  private static JavaProjectFixture fixture = new Function0<JavaProjectFixture>() {
    public JavaProjectFixture apply() {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      return _javaProjectFixture;
    }
  }.apply();
  
  @Before
  public void before() {
    try {
      TemplateCompletionProposalComputerTest.fixture.clear();
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public CharSequence method(final CharSequence code) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import javax.swing.*;");
    _builder.newLine();
    _builder.append("public class Template");
    int _incrementAndGet = TemplateCompletionProposalComputerTest.classId.incrementAndGet();
    _builder.append(_incrementAndGet, "");
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("void test (){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append(code, "		");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  @Test
  public void testNotImportedTypeNameCompletion() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("// import java.awt.Button;");
      _builder.newLine();
      _builder.append("public class Template");
      int _incrementAndGet = TemplateCompletionProposalComputerTest.classId.incrementAndGet();
      _builder.append(_incrementAndGet, "");
      _builder.append(" {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("void test() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("Button$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final CharSequence code = _builder;
      this.test(code);
  }
  
  @Test
  public void testOnQulifiedTypeName() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("// import java.awt.Button;");
      _builder.newLine();
      _builder.append("public class Template");
      int _incrementAndGet = TemplateCompletionProposalComputerTest.classId.incrementAndGet();
      _builder.append(_incrementAndGet, "");
      _builder.append(" {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("void test() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("java.awt.Button$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final CharSequence code = _builder;
      this.test(code);
  }
  
  @Test
  public void testImportedTypeNameCompletion() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import java.awt.Button;");
      _builder.newLine();
      _builder.append("public class Template");
      int _incrementAndGet = TemplateCompletionProposalComputerTest.classId.incrementAndGet();
      _builder.append(_incrementAndGet, "");
      _builder.append(" {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("void test() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("Button$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final CharSequence code = _builder;
      this.test(code);
  }
  
  @Test
  public void testInMessageSend() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List l;");
      _builder.newLine();
      _builder.append("l.add(l$);");
      _builder.newLine();
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testInCompletionOnQualifiedNameRef() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List l;");
      _builder.newLine();
      _builder.append("l.$");
      _builder.newLine();
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testInMessageSend2() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("List l;");
      _builder.newLine();
      _builder.append("l.add(l.$);");
      _builder.newLine();
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testLocalWithTypeName() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import java.awt.Button;");
      _builder.newLine();
      _builder.append("public class Template");
      int _incrementAndGet = TemplateCompletionProposalComputerTest.classId.incrementAndGet();
      _builder.append(_incrementAndGet, "");
      _builder.append(" {");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("void test() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("Integer i= null;");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("i$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final CharSequence code = _builder;
      this.test(code);
  }
  
  private void test(final CharSequence code) {
    this.test(code, 1);
  }
  
  private void test(final CharSequence code, final int numberOfExpectedProposals) {
    try {
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        cu.becomeWorkingCopy(null);
        CompilationUnit _reconcile = cu.reconcile(AST.JLS4, true, true, null, null);
        final CompilationUnit ast = _reconcile;
        Assert.assertNotNull(ast);
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}

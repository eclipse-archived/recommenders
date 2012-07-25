package org.eclipse.recommenders.tests.completion.rcp.calls;

import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.completion.rcp.calls.CallCompletionProposalComputerSmokeTest;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class QueryTest {
  private CallsCompletionProposalComputer sut;
  
  private List<IJavaCompletionProposal> proposals;
  
  private CharSequence code;
  
  @Test
  public void testDefMethodReturn01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("List l = Collections.emptyList();");
    _builder.newLine();
    _builder.append("l.get(0).$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.METHOD_RETURN);
  }
  
  @Test
  public void testDefMethodReturn012() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("List l;");
    _builder.newLine();
    _builder.append("Object o = l.get(0);");
    _builder.newLine();
    _builder.append("o.$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.METHOD_RETURN);
  }
  
  @Test
  public void testDefField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("List l;");
    _builder.newLine();
    _builder.append("void __test(){");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("l.$;");
    _builder.newLine();
    _builder.append("}");
    CharSequence _classbody = CodeBuilder.classbody(_builder);
    this.code = _classbody;
    this.exercise();
    this.verifyDefinition(Kind.FIELD);
  }
  
  @Test
  public void testFindCalls01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o = null;");
    _builder.newLine();
    _builder.append("o.equals(new Object() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("public boolean equals(Object obj) {");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("o.hashCode();");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("return false;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("});");
    _builder.newLine();
    _builder.append("o.$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    HashSet<String> _newHashSet = CollectionLiterals.<String>newHashSet("equals");
    this.verifyCalls(_newHashSet);
  }
  
  @Test
  public void testFindCalls02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o = null;");
    _builder.newLine();
    _builder.append("o.equals();");
    _builder.newLine();
    _builder.append("Object o2 = null;");
    _builder.newLine();
    _builder.append("o2.hashCode();");
    _builder.newLine();
    _builder.append("o.$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    HashSet<String> _newHashSet = CollectionLiterals.<String>newHashSet("equals");
    this.verifyCalls(_newHashSet);
  }
  
  @Test
  public void testDefThis01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.THIS);
  }
  
  @Test
  public void testDefThis02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.THIS);
  }
  
  @Test
  public void testDefThis03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("this.$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.THIS);
  }
  
  @Test
  public void testDefThis04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("super.$");
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    this.verifyDefinition(Kind.THIS);
  }
  
  @Test
  public void testDefThis05() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public boolean equals(Object o){");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("boolean res = super.equals(o);");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("this.hash$");
    _builder.newLine();
    _builder.append("}");
    CharSequence _classbody = CodeBuilder.classbody(_builder);
    this.code = _classbody;
    this.exercise();
    this.verifyDefinition(Kind.THIS);
  }
  
  public List<IJavaCompletionProposal> exercise() {
    List<IJavaCompletionProposal> _xblockexpression = null;
    {
      final Tuple<List<IJavaCompletionProposal>,CallsCompletionProposalComputer> actual = CallCompletionProposalComputerSmokeTest.exercise(this.code);
      CallsCompletionProposalComputer _second = actual.getSecond();
      this.sut = _second;
      List<IJavaCompletionProposal> _first = actual.getFirst();
      List<IJavaCompletionProposal> _proposals = this.proposals = _first;
      _xblockexpression = (_proposals);
    }
    return _xblockexpression;
  }
  
  public void verifyDefinition(final Kind kind) {
    Assert.assertEquals(kind, this.sut.query.kind);
  }
  
  public void verifyCalls(final Set<String> expected) {
    final HashSet<Object> actual = CollectionLiterals.<Object>newHashSet();
    final Function1<IMethodName,String> _function = new Function1<IMethodName,String>() {
        public String apply(final IMethodName e) {
          String _name = e.getName();
          return _name;
        }
      };
    Iterable<String> _map = IterableExtensions.<IMethodName, String>map(this.sut.query.calls, _function);
    Iterables.<Object>addAll(actual, _map);
    Assert.assertEquals(expected, actual);
  }
}

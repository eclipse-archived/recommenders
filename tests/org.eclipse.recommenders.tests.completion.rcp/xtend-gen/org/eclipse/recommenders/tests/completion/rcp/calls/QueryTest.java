package org.eclipse.recommenders.tests.completion.rcp.calls;

import java.util.List;
import junit.framework.Assert;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.completion.rcp.calls.CallCompletionProposalComputerSmokeTest;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
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
  
  public List<IJavaCompletionProposal> exercise() {
    List<IJavaCompletionProposal> _xblockexpression = null;
    {
      Tuple<List<IJavaCompletionProposal>,CallsCompletionProposalComputer> _exercise = CallCompletionProposalComputerSmokeTest.exercise(this.code);
      final Tuple<List<IJavaCompletionProposal>,CallsCompletionProposalComputer> actual = _exercise;
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
}

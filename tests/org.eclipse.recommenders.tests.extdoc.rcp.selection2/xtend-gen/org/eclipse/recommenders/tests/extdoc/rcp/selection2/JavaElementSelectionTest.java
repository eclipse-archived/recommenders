package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.recommenders.tests.extdoc.rcp.selection2.XtendUtils;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xtend2.lib.StringConcatenation;
import org.junit.Test;

@SuppressWarnings("all")
public class JavaElementSelectionTest {
  @Test
  public void testAnonymousTypeDeclarationInFieldInitializer() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Myclass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("Str\u2022ing s = new St\u2022ring(\"\");");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<String,Integer> _operator_mappedTo = ObjectExtensions.<String, Integer>operator_mappedTo("Ljava/lang/String;", ((Integer)2));
      List<String> _newListWithFrequency = XtendUtils.<String>newListWithFrequency(_operator_mappedTo);
      final List<String> expected = _newListWithFrequency;
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      final JavaProjectFixture fixture = _javaProjectFixture;
      String _string = code.toString();
      Tuple<CompilationUnit,Set<Integer>> _parseWithMarkers = fixture.parseWithMarkers(_string, "MyClass.java");
      final Tuple<CompilationUnit,Set<Integer>> struct = _parseWithMarkers;
      CompilationUnit _first = struct.getFirst();
      final CompilationUnit cu = _first;
      Set<Integer> _second = struct.getSecond();
      final Set<Integer> pos = _second;
      for (final Integer position : pos) {
        {
          ASTNode _perform = NodeFinder.perform(cu, position, 0);
          final ASTNode selection = _perform;
          final ASTNode selection_1 = selection;
          boolean matched = false;
          if (!matched) {
            if (selection_1 instanceof SimpleName) {
              final SimpleName selection_2 = (SimpleName) selection_1;
              matched=true;
              {
                IBinding _resolveBinding = selection_2.resolveBinding();
                final IBinding binding = _resolveBinding;
                IJavaElement _javaElement = binding.getJavaElement();
                final IJavaElement javaElement = _javaElement;
                InputOutput.<IJavaElement>println(javaElement);
              }
            }
          }
        }
      }
  }
}

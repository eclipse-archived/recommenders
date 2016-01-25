package org.eclipse.recommenders.jdt.templates;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.testing.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Joiner;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class SnippetCodeBuilderTest {

    private static final Joiner JOINER = Joiner.on('\n');
    private static final String NO_SNIPPET = "";
    private static final String HASH_MARKER = "#";

    private JavaProjectFixture fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), getClass().getName());

    private final CharSequence code;
    private final String customMarker;
    private final String expectedResult;
    private final List<String> nodeNames;

    private CompilationUnit ast;
    private IDocument document;
    private IRegion selection;
    private HashMap<ASTNode, String> nodesToReplace;

    private static int testCount;

    public SnippetCodeBuilderTest(String description, CharSequence code, String customMarker, List<String> nodeNames,
            String expectedResult) {
        this.code = code;
        this.customMarker = customMarker;
        this.expectedResult = expectedResult;
        this.nodeNames = nodeNames;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> scenarios() {
        Collection<Object[]> scenarios = new LinkedList<>();

        // @formatter:off
        scenarios.add(scenario("No selection",
                multiLine("class Example { }"),
                NO_SNIPPET));

        scenarios.add(scenario("Local variable variable declaration",
                multiLine("package org.example;",
                          "class Example {",
                          "    void m() {",
                          "        $String s;$",
                          "     }",
                          "}"),
                multiLine("String ${s:newName(java.lang.String)};",
                          "${cursor}")));

        scenarios.add(scenario("Local variable declaration and initialization",
                multiLine("package org.example;",
                          "class Example {",
                          "    void m() {",
                          "        $int i = 1;$",
                          "     }",
                          "}"),
                multiLine("int ${i:newName(int)} = 1;",
                          "${cursor}")));

        scenarios.add(scenario("Local variable declaration and assignment with line break",
                multiLine("package org.example;",
                          "class Example {",
                          "    void m() {",
                          "        $int i = 0;",
                          "        i = 1;$",
                          "     }",
                          "}"),
                multiLine("int ${i:newName(int)} = 0;",
                          "${i} = 1;",
                          "${cursor}")));

        scenarios.add(scenario("Array declaration and initialization",
                multiLine("package org.example;",
                          "class Example {",
                          "    void m() {",
                          "        $String[][] s = new String[][] { { \"hello\" } };$",
                          "     }",
                          "}"),
                multiLine("String[][] ${s:newName('java.lang.String[][]')} = new String[][] { { \"hello\" } };",
                          "${cursor}")));

        scenarios.add(scenario("Field access of array field",
                multiLine("package org.example;",
                          "class Example {",
                          "    String f[];",
                          "    void m() {",
                          "        $f = null;$",
                          "     }",
                          "}"),
                multiLine("${f:field('java.lang.String[]')} = null;",
                          "${cursor}")));

        scenarios.add(scenario("Field access of array field: declaration after access",
                multiLine("package org.example;",
                          "class Example {",
                          "    void m() {",
                          "        $f = null;$",
                          "     }",
                          "    String f[];",
                          "}"),
                multiLine("${f:field('java.lang.String[]')} = null;",
                          "${cursor}")));

        scenarios.add(scenario("Static field reference",
                multiLine("package org.example;",
                          "class Example {",
                          "    static String F;",
                          "    void m() {",
                          "        $F = null;$",
                          "     }",
                          "}"),
                multiLine("F = null;",
                          "${:importStatic(org.example.Example.F)}${cursor}")));

        scenarios.add(scenario("Method call on local variable of imported type",
                multiLine("package org.example;",
                          "import java.util.ArrayList;",
                          "import java.util.List;",
                          "class Example {",
                          "    void m() {",
                          "        $List l = new ArrayList();",
                          "        l.hashCode();$",
                          "     }",
                          "}"),
                multiLine("List ${l:newName(java.util.List)} = new ArrayList();",
                          "${l}.hashCode();",
                          "${:import(java.util.ArrayList, java.util.List)}${cursor}")));

        scenarios.add(scenario("Variable declaration of imported nested type",
                multiLine("package org.example;",
                          "import java.util.Map;",
                          "class Example {",
                          "    void m() {",
                          "        $Map.Entry e = null;$",
                          "     }",
                          "}"),
                multiLine("Map.Entry ${e:newName(java.util.Map.Entry)} = null;",
                          "${:import(java.util.Map)}${cursor}")));

        scenarios.add(scenario("Multiple same type local variables in different blocks",
                multiLine("package org.example;",
                          "class Example {",
                          "    $void m1() { String e; }",
                          "    void m2() { String e2; }",
                          "    void m3() { String e; }$",
                          "}"),
                multiLine("void m1() { String ${e:newName(java.lang.String)}; }",
                          "void m2() { String ${e2:newName(java.lang.String)}; }",
                          "void m3() { String ${e1:newName(java.lang.String)}; }",
                        "${cursor}")));

        scenarios.add(scenario("Method declaration with argument",
                multiLine("package org.example;",
                          "class Example {",
                          "    $void m(String s) { s = \"hello\"; }$",
                          "}"),
                multiLine("void m(String ${s:newName(java.lang.String)}) { ${s} = \"hello\"; }",
                        "${cursor}")));

        scenarios.add(scenario("Interface declaration",
                multiLine("package org.example;",
                          "$interface Interface { }$"),
                multiLine("interface Interface { }",
                          "${cursor}")));

        scenarios.add(scenario("Local Constructor invocation",
                multiLine("package org.example;",
                          "class Example {",
                          "    static void main(String[] args) {",
                          "        $Example example = new Example();$",
                          "    }",
                          "}"),
                multiLine("Example ${example:newName(org.example.Example)} = new Example();",
                          "${:import(org.example.Example)}${cursor}")));

        scenarios.add(scenario("Field declaration: unresolvable type",
                multiLine("package org.example;",
                          "class Example {",
                          "    $Unresolvable f;$",
                          "}"),
                multiLine("Unresolvable f;",
                          "${cursor}")));

        scenarios.add(scenario("Generic Field declaration",
                multiLine("package org.example;",
                          "class Example<T> {",
                          "    $Example<String> f;$",
                          "}"),
                multiLine("Example<String> ${f:newName(org.example.Example)};",
                          "${:import(org.example.Example)}${cursor}")));

        scenarios.add(scenario("Generic Field declaration: unresolvable generic type",
                multiLine("package org.example;",
                          "class Example<T> {",
                          "    $Example<Unresolvable> f;$",
                          "}"),
                multiLine("Example<Unresolvable> ${f:newName(org.example.Example)};",
                          "${:import(org.example.Example)}${cursor}")));

        scenarios.add(customMarkerScenario("Dollar string",
                multiLine("class Example {",
                          "    String f = #\"Cost: $20\";#",
                          "}"),
                HASH_MARKER,
                multiLine("\"Cost: $$20\";",
                          "${cursor}")));

        scenarios.add(customMarkerScenario("Dollar string: two dollars",
                multiLine("class Example {",
                          "    String f = #\"Cost: $$20\";#",
                          "}"),
                HASH_MARKER,
                multiLine("\"Cost: $$$$20\";",
                          "${cursor}")));

        scenarios.add(customMarkerScenario("Dollar string: variable declaration with dollar in name",
                multiLine("class Example {",
                          "    void m() {",
                          "        #String f$ield;#",
                          "    }",
                          "}"),
                HASH_MARKER,
                multiLine("String ${f_ield:newName(java.lang.String)};",
                          "${cursor}")));

        scenarios.add(customMarkerScenario("Dollar string: two names, one with dollar",
                multiLine("class Example {",
                          "    void m() {",
                          "        String var_name;",
                          "        String var$name;",
                          "        #var_name = var$name;#",
                          "    }",
                          "}"),
                HASH_MARKER,
                multiLine("${var_name:var(java.lang.String)} = ${var_name1:var(java.lang.String)};",
                          "${cursor}")));

        scenarios.add(scenario("Badly formatted code",
                multiLine("class Example {",
                          "    void m() {",
                          "    $    int var;",
                          "        var = 1;",
                          "            var = 2;",
                          "     var = 3;    $",
                          "    }",
                          "}"),
                multiLine("    int ${var:newName(int)};",
                          "${var} = 1;",
                          "    ${var} = 2;",
                          "     ${var} = 3;    ",
                          "${cursor}")));

        scenarios.add(scenario("Line break within node",
                multiLine("class Example {",
                          "    void m() {",
                          "        $new Example()",
                          "                       .m();$",
                          "    }",
                          "}"),
                multiLine("new Example()",
                          "               .m();",
                          "${cursor}")));
        scenarios.add(scenario("Line break within node: non-whitespace before selection",
                multiLine("class Example {",
                          "    int m() {",
                          "        int result = $new Example()",
                          "                .m();$",
                          "        return result;",
                          "    }",
                          "}"),
                multiLine("new Example()",
                          "        .m();",
                          "${cursor}")));

        scenarios.add(scenario("Single line file selected",
                "$class Example { }$",
                multiLine("class Example { }",
                          "${cursor}")));

        scenarios.add(scenario("Single line file with leading whitespace selected",
                "$    class Example { }$",
                multiLine("class Example { }",
                          "${cursor}")));

        scenarios.add(scenario("Local variable referenced multiple times (Bug 455598)",
                multiLine("class Example {",
                          "    void m(String s) {",
                          "        $s = s.toString();",
                          "        s = s.toString();$",
                          "    }",
                          "}"),
                multiLine("${s:var(java.lang.String)} = ${s}.toString();",
                          "${s} = ${s}.toString();",
                          "${cursor}")));
        scenarios.add(scenario("Field referenced multiple times (Bug 455598)",
                multiLine("class Example {",
                          "    String f;",
                          "    void m() {",
                          "        $f = f.toString();",
                          "        f = f.toString();$",
                          "    }",
                          "}"),
                multiLine("${f:field(java.lang.String)} = ${f}.toString();",
                          "${f} = ${f}.toString();",
                          "${cursor}")));

        scenarios.add(scenario("Entire selected region replaced",
                multiLine("package org.example;",
                        "class Example {",
                        "    void m() {",
                        "        $$String s;$$",
                        "    }",
                        "}"),
                asList("replaced"),
                multiLine("${replaced}",
                          "${cursor}")));
        scenarios.add(scenario("Part of selected region replaced",
                multiLine("package org.example;",
                        "class Example {",
                        "    void m() {",
                        "        $int i = $0$;$",
                        "    }",
                        "}"),
                asList("replaced"),
                multiLine("int ${i:newName(int)} = ${replaced:var(int)};",
                          "${cursor}")));
        scenarios.add(scenario("Multiple selected regions replaced",
                multiLine("package org.example;",
                        "class Example {",
                        "    void m() {",
                        "        $int i = $0$ + $1$;$",
                        "    }",
                        "}"),
                asList("zero", "one"),
                multiLine("int ${i:newName(int)} = ${zero:var(int)} + ${one:var(int)};",
                          "${cursor}")));
        scenarios.add(scenario("Name clash of preferred template variable names",
                multiLine("package org.example;",
                        "class Example {",
                        "    void m() {",
                        "        $int i = $0$;$",
                        "    }",
                        "}"),
                asList("i"),
                multiLine("int ${i:newName(int)} = ${i1:var(int)};",
                          "${cursor}")));
        scenarios.add(scenario("Name clash of preferred template variable names",
                multiLine("package org.example;",
                        "class Example {",
                        "    void m() {",
                        "        $int x = $0$;",
                        "        int y = 1;$",
                        "    }",
                        "}"),
                asList("y"),
                multiLine("int ${x:newName(int)} = ${y:var(int)};",
                          "int ${y1:newName(int)} = 1;",
                          "${cursor}")));
        scenarios.add(scenario("Line break after replaced node",
                multiLine("class Example {",
                          "    void m() {",
                          "        $$new Example()$",
                          "                       .m();$",
                          "    }",
                          "}"),
                asList("example"),
                multiLine("${example:var(Example)}",
                          "               .m();",
                          "${cursor}")));
        // @formatter:on

        return scenarios;
    }

    private static Object[] scenario(String description, CharSequence code, String expectedResult) {
        return new Object[] { description, code, null, Collections.emptyList(), expectedResult };
    }

    private static Object[] scenario(String description, CharSequence code, List<String> nodeNames,
            String expectedResult) {
        return new Object[] { description, code, null, nodeNames, expectedResult };
    }

    private static Object[] customMarkerScenario(String description, CharSequence code, String marker,
            String expectedResult) {
        return new Object[] { description, code, marker, Collections.emptyList(), expectedResult };
    }

    private static String multiLine(String... lines) {
        String result = JOINER.join(lines);
        result += '\n';
        return result;
    }

    @Before
    public void setUp() throws Exception {
        fixture = new JavaProjectFixture(ResourcesPlugin.getWorkspace(), SnippetCodeBuilderTest.class.getName()
                + testCount++);

        Pair<ICompilationUnit, List<Integer>> struct;
        if (customMarker != null) {
            struct = fixture.createFileAndParseWithMarkers(code, customMarker);
        } else {
            struct = fixture.createFileAndPackageAndParseWithMarkers(code);
        }
        ICompilationUnit cu = struct.getFirst();

        CompilationUnitEditor editor = (CompilationUnitEditor) EditorUtility.openInEditor(cu);
        ITypeRoot root = (ITypeRoot) editor.getViewPartInput();
        ast = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);

        document = editor.getViewer().getDocument();

        int start, end;
        List<Integer> markers = struct.getSecond();
        if (!markers.isEmpty()) {
            start = markers.get(0);
            end = markers.get(markers.size() - 1);
        } else {
            start = -1;
            end = -1;
        }
        selection = new Region(start, end - start);

        nodesToReplace = new HashMap<>();
        for (int i = 0; i < nodeNames.size(); i++) {
            int nodeStart = markers.get(1 + 2 * i);
            int nodeEnd = markers.get(2 + 2 * i);
            ASTNode nodeToReplace = NodeFinder.perform(ast, nodeStart, nodeEnd - nodeStart);
            String nodeName = nodeNames.get(i);
            nodesToReplace.put(nodeToReplace, nodeName);
        }
    }

    @Test
    public void test() throws Exception {
        SnippetCodeBuilder sut = new SnippetCodeBuilder(ast, document, selection, nodesToReplace);

        String result = sut.build();

        assertThat(result, is(equalTo(expectedResult)));
    }
}

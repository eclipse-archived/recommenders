package org.eclipse.recommenders.tests.wala;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;

import com.google.common.io.Files;

public class CompilerUtil {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;

    public CompilerUtil() throws IOException {
        initialize();
    }

    private void initialize() throws IOException {
        compiler = ToolProvider.getSystemJavaCompiler();
        final File outputDir = Files.createTempDir();
        fileManager = compiler.getStandardFileManager(null, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir));
    }

    public boolean compile(final String... javaSourceFileContents) {
        final List<JavaFileObject> cus = newArrayList();
        for (final String source : javaSourceFileContents) {
            cus.add(new InMemoryJavaFile(source));
        }
        final CompilationTask task = compiler.getTask(null, fileManager, null, null, null, cus);
        return task.call();
    }

    public File getClassesLocation() {
        final Iterable<? extends File> location = fileManager.getLocation(StandardLocation.CLASS_OUTPUT);
        return ensureIsNotNull(getFirst(location, null));
    }

    private static class InMemoryJavaFile extends SimpleJavaFileObject {

        private final String source;

        protected InMemoryJavaFile(final String source) {
            super(URI.create("string:///" + JavaProjectFixture.findClassName(source) + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
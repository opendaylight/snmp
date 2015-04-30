/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class OIDGeneratorTest {

    private static final String IF_ENTRY_CLASS_NAME = "IfEntryBuilder";
    private static final String IF_ENTRY_FILE_NAME = IF_ENTRY_CLASS_NAME + ".java";

    private OIDGenerator testOIDGenerator;
    private File mockOIDDirectory;
    private File mockYangDirectory;
    private File mockOIDClassFile;
    private File testJavaYangModelFile;
    private FileWriter mockFileWriter;
    private StringBuilder changedFileBuilder;

    @Before
    public void setUp() throws Exception {
        testJavaYangModelFile = new File(getClass().getClassLoader().getResource(IF_ENTRY_FILE_NAME).getFile());
        mockFileWriter = mock(FileWriter.class);

        mockOIDClassFile = mock(File.class);
        File mockOIDParent = mock(File.class);
        when(mockOIDParent.exists()).thenReturn(true);

        when(mockOIDClassFile.getParentFile()).thenReturn(mockOIDParent);
        mockOIDDirectory = testJavaYangModelFile.getParentFile();
        mockYangDirectory = testJavaYangModelFile.getParentFile();

        OIDGenerator oidGenerator = new OIDGenerator(mockOIDDirectory, mockYangDirectory, mockOIDClassFile);
        testOIDGenerator = spy(oidGenerator);

        changedFileBuilder = new StringBuilder();

        doReturn(mockFileWriter).when(testOIDGenerator).getFileWriterForFile(testJavaYangModelFile);
        doNothing().when(testOIDGenerator).writeLinesToFile(anyString(), any(File.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String writeString = (String) invocationOnMock.getArguments()[0];
                changedFileBuilder.append(writeString);
                return null;
            }
        }).when(mockFileWriter).write(anyString());

    }

    @After
    public void tearDown() throws Exception {

    }

    private File targetDir() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../target");
        return targetDir;
    }

    @Test
    public void testExecute() throws Exception {
        testOIDGenerator.execute();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

        String classOutputFolder = targetDir().getAbsolutePath() + "/generated-test-sources";
        Iterable options = Arrays.asList("-d", classOutputFolder);

        JavaFileObject fileObject = new JavaSourceFromString(IF_ENTRY_CLASS_NAME, changedFileBuilder.toString());
        Iterable<? extends JavaFileObject> iterableFileObjects = Arrays.asList(fileObject);
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, null, diagnosticCollector, options, null, iterableFileObjects);

        boolean didSucceed = compilationTask.call();
        Class ifEntryClass = null;

        if (didSucceed) {
            try {
                URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(classOutputFolder).toURL()});
                ifEntryClass = classLoader.loadClass(IF_ENTRY_CLASS_NAME);
            } catch (ClassNotFoundException e) {
                throw e;
            }
        } else {
            fail("Compilation failed");
        }

        List<String> testMethodNames = Arrays.asList("getIfDescr", "getIfMtu", "setIfMtu", "setIfDescr");

        if (ifEntryClass != null) {
            for (Method method : ifEntryClass.getMethods()) {
                if (testMethodNames.contains(method.getName())) {
                    OID oid = method.getAnnotation(OID.class);
                    assertNotNull("OID for method" + method.getName() + " was null", oid);
                }
            }
        } else {
            fail("ifEntryClass is null");
        }
    }

    /**
     * Using example from http://www.java2s.com/Code/Java/JDK-6/CompilingfromMemory.htm
     */
    private class JavaSourceFromString extends SimpleJavaFileObject {

        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}

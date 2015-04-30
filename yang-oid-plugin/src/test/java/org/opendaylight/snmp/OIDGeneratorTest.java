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

import java.io.File;
import java.io.FileWriter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class OIDGeneratorTest {

    private OIDGenerator testOIDGenerator;
    private File mockOIDDirectory;
    private File mockYangDirectory;
    private File mockOIDClassFile;
    private File testJavaYangModelFile;
    private FileWriter mockFileWriter;

    @Before
    public void setUp() throws Exception {
        testJavaYangModelFile = new File(getClass().getClassLoader().getResource("IfEntry.java").getFile());
        mockFileWriter = mock(FileWriter.class);

        mockOIDClassFile = mock(File.class);
        File mockOIDParent = mock(File.class);
        when(mockOIDParent.exists()).thenReturn(true);

        when(mockOIDClassFile.getParentFile()).thenReturn(mockOIDParent);
        mockOIDDirectory = testJavaYangModelFile.getParentFile();
        mockYangDirectory = testJavaYangModelFile.getParentFile();

        OIDGenerator oidGenerator = new OIDGenerator(mockOIDDirectory, mockYangDirectory, mockOIDClassFile);
        testOIDGenerator = spy(oidGenerator);

        doReturn(mockFileWriter).when(testOIDGenerator).getFileWriterForFile(testJavaYangModelFile);
        doNothing().when(testOIDGenerator).writeLinesToFile(anyString(), any(File.class));

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testExecute() throws Exception {
        testOIDGenerator.execute();
    }
}

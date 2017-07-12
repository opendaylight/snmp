/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.snmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "oid", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class OIDGenerator extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}/../src/main/oid", property = "oidDirectory", required = true)
    private File oidDirectory;

    @Parameter(defaultValue = "${project.build.directory}/../src/main/yang-gen-sal", property = "yangDirectory",
            required = true)
    private File yangDirectory;

    @Parameter(defaultValue = "${project.build.directory}/../src/main/java/org/opendaylight/snmp/OID.java",
            property = "oidClassDir", required = true)
    private File oidClassFile;

    private static final String IMPORT_STRING = "import org.opendaylight.snmp.OID;";
    private static final String OID_CLASS = "/*\n"
            + " * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.\n" + " *\n"
            + " * This program and the accompanying materials are made available under the\n"
            + " * terms of the Eclipse Public License v1.0 which accompanies this distribution,\n"
            + " * and is available at http://www.eclipse.org/legal/epl-v10.html\n" + " */\n"
            + "package org.opendaylight.snmp;\n" + "\n" + "import java.lang.annotation.ElementType;\n"
            + "import java.lang.annotation.Retention;\n" + "import java.lang.annotation.RetentionPolicy;\n"
            + "import java.lang.annotation.Target;\n" + "\n" + "@Retention(RetentionPolicy.RUNTIME)\n"
            + "@Target(ElementType.METHOD)\n" + "public @interface OID {\n"
            + "    public String value() default \"\";\n" + "}";

    public OIDGenerator() {

    }

    OIDGenerator(File oidDirectory, File yangDirectory, File oidClassFile) {
        this.oidDirectory = oidDirectory;
        this.yangDirectory = yangDirectory;
        this.oidClassFile = oidClassFile;
    }

    @Override
    public void execute() throws MojoFailureException {
        // Read in the oid files
        List<String> fileNames = new ArrayList<>(getFileNames(new ArrayList<String>(), oidDirectory.toPath()));
        Map<String, String> nameToOIDHashMap = new HashMap<>();

        for (String fileName : fileNames) {
            // Get all of the OIDs
            nameToOIDHashMap.putAll(parseOIDsFromFile(new File(fileName)));
        }

        fileNames = new ArrayList<>(getFileNames(new ArrayList<String>(), yangDirectory.toPath()));
        Boolean modified = false;
        for (String fileName : fileNames) {
            // Parse all the generated yang files
            try {
                modified = parseAndUpdateGeneratedYangFile(new File(fileName), nameToOIDHashMap) || modified;
            } catch (IOException e) {
                getLog().error("Could not process " + fileName, e);
                throw new MojoFailureException("Could not process " + fileName + " due to " + e);
            }
        }

        if (modified) {
            // Create the folder if needed

            // Write the OID class to disk

            File oidClassFolder = oidClassFile.getParentFile();
            if (!oidClassFolder.exists() && !oidClassFolder.mkdirs()) {
                throw new MojoFailureException("Error creating directory " + oidClassFolder);
            }

            // Write the new lines to the file
            try {
                writeLinesToFile(OID_CLASS, oidClassFile);
            } catch (IOException e) {
                getLog().info("Error writting changes to file", e);
            }
        }
    }

    private Map<String, String> parseOIDsFromFile(File file) throws MojoFailureException {
        Map<String, String> nameToOIDMap = new HashMap<>();
        String pattern = "\"(\\w+)\"\\s+\"([\\d\\.]+)\"";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;

        try (BufferedReader br = getReaderForFile(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                matcher = regex.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1).toLowerCase(Locale.getDefault());
                    String oid = matcher.group(2);
                    nameToOIDMap.put(name, oid);
                }
            }
        } catch (IOException e) {
            getLog().error("Error reading file " + file, e);
            throw new MojoFailureException("Error reading file " + file + ": " + e);
        }

        return nameToOIDMap;
    }

    BufferedReader getReaderForFile(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
    }

    void writeLinesToFile(String content, File file) throws IOException {
        try (Writer fileWriter = getFileWriterForFile(file)) {
            fileWriter.write(content);
        }
    }

    private List<String> getFileNames(List<String> fileNames, Path dir) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getFileNames(fileNames, path);
                } else {
                    fileNames.add(path.toAbsolutePath().toString());
                }
            }
            stream.close();
        } catch (IOException e) {
            getLog().warn("Error getting files in path", e);
        }
        return fileNames;
    }

    private String getAnnotation(String oid) {
        return String.format("        @org.opendaylight.snmp.OID(value = \"%s\")", oid);
    }

    private boolean isAnnotation(String line) {
        return line.contains("@org.opendaylight.snmp.OID(value =");
    }

    Writer getFileWriterForFile(File file) throws IOException {
        return new OutputStreamWriter(new FileOutputStream(file), Charset.defaultCharset());
    }

    private Boolean parseAndUpdateGeneratedYangFile(File file, Map<String, String> nameToOIDHashMap)
            throws IOException {
        List<String> newLines = new ArrayList<>();

        String pattern = "public\\s[\\w\\.]+\\s[g|s]et(\\w+)\\(";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;
        Boolean isModified = false;

        try (BufferedReader br = getReaderForFile(file)) {
            String line;
            while ((line = br.readLine()) != null) {
                matcher = regex.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1).toLowerCase(Locale.getDefault());
                    if (nameToOIDHashMap.containsKey(name)) {
                        isModified = true;
                        newLines.add(getAnnotation(nameToOIDHashMap.get(name)));
                    }
                } else if (isAnnotation(line)) {
                    getLog().debug("Replacing existing annotation: " + line.trim());
                    isModified = true;
                    continue;
                }
                newLines.add(line);
            }
        }

        if (isModified) {
            // Write the new lines to the file
            getLog().info(String.format("Writing changes to file %s", file.getName()));
            try (Writer fileWriter = getFileWriterForFile(file)) {
                Boolean lastLineIsImport = false;
                Boolean isImportLine;
                for (String newLine : newLines) {
                    // Include the import statement for the OID tag as the last import

                    isImportLine = newLine.trim().startsWith("import ");
                    if (!isImportLine && lastLineIsImport) {
                        fileWriter.write(IMPORT_STRING + "\n");
                    }

                    lastLineIsImport = isImportLine;
                    fileWriter.write(newLine + "\n");
                }
            }
        }
        return isModified;
    }
}

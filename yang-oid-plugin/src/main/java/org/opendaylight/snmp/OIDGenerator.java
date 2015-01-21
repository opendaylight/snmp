/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.snmp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Mojo( name = "oid", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class OIDGenerator extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}/../src/main/oid", property = "oidDirectory", required = true )
    private File oidDirectory;

    @Parameter( defaultValue = "${project.build.directory}/../src/main/yang-gen-sal", property = "yangDirectory", required = true)
    private File yangDirectory;

    @Parameter( defaultValue = "${project.build.directory}/../src/main/java/org/opendaylight/snmp/OID.java", property = "oidClassDir", required = true)
    private File oidClassFile;

    private static Log LOG;
    private HashMap<String, String> nameToOIDHashMap;
    private static final String importString = "import org.opendaylight.snmp.OID;";
    private static final String OIDClass = "/*\n" +
            " * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.\n" +
            " *\n" +
            " * This program and the accompanying materials are made available under the\n" +
            " * terms of the Eclipse Public License v1.0 which accompanies this distribution,\n" +
            " * and is available at http://www.eclipse.org/legal/epl-v10.html\n" +
            " */\n" +
            "package org.opendaylight.snmp;\n" +
            "\n" +
            "import java.lang.annotation.ElementType;\n" +
            "import java.lang.annotation.Retention;\n" +
            "import java.lang.annotation.RetentionPolicy;\n" +
            "import java.lang.annotation.Target;\n" +
            "\n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "@Target(ElementType.METHOD)\n" +
            "public @interface OID {\n" +
            "    public String value() default \"\";\n" +
            "}";

    public void execute() throws MojoExecutionException {
        LOG = getLog();

        // Read in the oid files
        ArrayList<String> fileNames = new ArrayList(getFileNames(new ArrayList<String>(), oidDirectory.toPath()));
        nameToOIDHashMap = new HashMap<>();

        for (String fileName : fileNames) {
            // Get all of the OIDs
            nameToOIDHashMap.putAll(parseOIDsFromFile(new File(fileName)));
        }

        fileNames = new ArrayList<>(getFileNames(new ArrayList<String>(), yangDirectory.toPath()));
        Boolean modified = false;
        for (String fileName : fileNames) {
            // Parse all the generated yang files
            modified = parseGeneratedYangFile(new File(fileName)) || modified;
        }

        if (modified) {
            // Create the folder if needed

            // Write the OID class to disk

            File oidClassFolder = new File(oidClassFile.getParent());
            if (!oidClassFolder.exists()) {
                oidClassFolder.mkdirs();
            }
            // Write the new lines to the file
            try {
                FileWriter fileWriter = new FileWriter(oidClassFile.getAbsolutePath());
                fileWriter.write(OIDClass);
                fileWriter.close();
            } catch (Exception e) {
                LOG.info(e.getMessage());
            }
        }
    }

    private HashMap<String, String> parseOIDsFromFile(File file) {
        HashMap<String, String> nameToOIDHashMap = new HashMap<>();
        String pattern = "\"(\\w+)\"\\s+\"([\\d\\.]+)\"";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                matcher = regex.matcher(line);
                if (matcher.find()){
                    String name = matcher.group(1).toLowerCase();
                    String oid = matcher.group(2);
                    nameToOIDHashMap.put(name, oid);
                }
            }
            br.close();
        } catch (Exception e) {
            // Ignore the exceptions
        }

        return nameToOIDHashMap;
    }

    private List<String> getFileNames(List<String> fileNames, Path dir){
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
            for (Path path : stream) {
                if(path.toFile().isDirectory())getFileNames(fileNames, path);
                else {
                    fileNames.add(path.toAbsolutePath().toString());
                }
            }
            stream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return fileNames;
    }

    private String getAnnotation(String oid) {
        return String.format("        @OID(value = \"%s\")", oid);
    }

    private Boolean parseGeneratedYangFile(File file) {
        ArrayList<String> newLines = new ArrayList<>();

        String pattern = "public\\s[\\w\\.]+\\s[g|s]et(\\w+)\\(";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;
        Boolean isModified = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                matcher = regex.matcher(line);
                if (matcher.find()){
                    String name = matcher.group(1).toLowerCase();
                    if (nameToOIDHashMap.containsKey(name)) {
                        isModified = true;
                        newLines.add(getAnnotation(nameToOIDHashMap.get(name)));
                    } else {

                    }
                }
                newLines.add(line);
            }
            br.close();

            if (isModified) {
                // Write the new lines to the file
                LOG.info(String.format("Writing changes to file %s", file.getName()));
                FileWriter fileWriter = new FileWriter(file.getAbsolutePath());

                Boolean lastLineIsImport = false;
                Boolean isImportLine;
                for (String newLine : newLines) {
                    // Include the import statement for the OID tag as the last import

                    isImportLine = newLine.trim().startsWith("import ");
                    if (!isImportLine && lastLineIsImport) {
                        fileWriter.write(importString + "\n");
                    }

                    lastLineIsImport = isImportLine;
                    fileWriter.write(newLine + "\n");
                }

                fileWriter.close();
            }
            return isModified;
        } catch (Exception e) {
            // Ignore the exceptions
        }
        return false;
    }
}

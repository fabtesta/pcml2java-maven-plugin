package com.github.fabtesta.pcml2java.maven;

import com.github.fabtesta.pcml2java.PCML2Java;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal gensrc
 * @phase generate-sources
 */
public class PCML2JavaGenerate extends AbstractMojo {

    /**
     * The package name for the generated classes
     * 
     * @parameter
     * @required
     */
    private String packageName;

    /**
     * The source folder to scan for PCML-Files
     * 
     * @parameter
     * @required
     */
    private String sourceFolder;

    /**
     * Should we generate constants for each field?
     * 
     * @parameter
     */
    private boolean generateConstants;

    /**
     * automatically generate @Size(max=?) for each field.
     * 
     * @parameter
     */
    private boolean beanValidation;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        PCML2Java pcml2Java = new PCML2Java();
        pcml2Java.setGenerateConstants(generateConstants);
        pcml2Java.setBeanValidation(beanValidation);

        getLog().info("generating for " + packageName + " from " + sourceFolder);
        if (generateConstants) {
            getLog().info("generating constants for all fields");
        }
        if (beanValidation) {
            getLog().info("annotating supported fields with @Size(max=?) Bean-Validation");
        }
        pcml2Java.createJavaClassesForPCMLFiles(packageName, sourceFolder);
    }
}

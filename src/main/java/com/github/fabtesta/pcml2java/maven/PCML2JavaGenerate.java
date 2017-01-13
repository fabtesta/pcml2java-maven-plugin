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

    /**
     * The superclass for all PCML request beans
     *
     * @parameter
     */
    private String requestSuperClass;

    /**
     * The superclass for all PCML response beans
     *
     * @parameter
     */
    private String responseSuperClass;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        PCML2Java pcml2Java = new PCML2Java(generateConstants, beanValidation,packageName, sourceFolder, requestSuperClass, responseSuperClass);
        getLog().info("generating for " + packageName + " from " + sourceFolder);
        if (generateConstants) {
            getLog().info("generating constants for all fields");
        }
        if (beanValidation) {
            getLog().info("annotating supported fields with @Size(max=?) Bean-Validation");
        }

        if (!requestSuperClass.isEmpty()) {
            getLog().info("requests superclass " + requestSuperClass);
        }

        if (!responseSuperClass.isEmpty()) {
            getLog().info("responses superclass " + responseSuperClass);
        }

        pcml2Java.createJavaClassesForPCMLFiles();
    }
}

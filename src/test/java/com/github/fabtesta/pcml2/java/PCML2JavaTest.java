package com.github.fabtesta.pcml2.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.github.fabtesta.pcml2java.PCML2Java;

public class PCML2JavaTest {

    @Test
    public void testToCamelCase() {
        String structName = "FABTESTA_CODE";
        String expected = "fabtestaCode";
        String camelCase = PCML2Java.toLowerCamelCase(structName);
        assertEquals(expected, camelCase);
    }

    @Test
    public void testCreateJavaClassesForPCMLFiles() {
        PCML2Java beanGenerator = new PCML2Java();

        beanGenerator.setBeanValidation(true);
        beanGenerator.setGenerateConstants(true);

        String packageName = "com.github.fabtesta.test";
        String sourceFolder = "src";
        beanGenerator.createJavaClassesForPCMLFiles(packageName, sourceFolder);
        assertTrue(new File("target/generated-sources/com/github/fabtesta/test/LetterCode.java").exists());
        assertTrue(new File("target/generated-sources/com/github/fabtesta/test/LetterCodeService.java").exists());
    }

}
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

        String packageName = "com.github.fabtesta.test";
        String sourceFolder = "src";
        String requestSuperClass = "com.github.fabtesta.test.ServiceRequest";
        String responseSuperClass = "com.github.fabtesta.test.ServiceResponse";

        PCML2Java beanGenerator = new PCML2Java(true,true,packageName, sourceFolder,requestSuperClass, responseSuperClass);

        beanGenerator.createJavaClassesForPCMLFiles();
        assertTrue(new File("target/generated-sources/com/github/fabtesta/test/lettercodeservice/LetterCode.java").exists());
        assertTrue(new File("target/generated-sources/com/github/fabtesta/test/lettercodeservice/LetterCodeServiceRequest.java").exists());
        assertTrue(new File("target/generated-sources/com/github/fabtesta/test/lettercodeservice/LetterCodeServiceResponse.java").exists());
    }

}

package de.twimbee.pcml2.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.twimbee.pcml2java.PCML2Java;

public class PCML2JavaTest {

    @Test
    public void testToCamelCase() {
        String structName = "BUCHER_CODE";
        String expected = "bucherCode";
        String camelCase = PCML2Java.toLowerCamelCase(structName);
        assertEquals(expected, camelCase);
    }

    @Test
    public void testCreateJavaClassesForPCMLFiles() {
        PCML2Java beanGenerator = new PCML2Java();

        beanGenerator.setBeanValidation(true);
        beanGenerator.setGenerateConstants(true);

        String packageName = "de.twimbee.test";
        String sourceFolder = "src";
        beanGenerator.createJavaClassesForPCMLFiles(packageName, sourceFolder);
        assertTrue(new File("target/generated-sources/de/twimbee/test/LetterCode.java").exists());
        assertTrue(new File("target/generated-sources/de/twimbee/test/LetterCodeService.java").exists());
    }

}

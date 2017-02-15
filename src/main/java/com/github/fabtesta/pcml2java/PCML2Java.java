package com.github.fabtesta.pcml2java;

import com.google.common.base.CaseFormat;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.sun.codemodel.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

public class PCML2Java {

    private static final String GENERATED_SOURCES_DIR = "target/generated-sources";

    private static final List<Class<?>> sizeAnnotationTypes = new ArrayList<>();

    static {
        sizeAnnotationTypes.add(CharSequence.class);
        sizeAnnotationTypes.add(Collection.class);
        sizeAnnotationTypes.add(Map.class);
    }

    private boolean generateConstants;
    private boolean beanValidation;
    private String packageName;
    private String sourceDirectory;
    private String requestSuperClass;
    private String responseSuperClass;

    public static final String InnerBean = "InnerBean";
    public static final String RequestBean = "RequestBean";
    public static final String ResponseBean = "ResponseBean";

    public PCML2Java(boolean generateConstants, boolean beanValidation, String packageName, String sourceDirectory, String requestSuperClass, String responseSuperClass) {
        this.generateConstants = generateConstants;
        this.beanValidation = beanValidation;
        this.packageName = packageName;
        this.sourceDirectory = sourceDirectory;
        this.requestSuperClass = requestSuperClass;
        this.responseSuperClass = responseSuperClass;
    }

    public void createJavaClassesForPCMLFiles() {
        try {
            // TODO outputDir configurable? maven restrictions?
            File destDir = new File(GENERATED_SOURCES_DIR);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            // delete all files in destination folder
            File destDeleteDir = new File(destDir, packageName.replace(".", "/"));
            if (destDeleteDir.exists()) {
                File[] filesToDelete = destDeleteDir.listFiles();
                for (File file : filesToDelete) {
                    file.delete();
                }
            }

            // scan for all *pcml files
            List<File> pcmlFiles = findPCMLFilesInDirectory(sourceDirectory);
            for (File file : pcmlFiles) {

                FileInputStream fis = new FileInputStream(file);

                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(fis);

                Element rootNode = doc.getRootElement();

                //Must generated structs classes first
                List<Element> structs = rootNode.getChildren("struct");
                for (Element struct : structs) {
                    createJavaClass(struct, packageName, destDir, InnerBean);
                }

                Element program = rootNode.getChild("program");
                createJavaClass(program, packageName, destDir, RequestBean);
                createJavaClass(program, packageName, destDir, ResponseBean);
            }

        } catch (JClassAlreadyExistsException | IOException | ClassNotFoundException | JDOMException e) {
            throw new RuntimeException("Could not genereate JavaBeans from PCML-Files", e);
        }
    }

    private void createJavaClass(Element node, String packageName, File destDir, String pcmlBeanType) throws JClassAlreadyExistsException,
            IOException, ClassNotFoundException {
        String nodeName = node.getAttributeValue("name");

        String classPostFix = "";
        String usageTarget = "";
        if (pcmlBeanType == RequestBean) {
            classPostFix = "Request";
            usageTarget = "input";
        }
        else if (pcmlBeanType == ResponseBean) {
            classPostFix = "Response";
            usageTarget = "output";
        }
        else if (pcmlBeanType == InnerBean) {
            classPostFix = "";
            usageTarget = "inherit";
        }

        String className = toTitleCamelCase(nodeName)+classPostFix;

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass myClass = codeModel._class(packageName + "." + className);

        if(!requestSuperClass.isEmpty() && pcmlBeanType == RequestBean) {
            JCodeModel tmpCodeModel = new JCodeModel();
            JDefinedClass superClass = tmpCodeModel._class(requestSuperClass);
            myClass._extends(superClass);
        }

        if(!responseSuperClass.isEmpty() && pcmlBeanType == ResponseBean) {
            JCodeModel tmpCodeModel = new JCodeModel();
            JDefinedClass superClass = tmpCodeModel._class(responseSuperClass);
            myClass._extends(superClass);
        }

        final String finalUsageTarget = usageTarget;
        Predicate<Element> predicate = input -> input.getAttributeValue("usage").equals(finalUsageTarget);

        Collection<Element> children = Collections2.filter(node.getChildren("data"), predicate);

        // First generate the constants
        if (this.generateConstants) {
            for (Element dataField : children) {
                String nameRpg = dataField.getAttributeValue("name");
                //String name = toTitleCamelCase(nameRpg);

                JFieldVar constant = myClass.field(JMod.STATIC + JMod.PUBLIC + JMod.FINAL, String.class, nameRpg);
                constant.init(JExpr.lit(nameRpg));
            }
        }

        // Then generate the fields
        for (Element dataField : children) {
            String usagePrefix = dataField.getAttributeValue("usage").equals("inherit") ? "" : dataField.getAttributeValue("usage") + "_";
            String nameRpg = dataField.getAttributeValue("name");
            String name = toLowerCamelCase(usagePrefix + nameRpg);

            JType fieldType = null;
            JClass fieldArrayClass = null;
            Class<?> primitiveType = null;
            if (!dataField.getAttributeValue("type").equalsIgnoreCase("struct")) {
                primitiveType = mapToJavaType(dataField.getAttributeValue("type"),
                        dataField.getAttributeValue("length"), dataField.getAttributeValue("precision"));
                fieldType = codeModel.ref(primitiveType);
            } else {
                String structName = dataField.getAttributeValue("struct");
                String structClassName = toTitleCamelCase(structName);
                boolean structIsArray = Integer.parseInt(dataField.getAttributeValue("count")) > 1;
                JCodeModel tmpCodeModel = new JCodeModel();
                fieldType = tmpCodeModel._class(packageName + "." + structClassName);
                if(structIsArray)
                {
                    JClass genericArray = tmpCodeModel.ref(ArrayList.class);
                    fieldArrayClass = genericArray.narrow(fieldType);
                }
            }
            JFieldVar field = null;
            if(fieldArrayClass != null)
                field = myClass.field(JMod.PRIVATE, fieldArrayClass, name);
            else
                field = myClass.field(JMod.PRIVATE, fieldType, name);

            // @javax.validation.constraints.Size(min = 3, max = 3)
            if (beanValidation && primitiveType != null && isSizeAnnotationSupported(primitiveType)) {
                JAnnotationUse sizeValidationAnnotation = field.annotate(javax.validation.constraints.Size.class);
                sizeValidationAnnotation.param("max", Integer.parseInt(dataField.getAttributeValue("length")));
            }

            String capitalName = toTitleCamelCase(usagePrefix + nameRpg);
            String getterName = "get" + capitalName;
            JMethod getter = myClass.method(JMod.PUBLIC, fieldArrayClass != null ? fieldArrayClass : fieldType, getterName);
            getter.body()._return(field);

            String setterName = "set" + capitalName;
            JMethod setter = myClass.method(JMod.PUBLIC, void.class, setterName);
            setter.param(fieldArrayClass != null ? fieldArrayClass : fieldType, name);
            setter.body().assign(JExpr._this().ref(name), JExpr.ref(name));

        }

        codeModel.build(destDir);
    }

    private static boolean isSizeAnnotationSupported(Class<?> fieldType) {
        for (Class<?> c : sizeAnnotationTypes) {
            if (c.isAssignableFrom(fieldType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the correct Java class for the given parameters
     * <p>
     * <table border=1>
     * <tr valign=top>
     * <th>PCML Description</th>
     * <th>Object Returned</th>
     * </tr>
     * <tr valign=top>
     * <td><code>type=char</td>
     * <td><code>String</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=byte</td>
     * <td><code>byte[]</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=2<br>
     * precision=15</td>
     * <td><code>Short</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=2<br>
     * precision=16</td>
     * <td><code>Integer</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=4<br>
     * precision=31</td>
     * <td><code>Integer</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=4<br>
     * precision=32</td>
     * <td><code>Long</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=8<br>
     * precision=63</td>
     * <td><code>Long</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=int<br>
     * length=8<br>
     * precision=64</td>
     * <td><code>BigInteger</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=packed</td>
     * <td><code>BigDecimal</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=zoned</td>
     * <td><code>BigDecimal</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=float<br>
     * length=4</td>
     * <td><code>Float</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=float<br>
     * length=8</td>
     * <td><code>Double</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=date</td>
     * <td><code>java.sql.Date</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=time</td>
     * <td><code>java.sql.Time</code></td>
     * </tr>
     * <tr valign=top>
     * <td><code>type=timestamp</td>
     * <td><code>java.sql.Timestamp</code></td>
     * </tr>
     * </table>
     *
     * @param attributeValue
     * @param attributeValue2
     * @return
     */
    private static Class<?> mapToJavaType(String type, String lengthString, String precisionString) {
        Integer length = lengthString != null && !lengthString.isEmpty() ? Integer.valueOf(lengthString) : 4;
        Integer precision = precisionString != null && !precisionString.isEmpty() ? Integer.valueOf(precisionString)
                : 32;
        switch (type) {
            case "char":
                return String.class;
            case "byte":
                return byte[].class;
            case "int":
                switch (length) {
                    case 2:
                        if (precision < 16) {
                            return Short.class;
                        } else {
                            return Integer.class;
                        }
                    case 4:
                        if (precision < 32) {
                            return Integer.class;
                        } else {
                            return Long.class;
                        }
                    case 8:
                        if (precision < 64) {
                            return Long.class;
                        } else {
                            return BigInteger.class;
                        }
                    default:
                        return Long.class;
                }
            case "packed":
                return BigDecimal.class;
            case "zoned":
                return BigDecimal.class;
            case "float":
                switch (length) {
                    case 4:
                        return Float.class;
                    case 8:
                        return Double.class;
                    default:
                        return Double.class;
                }
            case "date":
                return Date.class;
            case "time":
                return Time.class;
            case "timestamp":
                return Timestamp.class;
            default:
                return String.class;
        }
    }

//    private static JType mapToJType(String type, JCodeModel codeModel) {
//        switch (type) {
//            case "char":
//                return codeModel.directClass("String");
//            case "byte":
//                return byte[].class;
//            case "int":
//                switch (length) {
//                    case 2:
//                        if (precision < 16) {
//                            return Short.class;
//                        } else {
//                            return Integer.class;
//                        }
//                    case 4:
//                        if (precision < 32) {
//                            return Integer.class;
//                        } else {
//                            return Long.class;
//                        }
//                    case 8:
//                        if (precision < 64) {
//                            return Long.class;
//                        } else {
//                            return BigInteger.class;
//                        }
//                    default:
//                        return Long.class;
//                }
//            case "packed":
//                return BigDecimal.class;
//            case "zoned":
//                return BigDecimal.class;
//            case "float":
//                switch (length) {
//                    case 4:
//                        return Float.class;
//                    case 8:
//                        return Double.class;
//                    default:
//                        return Double.class;
//                }
//            case "date":
//                return Date.class;
//            case "time":
//                return Time.class;
//            case "timestamp":
//                return Timestamp.class;
//            default:
//                return String.class;
//        }
//    }

    /**
     * Converts a string from UNDERSCORE_CASE to camelCase
     *
     * @param name
     * @return
     */
    public static String toLowerCamelCase(String name) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
    }

    public static String toTitleCamelCase(String name) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
    }

    /**
     * returns a list of all .pcml files in the classpath
     *
     * @return
     */
    public List<File> findPCMLFilesInClasspath() {
        List<File> result = new LinkedList<>();

        URLClassLoader contextClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        for (URL url : contextClassLoader.getURLs()) {
            File file = new File(url.getPath());
            result.addAll(handleFile(file));
        }

        return result;
    }

    public List<File> findPCMLFilesInDirectory(String directory) {
        return handleDirectory(new File(directory));
    }

    private List<File> handleFile(File file) {
        List<File> result = new LinkedList<>();
        if (file.isDirectory()) {
            result.addAll(handleDirectory(file));
        } else if (file.isFile() && file.getName().endsWith(".pcml")) {
            result.add(file);
        }
        return result;
    }

    private List<File> handleDirectory(final File dir) {
        List<File> result = new LinkedList<>();
        for (File file : dir.listFiles()) {
            result.addAll(handleFile(file));
        }
        return result;
    }
}

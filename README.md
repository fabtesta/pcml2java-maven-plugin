pcml2java-maven-plugin
======================
Forked repo from https://github.com/Dr4K4n/pcml2java-maven-plugin.

Generates Java classes from IBM® .PCML-Files (Program Call Markup Language).

Supports both primitive data and struct input/ouput.  
Separates input from output beans.  
Supports superclass inheritance definition for both bean types.

Plugin is available in jitpack repository.  
Latest build [![](https://jitpack.io/v/fabtesta/pcml2java-maven-plugin.svg)](https://jitpack.io/#fabtesta/pcml2java-maven-plugin)  
Travis status [![Build Status](https://travis-ci.org/fabtesta/pcml2java-maven-plugin.svg?branch=master)](https://travis-ci.org/fabtesta/pcml2java-maven-plugin)

## Features
From version 2.1.0 supports struct arrays.  
From version 2.3.0 supports same struct names for different programs.  
From version 2.4.0 supports
1) arrays size validation through java validation api annotation.
2) primitive arrays size single elements validation through pcml2java-validator annotation.

## Basic Usage
1) Include following plugin-block in the pom.xml of your project.  
2) Define a sourceFolder where your PCML-Files are located and a packageName for the generated classes.
3) If your pcml contains array of strings, add pcml2java-validator dependency
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

...

<dependency>
    <groupId>com.github.fabtesta</groupId>
    <artifactId>pcml2java-validator</artifactId>
    <version>1.1.0</version>
</dependency>
    
...

<build>
	<plugins>
		<plugin>
			<groupId>com.github.fabtesta</groupId>
	    	<artifactId>pcml2java-maven-plugin</artifactId>
	    	<version>2.4.0</version>
			<configuration>
				<sourceFolder>src/main/resources</sourceFolder>
				<packageName>com.github.fabtesta.test</packageName>
				<generateConstants>true</generateConstants>
				<beanValidation>true</beanValidation>
				<requestSuperClass>com.github.fabtesta.test.ServiceRequest</requestSuperClass>
				<responseSuperClass>com.github.fabtesta.test.ServiceResponse</responseSuperClass>
			</configuration>
			<executions>
				<execution>
					<goals>
						<goal>gensrc</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

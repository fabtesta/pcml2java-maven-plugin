pcml2java-maven-plugin
======================
Forked repo from https://github.com/Dr4K4n/pcml2java-maven-plugin.

Generates Java classes from IBM® .PCML-Files (Program Call Markup Language).
Supports both primitive data and struct input/ouput.

Plugin is available in maven central repository.

## Basic Usage

Include following plugin-block in the pom.xml of your project. Define a sourceFolder where your PCML-Files are located and a packageName for the generated classes.

```
<build>
	<plugins>
		<plugin>
			groupId>it.fabtesta</groupId>
            <artifactId>pcml2java-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
			<configuration>
				<sourceFolder>src/main/resources</sourceFolder>
				<packageName>it.fabtesta.pcmlbeans</packageName>
				<generateConstants>true</generateConstants>
				<beanValidation>true</beanValidation>
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

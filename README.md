# Audlang Java Core

This project provides a *reference implementation* of the **[Audience Definition Language Specification (Audlang)](https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#audience-definition-language-specification)**.

The Java/[ANTLR](https://www.antlr.org/) implementation covers the full language feature set including **[comments](https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#15-comments)**.

```xml
		<dependency>
			<groupId>de.calamanari.adl</groupId>
			<artifactId>audlang-java-core</artifactId>
			<version>1.0.0</version>
		</dependency>
```

There is a small set of further dependencies (e.g., SLF4j for logging, JUnit), please refer to this project's POM for details.

Classes in this project support:

 * Expression Parsing
 * Validation
 * Optimization
 * Mapping of attributes and values
 * Formatting (including pretty-print)

:bulb: There is comprehensive logging available (level TRACE) to get an idea what the different features do with an expression.

While this project does not contain any target language converters, the core project provides a couple of foundation classes to simplify implementing custom converters (see [Conversion](./src/main/java/de/calamanari/adl/cnv/README.md)).

Additionally, the core project provides utilities related to the **[Audlang Type Conventions](https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#2-type-conventions)** (see [Type Support](./src/main/java/de/calamanari/adl/cnv/tps/README.md)).

The implementation is based on the **[Dual Layer Language Model (DLLM)](./TheDualLayerLanguageModel)** that decouples the Audlang DSL with its user-centric feature set from the core language features required to execute expressions on any target system.

Converter implementations based on this core project can concentrate on the specific aspects of a target data store resp. its language.

***Read next:***
 * **[Dual Layer Language Model (DLLM)](./TheDualLayerLanguageModel)**
 * **[Presentation Layer Expressions](./src/main/java/de/calamanari/adl/erl/README.md)**
 * **[Core Expressions](./src/main/java/de/calamanari/adl/irl/README.md)**
 * **[Optimization](./src/main/java/de/calamanari/adl/irl/biceps/README.md)**
 * **[Conversion](./src/main/java/de/calamanari/adl/cnv/README.md)**
 * **[Type Support](./src/main/java/de/calamanari/adl/cnv/tps/README.md)**
 




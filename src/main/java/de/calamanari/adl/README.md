#### [Project Overview](../../../../../../README.md)
----

# package adl

![adl-overview](./adl.svg)

----

This package contains some base interfaces and common functionality.

* [AudlangExpression](AudlangExpression.java) is a common interface covering all kinds of expressions in this project, so they can be treated in the same way (e.g., formatting).
* [AudlangField](AudlangField.java) and [AudlangFieldAware](AudlangFieldAware.java) play a central role when we want to extract argument names and values from expressions.
* [CombinedExpressionType](CombinedExpressionType.java) and [SpecialSetType](SpecialSetType.java) allow later differentiation of expressions with identical implementation but different meaning (AND/OR, ALL/NONE).
* [AudlangFormattable](AudlangFormattable.java) abstracts the ability of an element to be (pretty)-printed.
* [FormatUtils](FormatUtils.java), [FormatConstants](FormatConstants.java) and [FormatStyle](FormatStyle.java) abstract the mostly identical formatting (pretty-printing) of expressions.
* [TimeOut](TimeOut.java) and [TimeOutException](TimeOutException.java) relate to any process that might take too long (overload protection).
* [Visit](Visit.java) enumeration lists the two possible events (entry/exit) when visiting an element. We use the VISITOR-pattern frequently in this project when converting expressions.

The sub-packages of this project deal with:
* **[External Representation Layer](./erl/README.md)**: ANTLR-parser implementation and [PlExpression](./erl/PlExpression.java)
* **[Internal Representation Layer](./irl/README.md)**: [CoreExpression](./irl/CoreExpression.java) and all internal mangling/optimization
* **[Conversion](./cnv/README.md)**: Hierarchy of converters and supplementary code to convert an expression from one representation into another.


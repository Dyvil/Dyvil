package dyvil.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;

public enum BaseModifiers implements Modifier
{
	// Visibility Modifiers
	PACKAGE_PRIVATE(0, "package private"),
	PRIVATE(Modifiers.PRIVATE, "private"),
	PRIVATE_PROTECTED(Modifiers.PROTECTED, "private protected"),
	PROTECTED(Modifiers.PROTECTED, "protected"),
	PACKAGE_PROTECTED(Modifiers.PUBLIC, "package protected"),
	PUBLIC(Modifiers.PUBLIC, "public"),
	INTERNAL(Modifiers.INTERNAL, "internal"),
	// Access Modifiers
	STATIC(Modifiers.STATIC, "static"),
	FINAL(Modifiers.FINAL, "final"),
	ABSTRACT(Modifiers.ABSTRACT, "abstract"),
	// Method Modifiers
	PREFIX(Modifiers.STATIC, "prefix"),
	INFIX(Modifiers.INFIX, "infix"),
	POSTFIX(Modifiers.INFIX, "postfix"),
	IMPLICIT(Modifiers.IMPLICIT, "implicit"),
	OVERRIDE(Modifiers.OVERRIDE, "override"),
	INLINE(Modifiers.INLINE, "inline"),
	SYNCHRONIZED(Modifiers.SYNCHRONIZED, "synchronized"),
	EXTENSION(Modifiers.EXTENSION, "extension"),
	// Class Modifiers
	SEALED(Modifiers.SEALED, "sealed"),
	CASE(Modifiers.CASE_CLASS, "case"),
	// Field Modifiers
	CONST(Modifiers.CONST, "const"),
	LAZY(Modifiers.LAZY, "lazy");

	private final int    intValue;
	private final String name;

	BaseModifiers(int intValue, String name)
	{
		this.intValue = intValue;
		this.name = name;
	}

	@Override
	public int intValue()
	{
		return this.intValue;
	}

	@Override
	public void toString(StringBuilder builder)
	{
		builder.append(this.name);
	}
}

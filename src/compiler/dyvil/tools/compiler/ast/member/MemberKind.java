package dyvil.tools.compiler.ast.member;

import dyvil.reflect.Modifiers;

public enum MemberKind
{
	HEADER("header", Modifiers.ACCESS_MODIFIERS),
	CLASS("class", Modifiers.CLASS_MODIFIERS),
	METHOD("method", Modifiers.METHOD_MODIFIERS),
	CONSTRUCTOR("constructor", Modifiers.CONSTRUCTOR_MODIFIERS),
	INITIALIZER("initializer", Modifiers.INITIALIZER_MODIFIERS),
	FIELD("field", Modifiers.FIELD_MODIFIERS),
	PROPERTY("property", Modifiers.METHOD_MODIFIERS),
	METHOD_PARAMETER("parameter", Modifiers.PARAMETER_MODIFIERS),
	CLASS_PARAMETER("classparameter", Modifiers.CLASS_PARAMETER_MODIFIERS),
	VARIABLE("variable", Modifiers.VARIABLE_MODIFIERS);

	private final String name;
	private final int allowedModifiers;

	MemberKind(String name, int allowedModifiers)
	{
		this.name = name;
		this.allowedModifiers = allowedModifiers;
	}

	public String getName()
	{
		return this.name;
	}

	public int getAllowedModifiers()
	{
		return this.allowedModifiers;
	}
}

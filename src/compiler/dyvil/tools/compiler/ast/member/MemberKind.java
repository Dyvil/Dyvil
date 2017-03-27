package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.ast.modifiers.Modifier;

import static dyvil.reflect.Modifiers.*;

public enum MemberKind
{
	HEADER("header", BaseModifiers.ACCESS_MODIFIERS, PUBLIC),
	TYPE_ALIAS("typealias", BaseModifiers.ACCESS_MODIFIERS, PUBLIC),
	CLASS("class", BaseModifiers.CLASS_MODIFIERS, PUBLIC),
	METHOD("method", BaseModifiers.METHOD_MODIFIERS, PUBLIC),
	CONSTRUCTOR("constructor", BaseModifiers.CONSTRUCTOR_MODIFIERS, PUBLIC),
	INITIALIZER("initializer", BaseModifiers.INITIALIZER_MODIFIERS, PRIVATE),
	FIELD("field", BaseModifiers.FIELD_MODIFIERS, PROTECTED),
	PROPERTY("property", BaseModifiers.METHOD_MODIFIERS, PUBLIC),
	METHOD_PARAMETER("parameter", BaseModifiers.PARAMETER_MODIFIERS, 0),
	CLASS_PARAMETER("classparameter", BaseModifiers.CLASS_PARAMETER_MODIFIERS, PROTECTED),
	VARIABLE("variable", BaseModifiers.VARIABLE_MODIFIERS, 0);

	private final String name;
	private final String allowedModifiers;
	private final int    defaultAccess;

	MemberKind(String name, String allowedModifiers, int defaultAccess)
	{
		this.name = name;
		this.allowedModifiers = allowedModifiers;
		this.defaultAccess = defaultAccess;
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isModifierAllowed(Modifier modifier)
	{
		final String str = modifier.toString();
		final int index = this.allowedModifiers.indexOf(str);

		return index >= 0 && (this.allowedModifiers.charAt(index + str.length()) == ',');
	}

	public int getDefaultAccess(IMember member)
	{
		if (this == FIELD && ((IField) member).getEnclosingClass().isInterface())
		{
			return PUBLIC;
		}
		return this.defaultAccess;
	}
}

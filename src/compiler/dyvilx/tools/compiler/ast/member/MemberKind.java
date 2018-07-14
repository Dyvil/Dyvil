package dyvilx.tools.compiler.ast.member;

import dyvilx.tools.compiler.ast.attribute.Attribute;
import dyvilx.tools.compiler.ast.attribute.modifiers.BaseModifiers;
import dyvilx.tools.compiler.ast.field.IField;

import static dyvil.reflect.Modifiers.*;

public enum MemberKind
{
	// Header Elements
	HEADER("header", BaseModifiers.ACCESS_MODIFIERS, PUBLIC),
	TYPE_ALIAS("typealias", BaseModifiers.ACCESS_MODIFIERS, PUBLIC),
	// Classes
	CLASS("class", BaseModifiers.CLASS_MODIFIERS, PUBLIC),
	INTERFACE("interface", BaseModifiers.INTERFACE_MODIFIERS, PUBLIC),
	TRAIT("trait", BaseModifiers.TRAIT_MODIFIERS, PUBLIC),
	ANNOTATION("annotation", BaseModifiers.ANNOTATION_MODIFIERS, PUBLIC),
	ENUM("enum", BaseModifiers.ENUM_MODIFIERS, PUBLIC),
	OBJECT("object", BaseModifiers.OBJECT_MODIFIERS, PUBLIC),
	// Members,
	METHOD("method", BaseModifiers.METHOD_MODIFIERS, PUBLIC),
	CONSTRUCTOR("constructor", BaseModifiers.CONSTRUCTOR_MODIFIERS, PUBLIC),
	INITIALIZER("initializer", BaseModifiers.INITIALIZER_MODIFIERS, PRIVATE),
	FIELD("field", BaseModifiers.FIELD_MODIFIERS, PROTECTED),
	PROPERTY("property", BaseModifiers.METHOD_MODIFIERS, PUBLIC),
	// Variables and Parameters
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

	public boolean isAttributeAllowed(Attribute attribute)
	{
		if (attribute.flags() == 0)
		{
			return true;
		}

		final String str = attribute.toString();
		final int index = this.allowedModifiers.indexOf(str);

		return index >= 0 && (this.allowedModifiers.charAt(index + str.length()) == ',');
	}

	public int getDefaultAccess(Member member)
	{
		if (this == FIELD && ((IField) member).getEnclosingClass().isInterface())
		{
			return PUBLIC;
		}
		return this.defaultAccess;
	}
}

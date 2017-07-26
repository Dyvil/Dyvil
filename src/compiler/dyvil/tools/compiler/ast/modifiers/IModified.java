package dyvil.tools.compiler.ast.modifiers;

import dyvil.reflect.Modifiers;

public interface IModified
{
	ModifierSet getModifiers();

	void setModifiers(ModifierSet modifiers);

	default boolean hasModifier(int modifier)
	{
		final ModifierSet modifiers = this.getModifiers();
		return modifiers != null && modifiers.hasIntModifier(modifier);
	}

	default boolean isAbstract()
	{
		return this.hasModifier(Modifiers.ABSTRACT);
	}

	default boolean isAnnotation()
	{
		return this.hasModifier(Modifiers.ANNOTATION);
	}

	default boolean isDefault()
	{
		return this.hasModifier(Modifiers.DEFAULT);
	}

	default boolean isEnumConstant()
	{
		return this.hasModifier(Modifiers.ENUM_CONST);
	}

	default boolean isImplicit()
	{
		return this.hasModifier(Modifiers.IMPLICIT);
	}

	default boolean isInterface()
	{
		return this.hasModifier(Modifiers.INTERFACE);
	}

	default boolean isObject()
	{
		return this.hasModifier(Modifiers.OBJECT);
	}

	default boolean isVarargs()
	{
		return this.hasModifier(Modifiers.VARARGS);
	}
}

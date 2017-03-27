package dyvil.tools.compiler.ast.modifiers;

public interface IModified
{
	ModifierSet getModifiers();

	void setModifiers(ModifierSet modifiers);

	default boolean hasModifier(int mod)
	{
		final ModifierSet modifiers = this.getModifiers();
		return modifiers != null && modifiers.hasIntModifier(mod);
	}
}

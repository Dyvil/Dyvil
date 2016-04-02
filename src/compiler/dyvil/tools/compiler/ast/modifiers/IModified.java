package dyvil.tools.compiler.ast.modifiers;

public interface IModified
{
	void setModifiers(ModifierSet modifiers);
	
	ModifierSet getModifiers();
	
	default boolean hasModifier(int mod)
	{
		final ModifierSet modifiers = this.getModifiers();
		return modifiers != null && modifiers.hasIntModifier(mod);
	}
}

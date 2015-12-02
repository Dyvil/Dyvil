package dyvil.tools.compiler.ast.modifiers;

public interface IModified
{
	void setModifiers(ModifierSet modifiers);
	
	ModifierSet getModifiers();
	
	default boolean hasModifier(int mod)
	{
		ModifierSet modifierSet = this.getModifiers();
		if (modifierSet == null)
		{
			return mod == 0;
		}
		return modifierSet.hasIntModifier(mod);
	}
}

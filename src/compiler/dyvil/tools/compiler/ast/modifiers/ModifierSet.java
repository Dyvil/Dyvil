package dyvil.tools.compiler.ast.modifiers;

import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ModifierSet
{
	boolean hasModifier(Modifier modifier);

	boolean hasIntModifier(int modifier);

	void addModifier(Modifier modifier);

	void addIntModifier(int modifier);

	void check(MarkerList markers);

	int toFlags();

	static void write(ModifierSet modifierSet, DataOutput output) throws IOException
	{
		output.writeInt(modifierSet.toFlags());
	}

	static ModifierSet read(DataInput input) throws IOException
	{
		return new FlagModifierSet(input.readInt());
	}

	void toString(StringBuilder builder);
}

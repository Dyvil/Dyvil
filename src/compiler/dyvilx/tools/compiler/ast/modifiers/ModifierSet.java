package dyvilx.tools.compiler.ast.modifiers;

import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

public interface ModifierSet extends Iterable<Modifier>
{
	default boolean isEmpty()
	{
		return this.count() == 0;
	}

	int count();

	@Override
	Iterator<Modifier> iterator();

	boolean hasModifier(Modifier modifier);

	boolean hasIntModifier(int modifier);

	void addModifier(Modifier modifier);

	void addIntModifier(int modifier);

	void removeIntModifier(int modifier);

	void resolveTypes(IMember member, MarkerList markers);

	int toFlags();

	static void write(ModifierSet modifierSet, DataOutput output) throws IOException
	{
		output.writeInt(modifierSet.toFlags());
	}

	static ModifierSet read(DataInput input) throws IOException
	{
		return new FlagModifierSet(input.readInt());
	}

	void toString(MemberKind memberKind, StringBuilder builder);
}

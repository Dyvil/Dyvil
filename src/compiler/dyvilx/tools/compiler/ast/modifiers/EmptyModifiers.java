package dyvilx.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.EmptyIterator;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class EmptyModifiers implements ModifierSet
{
	public static final EmptyModifiers INSTANCE = new EmptyModifiers();

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public int count()
	{
		return 0;
	}

	@Override
	public Iterator<Modifier> iterator()
	{
		return EmptyIterator.apply();
	}

	@Override
	public boolean hasModifier(Modifier modifier)
	{
		return false;
	}

	@Override
	public boolean hasIntModifier(int modifier)
	{
		return false;
	}

	@Override
	public void addModifier(Modifier modifier)
	{
	}

	@Override
	public void addIntModifier(int modifier)
	{
	}

	@Override
	public void removeIntModifier(int modifier)
	{
	}

	@Override
	public void resolveTypes(IMember member, MarkerList markers)
	{
	}

	@Override
	public int toFlags()
	{
		return 0;
	}

	@Override
	public void toString(MemberKind memberKind, StringBuilder builder)
	{
	}
}

package dyvil.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.EmptyIterator;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class EmptyModifiers implements ModifierSet
{
	public static final EmptyModifiers INSTANCE = new EmptyModifiers();

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
	public void check(MarkerList markers)
	{

	}

	@Override
	public int toFlags()
	{
		return 0;
	}

	@Override
	public void toString(StringBuilder builder)
	{

	}
}

package dyvil.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class ModifierList implements ModifierSet
{
	private Modifier[] modifiers = new Modifier[2];
	private int count;
	private int intModifiers;

	public ModifierList()
	{
	}

	public ModifierList(int intModifiers)
	{
		this.intModifiers = intModifiers;
	}

	@Override
	public Iterator<Modifier> iterator()
	{
		return new ArrayIterator<Modifier>(this.modifiers, this.count);
	}

	@Override
	public boolean hasModifier(Modifier modifier)
	{
		for (int i = 0; i < this.count; i++)
		{
			if (this.modifiers[i].equals(modifier))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hasIntModifier(int modifier)
	{
		return (this.intModifiers & modifier) == modifier;
	}

	@Override
	public void addModifier(Modifier modifier)
	{
		int index = this.count++;
		if (index >= this.modifiers.length)
		{
			Modifier[] temp = new Modifier[index + 1];
			System.arraycopy(this.modifiers, 0, temp, 0, index);
			this.modifiers = temp;
		}
		this.modifiers[index] = modifier;

		this.intModifiers |= modifier.intValue();
	}

	@Override
	public void addIntModifier(int modifier)
	{
		this.intModifiers |= modifier;
	}

	@Override
	public void check(MarkerList markers)
	{
	}

	@Override
	public int toFlags()
	{
		return this.intModifiers;
	}

	@Override
	public void toString(StringBuilder builder)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.modifiers[i].toString(builder);
			builder.append(' ');
		}
	}
}

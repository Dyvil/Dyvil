package dyvilx.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

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
	public boolean isEmpty()
	{
		return this.count == 0;
	}

	@Override
	public int count()
	{
		return this.count;
	}

	@Override
	public Iterator<Modifier> iterator()
	{
		return new ArrayIterator<>(this.modifiers, this.count);
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
	public void removeIntModifier(int modifier)
	{
		this.intModifiers &= ~modifier;
	}

	@Override
	public void resolveTypes(IMember member, MarkerList markers)
	{
		final MemberKind memberKind = member.getKind();
		final int defaultAccess = memberKind.getDefaultAccess(member);
		StringBuilder errorBuilder = null;

		for (int i = 0; i < this.count; i++)
		{
			final Modifier modifier = this.modifiers[i];
			if (!memberKind.isModifierAllowed(modifier))
			{
				if (errorBuilder == null)
				{
					errorBuilder = new StringBuilder();
				}
				else
				{
					errorBuilder.append(", ");
				}
				modifier.toString(errorBuilder);
			}

			final int visibility = modifier.intValue() & Modifiers.VISIBILITY_MODIFIERS;
			if (visibility != 0 && visibility == defaultAccess)
			{
				markers.add(Markers.semantic(member.getPosition(), "modifiers.visibility.default",
				                             Util.memberNamed(member),
				                             ModifierUtil.accessModifiersToString(visibility)));
			}
		}

		if (errorBuilder != null)
		{
			markers.add(Markers.semanticError(member.getPosition(), "modifiers.illegal", Util.memberNamed(member),
			                                  errorBuilder.toString()));
		}
		if ((this.intModifiers & Modifiers.VISIBILITY_MODIFIERS) == 0)
		{
			// If there is no explicit or implicit visibility modifier already, add the default one
			this.intModifiers |= defaultAccess;
		}
	}

	@Override
	public int toFlags()
	{
		return this.intModifiers;
	}

	@Override
	public void toString(MemberKind memberKind, StringBuilder builder)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.modifiers[i].toString(builder);
			builder.append(' ');
		}
	}
}

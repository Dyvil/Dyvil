package dyvil.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
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
	public void check(IMember member, MarkerList markers)
	{
		final MemberKind memberKind = member.getKind();
		final int allowedModifiers = memberKind.getAllowedModifiers();
		StringBuilder stringBuilder = null;

		for (int i = 0; i < this.count; i++)
		{
			final Modifier modifier = this.modifiers[i];
			if ((modifier.intValue() & allowedModifiers) == 0)
			{
				if (stringBuilder == null)
				{
					stringBuilder = new StringBuilder();
				}
				else
				{
					stringBuilder.append(", ");
				}
				modifier.toString(stringBuilder);
			}
		}

		if (stringBuilder != null)
		{
			markers.add(Markers.semanticError(member.getPosition(), "modifiers.illegal", Util.memberNamed(member),
			                                  stringBuilder.toString()));
		}
	}

	public static void checkMethodModifiers(MarkerList markers, IMethod member, int modifiers)
	{
		boolean hasValue = member.getValue() != null;
		boolean isStatic = (modifiers & Modifiers.STATIC) != 0;
		boolean isAbstract = (modifiers & Modifiers.ABSTRACT) != 0;
		boolean isNative = (modifiers & Modifiers.NATIVE) != 0;

		// If the method does not have an implementation and is static
		if (isStatic && isAbstract)
		{
			markers.add(
				Markers.semanticError(member.getPosition(), "modifiers.static.abstract", Util.memberNamed(member)));
		}
		else if (isAbstract && isNative)
		{
			markers.add(
				Markers.semanticError(member.getPosition(), "modifiers.native.abstract", Util.memberNamed(member)));
		}
		else
		{
			if (isStatic)
			{
				if (!hasValue)
				{
					markers.add(Markers.semanticError(member.getPosition(), "modifiers.static.unimplemented",
					                                  Util.memberNamed(member)));
				}
			}
			if (isNative)
			{
				if (!hasValue)
				{
					markers.add(Markers.semanticError(member.getPosition(), "modifiers.native.implemented",
					                                  Util.memberNamed(member)));
				}
			}
			if (isAbstract)
			{
				final IClass enclosingClass = member.getEnclosingClass();
				if (!enclosingClass.isAbstract())
				{
					markers.add(Markers.semanticError(member.getPosition(), "modifiers.abstract.concrete_class",
					                                  Util.memberNamed(member), enclosingClass.getName()));
				}
				if (hasValue)
				{
					markers.add(Markers.semanticError(member.getPosition(), "modifiers.abstract.implemented",
					                                  Util.memberNamed(member)));
				}
			}
		}
		if (!hasValue && !isAbstract && !isNative && !isStatic)
		{
			markers
				.add(Markers.semanticError(member.getPosition(), "modifiers.unimplemented", Util.memberNamed(member)));
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

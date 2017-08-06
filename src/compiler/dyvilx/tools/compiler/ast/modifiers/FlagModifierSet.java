package dyvilx.tools.compiler.ast.modifiers;

import dyvil.collection.iterator.EmptyIterator;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class FlagModifierSet implements ModifierSet
{
	private int flags;

	public FlagModifierSet()
	{
	}

	public FlagModifierSet(int flags)
	{
		this.flags = flags;
	}

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
		return (this.flags & modifier) == modifier;
	}

	@Override
	public void addModifier(Modifier modifier)
	{
		this.flags |= modifier.intValue();
	}

	@Override
	public void addIntModifier(int modifier)
	{
		this.flags |= modifier;
	}

	@Override
	public void removeIntModifier(int modifier)
	{
		this.flags &= ~modifier;
	}

	@Override
	public void resolveTypes(IMember member, MarkerList markers)
	{
	}

	@Override
	public int toFlags()
	{
		return this.flags;
	}

	@Override
	public void toString(MemberKind memberKind, StringBuilder builder)
	{
		ModifierUtil.appendModifiers(this.flags, memberKind, builder);
	}
}

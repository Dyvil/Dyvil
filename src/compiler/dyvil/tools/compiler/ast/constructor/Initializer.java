package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public class Initializer extends Member implements IInitializer
{
	protected IValue value;

	public Initializer(ICodePosition position, ModifierSet modifiers)
	{
		super(position, Names.init, Types.VOID, modifiers);
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.CONSTRUCTOR;
	}
}

package dyvil.tools.compiler.ast.type.builtin;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeDelegate;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ResolvedTypeDelegate extends TypeDelegate
{
	protected ICodePosition position;

	public ResolvedTypeDelegate(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	@Override
	protected IType wrap(IType type)
	{
		return new ResolvedTypeDelegate(this.position, type);
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public IType atPosition(ICodePosition position)
	{
		return new ResolvedTypeDelegate(position, this.type);
	}

	@Override
	public int typeTag()
	{
		return PRIMITIVE;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.type.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
	}
}

package dyvilx.tools.compiler.ast.type.builtin;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.IUnresolvedType;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class UnknownType implements IUnresolvedType
{
	@Override
	public int typeTag()
	{
		return UNKNOWN;
	}

	@Override
	public Name getName()
	{
		return null;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}

	@Override
	public boolean isUninferred()
	{
		return true;
	}

	@Override
	public boolean isGenericType()
	{
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return this;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
	}

	@Override
	public void read(DataInput in) throws IOException
	{
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
	}

	@Override
	public String toString()
	{
		return "<unknown>";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("<unknown>");
	}
}

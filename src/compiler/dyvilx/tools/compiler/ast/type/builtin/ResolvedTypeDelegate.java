package dyvilx.tools.compiler.ast.type.builtin;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeDelegate;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ResolvedTypeDelegate extends TypeDelegate
{
	protected SourcePosition position;

	public ResolvedTypeDelegate(SourcePosition position, IType type)
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
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IType atPosition(SourcePosition position)
	{
		return new ResolvedTypeDelegate(position, this.type);
	}

	@Override
	public int typeTag()
	{
		return this.type.typeTag();
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.type.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type.read(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
	}
}

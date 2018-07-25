package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IAccessible;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public final class ThisExpr implements IValue
{
	protected SourcePosition position;
	protected IType type = Types.UNKNOWN;

	// Metadata
	protected IAccessible getter;

	public ThisExpr(IClass type)
	{
		this.type = type.getThisType();
		this.getter = type.getAccessibleThis(type);
	}

	public ThisExpr(IType type)
	{
		this.type = type;
	}

	public ThisExpr(IType type, IAccessible getter)
	{
		this.type = type;
		this.getter = getter;
	}

	public ThisExpr(SourcePosition position)
	{
		this.position = position;
	}

	public ThisExpr(SourcePosition position, IType type, MarkerList markers, IContext context)
	{
		this.position = position;
		this.type = type;
		this.resolveTypes(markers, context);
		this.checkTypes(markers, context);
	}

	public ThisExpr(SourcePosition position, IType type, IAccessible getter)
	{
		this.position = position;
		this.type = type;
		this.getter = getter;
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
	public int valueTag()
	{
		return THIS;
	}

	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public Object toObject()
	{
		return null;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);

		if (this.type != Types.UNKNOWN)
		{
			// Replace e.g. this<List> with this<List<T>> in class List<type T>
			final IClass iclass = this.type.getTheClass();
			if (iclass != null)
			{
				this.type = iclass.getThisType();
			}
			return this;
		}

		final IType thisType = context.getThisType();
		if (thisType != null)
		{
			this.type = thisType;
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "this.access.unresolved"));
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.SUPER_TYPE);
		if (!context.isThisAvailable())
		{
			markers.add(Markers.semanticError(this.position, "this.access.static"));
			return;
		}

		if (!this.type.isResolved())
		{
			return;
		}

		final IClass theClass = this.type.getTheClass();
		this.getter = context.getAccessibleThis(theClass);
		if (this.getter == null)
		{
			markers.add(Markers.semanticError(this.position, "this.instance", this.type));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.getter.writeGet(writer);

		if (type != null)
		{
			this.type.writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("this");

		if (this.type != Types.UNKNOWN)
		{
			buffer.append('<');
			this.type.toString(prefix, buffer);
			buffer.append('>');
		}
	}
}

package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.StringInterpolationExpr;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class ScopeDirective extends WriteCall
{
	protected IValue block;

	public ScopeDirective()
	{
		this.value = new StringInterpolationExpr();
	}

	@Override
	public int valueTag()
	{
		return SCOPE_DIRECTIVE;
	}

	// Getters and Setters

	@Override
	public IValue getValue()
	{
		if (this.value instanceof StringInterpolationExpr)
		{
			final ArgumentList values = ((StringInterpolationExpr) this.value).getValues();
			return values.isEmpty() ? null : values.get(0);
		}
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = new StringInterpolationExpr(value);
	}

	public IValue getBlock()
	{
		return this.block;
	}

	public void setBlock(IValue block)
	{
		this.block = block;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		if (this.block != null)
		{
			this.block.resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		if (this.block != null)
		{
			this.block = this.block.resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		if (this.block != null)
		{
			this.block.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		if (this.block != null)
		{
			this.block.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		super.foldConstants();
		if (this.block != null)
		{
			this.block = this.block.foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);
		if (this.block != null)
		{
			this.block.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.getValue() != null)
		{
			super.writeExpression(writer, type);
		}

		if (this.block != null)
		{
			this.block.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#');

		final IValue value = this.getValue();
		if (value != null)
		{
			buffer.append('(');
			value.toString(indent, buffer);
			buffer.append(')');

			if (this.block != null)
			{
				buffer.append(' ');
			}
		}
		if (this.block != null)
		{
			this.block.toString(indent, buffer);
		}
	}
}

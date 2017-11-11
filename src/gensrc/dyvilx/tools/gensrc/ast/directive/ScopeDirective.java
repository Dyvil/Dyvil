package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.gensrc.ast.GenSrcValue;
import dyvilx.tools.parsing.marker.MarkerList;

public class ScopeDirective implements GenSrcValue
{
	protected IValue expression;
	protected IValue block;

	public ScopeDirective()
	{
	}

	@Override
	public int valueTag()
	{
		return SCOPE_DIRECTIVE;
	}

	// Getters and Setters

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	public IValue getExpression()
	{
		return this.expression;
	}

	public void setExpression(IValue expression)
	{
		this.expression = expression;
	}

	public IValue getBlock()
	{
		return this.block;
	}

	public void setBlock(IValue block)
	{
		this.block = block;
	}

	//

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.expression != null)
		{
			this.expression.resolveTypes(markers, context);
		}
		if (this.block != null)
		{
			this.block.resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.expression != null)
		{
			this.expression = this.expression.resolve(markers, context);
			// TODO withType
		}
		if (this.block != null)
		{
			this.block = this.block.resolve(markers, context);
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.expression != null)
			this.expression.checkTypes(markers, context);
		if (this.block != null)
			this.block.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.expression != null)
		{
			this.expression.check(markers, context);
		}
		if (this.block != null)
		{
			this.block.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.expression != null)
		{
			this.expression = this.expression.foldConstants();
		}
		if (this.block != null)
		{
			this.block = this.block.foldConstants();
		}
		return null;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.expression != null)
		{
			this.expression = this.expression.cleanup(compilableList, classCompilableList);
		}
		if (this.block != null)
		{
			this.block.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		// TODO
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#');
		if (this.expression != null)
		{
			buffer.append('(');
			this.expression.toString(indent, buffer);
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

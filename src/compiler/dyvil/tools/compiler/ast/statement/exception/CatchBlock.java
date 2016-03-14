package dyvil.tools.compiler.ast.statement.exception;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class CatchBlock implements ITyped, IDefaultContext, IValueConsumer
{
	public ICodePosition position;
	public IType         type;
	public Name          varName;
	public IValue        action;

	protected Variable variable;

	public CatchBlock(ICodePosition position)
	{
		this.position = position;
	}

	public void setAction(IValue value)
	{
		this.action = value;
	}

	public IValue getAction()
	{
		return this.action;
	}

	@Override
	public void setValue(IValue value)
	{
		this.action = value;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.varName == name)
		{
			if (this.variable == null)
			{
				this.variable = new Variable(this.type.getPosition(), this.varName, this.type);
			}
			return this.variable;
		}

		return null;
	}

	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		this.action.resolveTypes(markers, context);
	}

	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);

		context = context.push(this);

		this.action = this.action.resolve(markers, context);

		context.pop();
	}

	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, IType.TypePosition.RETURN_TYPE);

		context = context.push(this);
		this.action.checkTypes(markers, context);
		context.pop();
	}

	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);

		if (!Types.THROWABLE.isSuperTypeOf(this.type))
		{
			final Marker marker = Markers.semanticError(this.position, "try.catch.type.not_throwable");
			marker.addInfo(Markers.getSemantic("exception.type", this.type));
			markers.add(marker);
		}

		context = context.push(this);
		this.action.check(markers, context);
		context.pop();
	}

	public void foldConstants()
	{
		this.type.foldConstants();
		this.action = this.action.foldConstants();
	}

	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);

		context = context.push(this);
		this.action = this.action.cleanup(context, compilableList);
		context.pop();
	}
}

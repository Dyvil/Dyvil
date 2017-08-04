package dyvil.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class BraceAccessExpr implements IValue, IDefaultContext
{
	protected IValue value;
	protected IValue statement;

	// Metadata
	protected SourcePosition position;
	protected IVariable     variable;
	protected FieldAccess   implicitAccess;

	public BraceAccessExpr(SourcePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return BRACE_ACCESS;
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

	public IValue getValue()
	{
		return this.value;
	}

	public void setValue(IValue value)
	{
		this.value = value;
	}

	public IValue getStatement()
	{
		return this.statement;
	}

	public void setStatement(IValue statement)
	{
		this.statement = statement;
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		return type == null ? this.implicitAccess : null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return name == Names.$0 ? this.variable : null;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return variable == this.variable;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return this.statement.getType();
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IValue withType = this.statement.withType(type, typeContext, markers, context);
		if (withType == null)
		{
			return null;
		}

		this.statement = withType;
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		this.statement.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		else
		{
			this.value = context.resolveImplicit(null);
		}

		if (this.value == null)
		{
			markers.add(Markers.semanticError(this.position, "braceaccess.invalid"));
		}
		else
		{
			final IType valueType = this.value.getType();

			final IValue typedValue = this.value.withType(valueType, valueType, markers, context);
			if (typedValue != null)
			{
				this.value = typedValue;
			}

			this.variable = new Variable(Names.$0, this.value.getType());
			this.implicitAccess = new FieldAccess(this.variable);
		}

		context = context.push(this);
		this.statement = this.statement.resolve(markers, context);
		context.pop();

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}

		context = context.push(this);
		this.statement.checkTypes(markers, context);
		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}

		context = context.push(this);
		this.statement.check(markers, context);
		context.pop();
	}

	@Override
	public IValue foldConstants()
	{
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}

		this.statement = this.statement.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}

		this.statement = this.statement.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value != null)
		{
			this.value.toString(prefix, buffer);
		}

		buffer.append('.');
		this.statement.toString(prefix, buffer);
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.value == null)
		{
			this.statement.writeExpression(writer, type);
			return;
		}

		this.value.writeExpression(writer, null);

		final int localCount = writer.localCount();

		this.variable.writeInit(writer, null);
		this.statement.writeExpression(writer, type);

		writer.resetLocals(localCount);
	}
}

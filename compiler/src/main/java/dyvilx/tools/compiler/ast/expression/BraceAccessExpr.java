package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class BraceAccessExpr implements IValue, IDefaultContext
{
	// =============== Fields ===============

	protected IValue value;
	protected IValue statement;

	// --------------- Metadata ---------------

	protected SourcePosition position;
	protected IVariable      variable;

	// =============== Constructors ===============

	public BraceAccessExpr(SourcePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	// =============== Properties ===============

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
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	// =============== Methods ===============

	// --------------- Expression Info ---------------

	@Override
	public int valueTag()
	{
		return BRACE_ACCESS;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	// --------------- Context Resolution ---------------

	@Override
	public IValue resolveImplicit(IType type)
	{
		return type == null ? new FieldAccess(this.variable) : null;
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

	// --------------- Expression Type ---------------

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

	// --------------- Resolution Phases ---------------

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
			final IValue implicitValue = context.resolveImplicit(null);
			if (implicitValue != null)
			{
				this.value = implicitValue.resolve(markers, context);
			}
			else
			{
				markers.add(Markers.semanticError(this.position, "braceaccess.invalid"));
				this.value = DummyValue.INSTANCE;
			}
		}

		final IType valueType = this.value.getType();

		final IValue typedValue = this.value.withType(valueType, valueType, markers, context);
		if (typedValue != null)
		{
			this.value = typedValue;
		}

		this.variable = new Variable(Names.$0, this.value.getType());

		context = context.push(this);
		this.statement = this.statement.resolve(markers, context);
		context.pop();

		return this;
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);

		context = context.push(this);
		this.statement.checkTypes(markers, context);
		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);

		context = context.push(this);
		this.statement.check(markers, context);
		context.pop();
	}

	// --------------- Compilation Phases ---------------

	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();

		this.statement = this.statement.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.value = this.value.cleanup(compilableList, classCompilableList);

		this.statement = this.statement.cleanup(compilableList, classCompilableList);
		return this;
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

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);

		buffer.append('.');
		this.statement.toString(prefix, buffer);
	}
}

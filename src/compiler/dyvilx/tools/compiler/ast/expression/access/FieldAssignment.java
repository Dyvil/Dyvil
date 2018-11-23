package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class FieldAssignment extends AbstractFieldAccess
{
	// =============== Fields ===============

	protected @NonNull IValue value;

	// =============== Constructors ===============

	public FieldAssignment()
	{
	}

	public FieldAssignment(SourcePosition position, IValue receiver, Name name, IValue value)
	{
		this.position = position;
		this.receiver = receiver;
		this.name = name;
		this.value = value;
	}

	public FieldAssignment(SourcePosition position, IValue receiver, IDataMember field, IValue value)
	{
		super(position, receiver, field);
		this.value = value;
	}

	// =============== Properties ===============

	// --------------- Value ---------------

	public IValue getValue()
	{
		return this.value;
	}

	public void setValue(IValue value)
	{
		this.value = value;
	}

	// --------------- Misc. Expression Properties ---------------

	@Override
	public int valueTag()
	{
		return FIELD_ASSIGN;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return this.field != null && this.value.isResolved();
	}

	// =============== Methods ===============

	// --------------- Type Checking ---------------

	@Override
	public boolean isType(IType type)
	{
		return Types.isVoid(type) || this.value.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.value.getTypeMatch(type, implicitContext);
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isVoid(type))
		{
			return this;
		}

		final IValue typedValue = this.value.withType(type, typeContext, markers, context);
		if (typedValue == null)
		{
			return null;
		}

		this.value = typedValue;
		return this;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		this.value.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);

		return super.resolve(markers, context);
	}

	@Override
	protected IValue resolveAsField(IValue receiver, MarkerList markers, IContext context)
	{
		final IDataMember field = ICall.resolveField(context, receiver, this.name);
		if (field == null)
		{
			return null;
		}

		this.receiver = receiver;
		this.field = field;
		this.capture(markers, context);
		return this;
	}

	@Override
	protected IValue resolveAsMethod(IValue receiver, MarkerList markers, IContext context)
	{
		final Name name = Util.addEq(this.name);
		final ArgumentList argument = new ArgumentList(this.value);
		final MethodAssignment assignment = new MethodAssignment(this.position, receiver, name, argument);
		return assignment.resolveCall(markers, context, false);
	}

	@Override
	protected IValue resolveAsType(IContext context)
	{
		return null;
	}

	@Override
	protected void reportResolve(MarkerList markers)
	{
		final Marker marker = Markers.semanticError(this.position, "resolve.field", this.name.unqualified);
		if (this.receiver != null)
		{
			marker.addInfo(Markers.getSemantic("receiver.type", this.receiver.getType()));
		}

		markers.add(marker);
	}

	@Override
	protected void capture(MarkerList markers, IContext context)
	{
		this.field = this.field.capture(context);

		// in case the field was captured, this ensures reference capture takes place
		if (!this.field.setAssigned())
		{
			final Marker marker = Markers.semanticError(this.position, "reference.variable.assignment",
			                                            Util.memberNamed(this.field));
			markers.add(marker);
		}
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		if (this.field != null)
		{
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
			this.value = this.field.checkAssign(markers, context, this.position, this.receiver, this.value);
		}

		this.value.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		this.value.check(markers, context);
	}

	// --------------- Pre-Compilation Phases ---------------

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(compilableList, classCompilableList);
		}
		this.value = this.value.cleanup(compilableList, classCompilableList);
		return this;
	}

	// --------------- Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int lineNumber = this.lineNumber();
		if (type == null || Types.isVoid(type))
		{
			this.field.writeSet(writer, this.receiver, this.value, lineNumber);
			return;
		}

		this.field.writeSetCopy(writer, this.receiver, this.value, lineNumber);
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);
		Formatting.appendSeparator(buffer, "field.assignment", '=');
		this.value.toString(indent, buffer);
	}
}

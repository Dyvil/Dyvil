package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.expression.operator.PostfixCall;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.reference.*;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.NamedType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.transform.SideEffectHelper;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class FieldAccess extends AbstractFieldAccess
{
	// =============== Constructors ===============

	public FieldAccess()
	{
	}

	public FieldAccess(IDataMember field)
	{
		super(field);
	}

	public FieldAccess(SourcePosition position, IValue receiver, IDataMember field)
	{
		super(position, receiver, field);
	}

	public FieldAccess(SourcePosition position, IValue receiver, Name name)
	{
		super(position, receiver, name);
	}

	// =============== Properties ===============

	// --------------- Misc. Expression Properties ---------------

	@Override
	public int valueTag()
	{
		return FIELD_ACCESS;
	}

	@Override
	public boolean isConstantOrField()
	{
		return this.field != null && this.field.hasModifier(Modifiers.CONST);
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.receiver != null && this.receiver.hasSideEffects();
	}

	@Override
	public boolean isResolved()
	{
		return this.field != null && this.field.getType().isResolved();
	}

	// =============== Methods ===============

	// --------------- AST Conversion ---------------

	@Override
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return new FieldAssignment(this.position.to(position), this.receiver, this.name, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, SourcePosition position, MarkerList markers, IContext context,
		SideEffectHelper helper)
	{
		// x (op)= z
		// -> x = x (op) z
		// note that x is only evaluated once

		final IValue fieldReceiver = helper.processValue(this.receiver);
		this.receiver = fieldReceiver;
		return new FieldAssignment(position, fieldReceiver, this.field, rhs);
	}

	@Override
	public IValue toReferenceValue(MarkerList markers, IContext context)
	{
		if (this.field == null)
		{
			return this; // avoid extra error
		}

		final IReference ref;
		if (this.field.isLocal())
		{
			if (!((IVariable) this.field).setReferenceType())
			{
				final Marker marker = Markers.semanticError(this.position, "reference.variable.invalid",
				                                            Util.memberNamed(this.field));
				markers.add(marker);
				return this;
			}

			ref = new VariableReference(this.field.capture(context));
		}
		else if (this.field.isStatic())
		{
			ref = new StaticFieldReference((IField) this.field);
		}
		else
		{
			ref = new InstanceFieldReference(this.receiver, (IField) this.field);
		}

		return new ReferenceOperator(this, ref);
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		if (this.field == null)
		{
			return this; // do not create an extra error
		}

		if (depth == 0 || !this.isConstantOrField())
		{
			return null;
		}

		IValue value = this.field.getValue();
		if (value == null)
		{
			return null;
		}

		return value.toAnnotationConstant(markers, context, depth - 1);
	}

	@Override
	public Marker getAnnotationError()
	{
		return Markers.semantic(this.getPosition(), "annotation.field.not_constant", this.name);
	}

	// --------------- Type Checking ---------------

	@Override
	public boolean isType(IType type)
	{
		return this.field != null && Types.isSuperType(type, this.getType());
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.field == null)
		{
			return MISMATCH;
		}
		return super.getTypeMatch(type, implicitContext);
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.field == null)
		{
			return this; // don't create an extra type error
		}

		return Types.isSuperType(type, this.getType()) ? this : null;
	}

	// --------------- Resolution Phases ---------------

	@Override
	protected IValue resolveAsField(IValue receiver, MarkerList markers, IContext context)
	{
		final IDataMember field = AbstractFieldAccess.resolveField(context, receiver, this.name);
		if (field == null)
		{
			return null;
		}

		if (field.isEnumConstant())
		{
			return new EnumValue(this.position, field);
		}

		this.receiver = receiver;
		this.field = field;
		this.capture(markers, context);
		return this;
	}

	@Override
	protected IValue resolveAsMethod(IValue receiver, MarkerList markers, IContext context)
	{
		// We use PostfixCall because it doesn't do implicit-based resolution nor field-apply resolution
		final PostfixCall call = new PostfixCall(this.position, receiver, this.name);
		return call.resolveCall(markers, context, false);
	}

	@Override
	protected IValue resolveAsType(IContext context)
	{
		final IType parentType;
		if (this.receiver == null)
		{
			parentType = null;
		}
		else if (this.receiver.isClassAccess())
		{
			parentType = this.receiver.getType();
		}
		else
		{
			return null;
		}

		final IType type = new NamedType(this.position, this.name, parentType).resolveType(null, context);
		return type != null ? new ClassAccess(this.position, type) : null;
	}

	@Override
	protected void reportResolve(MarkerList markers)
	{
		final Marker marker = Markers.semanticError(this.position, "method.access.resolve.field", this.name);
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
	}

	// --------------- Pre-Compilation Phases ---------------

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
			return this;
		}
		if (this.field != null && this.field.hasConstantValue())
		{
			// inline constant fields
			return this.field.getValue();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	// --------------- Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int lineNumber = this.lineNumber();
		this.field.writeGet(writer, this.receiver, lineNumber);

		if (type == null)
		{
			type = this.getType();
		}
		else if (Types.isVoid(type))
		{
			type = this.getType();
			this.field.getType().writeCast(writer, type, lineNumber);

			writer.visitInsn(type.getReturnOpcode());
			return;
		}

		this.field.getType().writeCast(writer, type, lineNumber);
	}

	@Override
	public int writeStore(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.field.hasModifier(Modifiers.FINAL) && this.field.isLocal() && this.field instanceof IVariable)
		{
			// no extra read+store necessary for final variables
			return ((IVariable) this.field).getLocalIndex();
		}
		return super.writeStore(writer, type);
	}
}

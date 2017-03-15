package dyvil.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.expression.constant.EnumValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.expression.operator.PostfixCall;
import dyvil.tools.compiler.ast.reference.IReference;
import dyvil.tools.compiler.ast.reference.InstanceFieldReference;
import dyvil.tools.compiler.ast.reference.StaticFieldReference;
import dyvil.tools.compiler.ast.reference.VariableReference;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.SideEffectHelper;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class FieldAccess implements IValue, INamed, IReceiverAccess
{
	protected ICodePosition position;
	protected IValue        receiver;
	protected Name          name;

	// Metadata
	protected IDataMember field;
	protected IType       type;

	public FieldAccess()
	{
	}

	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}

	public FieldAccess(IDataMember field)
	{
		this.field = field;
		this.name = field.getName();
	}

	public FieldAccess(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}

	public FieldAccess(ICodePosition position, IValue instance, IDataMember field)
	{
		this.position = position;
		this.receiver = instance;
		this.field = field;
		this.name = field.getName();
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return FIELD_ACCESS;
	}

	public IValue getInstance()
	{
		return this.receiver;
	}

	public IDataMember getField()
	{
		return this.field;
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

	@Override
	public IValue toAssignment(IValue rhs, ICodePosition position)
	{
		return new FieldAssignment(this.position.to(position), this.receiver, this.name, rhs);
	}

	@Override
	public IValue toCompoundAssignment(IValue rhs, ICodePosition position, MarkerList markers, IContext context,
		                                  SideEffectHelper helper)
	{
		// x op= z
		// -> x = x.op(z)

		final IValue fieldReceiver = helper.processValue(this.receiver);
		this.receiver = fieldReceiver;
		return new FieldAssignment(position, fieldReceiver, this.field, rhs);
	}

	@Override
	public IReference toReference()
	{
		if (this.field == null)
		{
			return null;
		}

		if (!this.field.isLocal())
		{
			if (this.field.hasModifier(Modifiers.STATIC))
			{
				return new StaticFieldReference((IField) this.field);
			}
			else
			{
				return new InstanceFieldReference(this.receiver, (IField) this.field);
			}
		}
		if (this.field instanceof IVariable)
		{
			// We have to pass the actual FieldAccess here because variable access are sometimes replaced with captures
			return new VariableReference(this);
		}
		return null;
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.field == null)
			{
				return Types.UNKNOWN;
			}

			if (this.receiver == null)
			{
				return this.field.getType().asReturnType();
			}

			final ITypeContext typeContext = this.receiver.getType();
			return this.type = this.field.getType().getConcreteType(typeContext).asReturnType();
		}
		return this.type;
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

	@Override
	public boolean isType(IType type)
	{
		return this.field != null && Types.isSuperType(type, this.getType());
	}

	@Override
	public int getTypeMatch(IType type)
	{
		if (this.field == null)
		{
			return MISMATCH;
		}
		return IValue.super.getTypeMatch(type);
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setReceiver(IValue receiver)
	{
		this.receiver = receiver;
	}

	@Override
	public IValue getReceiver()
	{
		return this.receiver;
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

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveReceiver(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveReceiver(markers, context);

		IValue v = this.resolveFieldAccess(markers, context);
		if (v != null)
		{
			return v;
		}

		// Don't report an error if the receiver is not resolved
		if (this.receiver != null && !this.receiver.isResolved())
		{
			return this;
		}

		final Marker marker = Markers.semanticError(this.position, "method.access.resolve.field", this.name);
		if (this.receiver != null)
		{
			marker.addInfo(Markers.getSemantic("receiver.type", this.receiver.getType()));
		}

		markers.add(marker);
		return this;
	}

	protected IValue resolveFieldAccess(MarkerList markers, IContext context)
	{
		// Duplicate in FieldAssignment

		IValue value;

		if (ICall.privateAccess(context, this.receiver))
		{
			final IValue implicit;
			if (this.receiver == null && (implicit = context.getImplicit()) != null)
			{
				value = this.resolveMethod(implicit, markers, context);
				if (value != null)
				{
					return value;
				}

				value = this.resolveField(implicit, context);
				if (value != null)
				{
					return value;
				}
			}

			value = this.resolveField(this.receiver, context);
			if (value != null)
			{
				return value;
			}

			value = this.resolveMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}
		}
		else
		{
			value = this.resolveMethod(this.receiver, markers, context);
			if (value != null)
			{
				return value;
			}

			value = this.resolveField(this.receiver, context);
			if (value != null)
			{
				return value;
			}
		}

		// Qualified Type Name Resolution

		return this.resolveTypeAccess(context);
	}

	private IValue resolveTypeAccess(IContext context)
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

	private IValue resolveField(IValue receiver, IContext context)
	{
		IDataMember field = ICall.resolveField(context, receiver, this.name);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				return new EnumValue(field.getType(), this.name);
			}

			this.field = field;
			this.receiver = receiver;
			return this;
		}
		return null;
	}

	private IValue resolveMethod(IValue receiver, MarkerList markers, IContext context)
	{
		// We use PostfixCall because it doesn't do implicit-based resolution nor field-apply resolution
		final PostfixCall call = new PostfixCall(this.position, receiver, this.name);
		return call.resolveCall(markers, context, false);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}

		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		if (this.field != null && this.field.hasModifier(Modifiers.CONST))
		{
			if (this.receiver != null && this.receiver.valueTag() == IValue.POP_EXPR)
			{
				// Cannot constant-fold
				return this;
			}

			final IValue value = this.field.getValue();
			if (value != null && value.isConstantOrField())
			{
				return value;
			}
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

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int lineNumber = this.getLineNumber();
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
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(indent, buffer);
			buffer.append('.');
		}

		buffer.append(this.name);
	}
}

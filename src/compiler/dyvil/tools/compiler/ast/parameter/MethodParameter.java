package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalMethod;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.reference.ImplicitReferenceType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public final class MethodParameter extends Parameter
{
	// Metadata
	protected ICallableMember method;

	protected ReferenceType refType;
	protected boolean       assigned;

	public MethodParameter()
	{
	}

	public MethodParameter(Name name)
	{
		super(name);
	}

	public MethodParameter(Name name, IType type)
	{
		super(name, type);
	}

	public MethodParameter(Name name, IType type, ModifierSet modifierSet)
	{
		super(name, type, modifierSet);
	}

	public MethodParameter(ICodePosition position, Name name, IType type)
	{
		super(position, name, type);
	}

	public MethodParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.METHOD_PARAMETER;
	}

	@Override
	public boolean isField()
	{
		return false;
	}

	@Override
	public boolean isVariable()
	{
		return true;
	}

	@Override
	public boolean isAssigned()
	{
		return this.assigned;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.PARAMETER;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public IType getInternalType()
	{
		return this.refType != null ? this.refType : this.type;
	}

	@Override
	public boolean isReferenceCapturable()
	{
		return this.refType != null;
	}

	@Override
	public boolean isReferenceType()
	{
		return this.refType != null;
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue receiver, IValue newValue)
	{
		this.assigned = true;
		return super.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.type != null && this.type.isExtension())
		{
			this.getModifiers().addIntModifier(Modifiers.INFIX_FLAG);
		}

		if (this.modifiers != null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.VAR))
			{
				markers.add(Markers.semanticWarning(this.position, "parameter.var.deprecated"));
				if (this.method instanceof ExternalMethod)
				{
					this.type = this.type.getElementType();
				}
				this.refType = new ImplicitReferenceType(this.type.getRefClass(), this.type);
			}
			else if (this.modifiers.hasIntModifier(Modifiers.INFIX_FLAG))
			{
				this.type.setExtension(true);
			}
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.modifiers != null)
		{
			ModifierUtil.checkModifiers(markers, this, this.modifiers, Modifiers.PARAMETER_MODIFIERS);
		}
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, this.localIndex);
			return;
		}
		writer.visitVarInsn(this.type.getLoadOpcode(), this.localIndex);
	}

	@Override
	public void writeGet_Unwrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			this.refType.writeUnwrap(writer);
		}
	}

	@Override
	public boolean writeSet_PreValue(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, this.localIndex);
			return true;
		}
		return false;
	}

	@Override
	public void writeSet_Wrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			this.refType.writeWrap(writer);
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType == null)
		{
			writer.visitVarInsn(this.type.getStoreOpcode(), this.localIndex);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}

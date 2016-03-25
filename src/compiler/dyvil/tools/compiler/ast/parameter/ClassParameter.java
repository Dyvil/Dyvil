package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public final class ClassParameter extends Parameter implements IField
{
	// Metadata
	protected IClass enclosingClass;

	public ClassParameter()
	{
	}

	public ClassParameter(Name name)
	{
		super(name);
	}

	public ClassParameter(Name name, IType type)
	{
		super(name, type);
	}

	public ClassParameter(Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
	}

	public ClassParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS_PARAMETER;
	}

	@Override
	public boolean isField()
	{
		return true;
	}

	@Override
	public boolean isVariable()
	{
		return false;
	}

	@Override
	public boolean isAssigned()
	{
		return true;
	}

	@Override
	public boolean isReferenceCapturable()
	{
		return false;
	}

	@Override
	public IDataMember capture(IContext context)
	{
		return this;
	}

	@Override
	public IDataMember capture(IContext context, IVariable variable)
	{
		return this;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		if (receiver != null)
		{
			if (this.hasModifier(Modifiers.STATIC))
			{
				if (receiver.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(Markers.semantic(position, "classparameter.access.static", this.name.unqualified));
					return null;
				}
			}
			else if (receiver.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(Markers.semantic(position, "classparameter.access.instance", this.name.unqualified));
			}
		}
		else if (!this.hasModifier(Modifiers.STATIC))
		{
			markers.add(Markers.semantic(position, "classparameter.access.unqualified", this.name.unqualified));
			return new ThisExpr(position, this.enclosingClass.getType(), context, markers);
		}

		ModifierUtil.checkVisibility(this, position, markers, context);

		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue receiver, IValue newValue)
	{
		if (this.enclosingClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(Markers.semanticError(position, "classparameter.assign.annotation", this.name.unqualified));
		}
		return super.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.modifiers != null)
		{
			ModifierUtil.checkModifiers(markers, this, this.modifiers, Modifiers.CLASS_PARAMETER_MODIFIERS);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer)
	{
	}

	@Override
	public void writeInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.enclosingClass.hasModifier(Modifiers.ANNOTATION))
		{
			StringBuilder desc = new StringBuilder("()");
			this.type.appendExtendedName(desc);
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, this.enclosingClass.getInternalName(), this.name.qualified,
			                       desc.toString(), true);
		}
		else
		{
			writer.visitFieldInsn(Opcodes.GETFIELD, this.enclosingClass.getInternalName(), this.name.qualified,
			                      this.getDescriptor());
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.PUTFIELD, this.enclosingClass.getInternalName(), this.name.qualified,
		                      this.getDescriptor());
	}
}

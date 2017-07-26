package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;
import dyvil.tools.parsing.Name;

public interface IParameter extends IVariable, IClassMember
{
	String DEFAULT_PREFIX_INIT = "init$paramDefault$";
	String DEFAULT_PREFIX      = "$paramDefault$";

	@Override
	Name getName();

	@Override
	void setName(Name name);

	@Override
	String getInternalName();

	Name getLabel();

	void setLabel(Name name);

	String getQualifiedLabel();

	IType getCovariantType();

	@Override
	default IClass getEnclosingClass()
	{
		return null;
	}

	@Override
	default void setEnclosingClass(IClass enclosingClass)
	{
	}

	ICallableMember getMethod();

	void setMethod(ICallableMember method);

	int getIndex();

	void setIndex(int index);

	@Override
	boolean isLocal();

	boolean isVarargs();

	void setVarargs();

	default boolean isDefault()
	{
		return this.hasModifier(Modifiers.DEFAULT);
	}

	default boolean isImplicit()
	{
		return this.hasModifier(Modifiers.IMPLICIT);
	}

	@Override
	default void writeInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.isReferenceType())
		{
			return;
		}

		writer.visitVarInsn(this.getType().getLoadOpcode(), this.getLocalIndex());

		Variable.writeRefInit(this, writer, null);
	}

	@Override
	default void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		this.writeInit(writer);
	}

	default void writeParameter(MethodWriter writer)
	{
		final AnnotationList annotations = this.getAnnotations();
		final IType type = this.getType();
		final long flags = ModifierUtil.getFlags(this);

		final int index = this.getIndex();
		final int localIndex = writer.localCount();

		this.setLocalIndex(localIndex);

		// Add the ACC_VARARGS modifier if necessary
		final int javaModifiers = ModifierUtil.getJavaModifiers(flags) | (this.isVarargs() ? Modifiers.ACC_VARARGS : 0);
		writer.visitParameter(localIndex, this.getQualifiedLabel(), type, javaModifiers);

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		if (annotations != null)
		{
			annotations.write(visitor);
		}

		ModifierUtil.writeModifiers(visitor, this, flags);

		IType.writeAnnotations(type, writer, TypeReference.newFormalParameterReference(index), "");
	}

	default void writeDefaultValue(ClassWriter writer)
	{
		final IValue value = this.getValue();
		assert value != null;

		final ICallableMember method = this.getMethod();
		final IType type = this.getType();

		final String name;
		final int access;
		if (method == null)
		{
			name = "init$paramDefault$" + this.getInternalName();
			access = Modifiers.STATIC;
		}
		else
		{
			name = method.getInternalName() + "$paramDefault$" + this.getInternalName();
			access = method.getModifiers().toFlags() & Modifiers.MEMBER_MODIFIERS | Modifiers.STATIC;
		}

		final String desc = "()" + this.getDescriptor();
		final String signature = "()" + this.getSignature();
		final MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(access, name, desc, signature, null));

		mw.visitCode();
		value.writeExpression(mw, type);
		mw.visitEnd(type);
	}

	default void writeGetDefaultValue(MethodWriter writer)
	{
		final ICallableMember method = this.getMethod();
		final IClass enclosingClass = this.getEnclosingClass();

		final String name =
			(method == null ? DEFAULT_PREFIX_INIT : method.getInternalName() + DEFAULT_PREFIX) + this.getInternalName();
		final String desc = "()" + this.getDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, enclosingClass.getInternalName(), name, desc,
		                       enclosingClass.isInterface());
	}

	default AnnotationVisitor visitAnnotation(String internalType)
	{
		if (!this.addRawAnnotation(internalType, null))
		{
			return null;
		}

		IType type = new InternalType(internalType);
		Annotation annotation = new Annotation(type);
		return new AnnotationReader(this.getAnnotations(), annotation);
	}
}

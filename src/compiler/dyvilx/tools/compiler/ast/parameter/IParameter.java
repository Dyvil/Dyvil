package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.DummyValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.backend.annotation.AnnotationReader;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;

public interface IParameter extends IVariable, ClassMember
{
	String DEFAULT_PREFIX_INIT = "init$paramDefault$";
	String DEFAULT_PREFIX      = "$paramDefault$";

	// ------------------------------ Attributable Implementation ------------------------------

	@Override
	default int getJavaFlags()
	{
		return ClassMember.super.getJavaFlags() | (this.isVarargs() ? Modifiers.ACC_VARARGS : 0);
	}

	// --------------- Label ---------------

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

	default void setVarargs()
	{
		this.getAttributes().addFlag(Modifiers.VARARGS);
	}

	default IValue getDefaultValue(IContext context)
	{
		if (!this.isDefault())
		{
			return null;
		}
		return new DummyValue(this::getType, (writer, type) -> this.writeGetDefaultValue(writer));
	}

	@Override
	default void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
	}

	default void writeParameter(MethodWriter writer)
	{
		final IType type = this.getInternalType();

		// #443
		final ICallableMember method = this.getMethod();
		final int index =
			this.getIndex() + (method != null && method.hasModifier(Modifiers.EXTENSION) && !method.isStatic() ? 1 : 0);

		final int localIndex = writer.localCount();

		this.setLocalIndex(localIndex);

		final AttributeList attributes = this.getAttributes();
		int javaFlags = this.getJavaFlags();
		final long dyvilFlags = this.getDyvilFlags();

		if (this.isVarargs())
		{
			javaFlags |= Modifiers.ACC_VARARGS;
		}

		writer.visitParameter(localIndex, this.getQualifiedLabel(), type, javaFlags);

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		attributes.write(visitor);

		ModifierUtil.writeDyvilModifiers(visitor, dyvilFlags);

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
			access = (int) (method.getAttributes().flags() & Modifiers.ACCESS_MODIFIERS) | Modifiers.STATIC;
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
		if (this.skipAnnotation(internalType, null))
		{
			return null;
		}

		IType type = new InternalType(internalType);
		Annotation annotation = new ExternalAnnotation(type);
		return new AnnotationReader(annotation, this.annotationConsumer());
	}
}

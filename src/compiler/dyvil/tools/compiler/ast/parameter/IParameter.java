package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.ClassOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationReader;

public interface IParameter extends IVariable, IClassMember
{
	String DEFAULT_VALUE       = "Ldyvil/annotation/internal/DefaultValue;";
	String DEFAULT_ARRAY_VALUE = "Ldyvil/annotation/internal/DefaultArrayValue;";

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

	void setVarargs(boolean varargs);

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
		final IValue defaultValue = this.getValue();
		final long flags = ModifierUtil.getFlags(this);

		final int index = this.getIndex();
		final int localIndex = writer.localCount();

		this.setLocalIndex(localIndex);
		writer.visitParameter(localIndex, this.getInternalName(), type, ModifierUtil.getJavaModifiers(flags));

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		if (annotations != null)
		{
			annotations.write(visitor);
		}

		ModifierUtil.writeModifiers(visitor, this, flags);

		IType.writeAnnotations(type, writer, TypeReference.newFormalParameterReference(index), "");

		// Default Value
		if (defaultValue == null)
		{
			return;
		}

		writeDefaultValue(writer, type, defaultValue, index);
	}

	static void writeDefaultValue(MethodWriter writer, IType type, IValue defaultValue, int index)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType != null)
		{
			final AnnotationVisitor annotationVisitor = writer
				                                            .visitParameterAnnotation(index, DEFAULT_ARRAY_VALUE, false)
				                                            .visitArray("value");

			final ArrayExpr arrayExpr = (ArrayExpr) defaultValue;
			final ArgumentList values = arrayExpr.getValues();
			final int size = values.size();
			final IType elementType = arrayType.getElementType();

			for (int i = 0; i < size; i++)
			{
				writeDefaultAnnotation(annotationVisitor, elementType, values.get(i));
			}

			annotationVisitor.visitEnd();

			return;
		}

		final AnnotationVisitor annotationVisitor = writer.visitParameterAnnotation(index, DEFAULT_VALUE, false);
		writeDefaultAnnotation(annotationVisitor, type, defaultValue);

		annotationVisitor.visitEnd();
	}

	static void writeDefaultAnnotation(AnnotationVisitor visitor, IType type, IValue value)
	{
		if (type.isPrimitive())
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
				visitor.visit("booleanValue", value.booleanValue());
				return;
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				visitor.visit("intValue", value.intValue());
				return;
			case PrimitiveType.LONG_CODE:
				visitor.visit("longValue", value.longValue());
				return;
			case PrimitiveType.FLOAT_CODE:
				visitor.visit("floatValue", value.floatValue());
				return;
			case PrimitiveType.DOUBLE_CODE:
				visitor.visit("doubleValue", value.doubleValue());
				return;
			}

			return;
		}

		IClass iClass = type.getTheClass();
		if (iClass == Types.STRING_CLASS)
		{
			visitor.visit("stringValue", value.stringValue());
			return;
		}
		if (iClass == ClassOperator.LazyFields.CLASS_CLASS)
		{
			visitor.visit("classValue", value.toObject());
		}
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

package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.expression.constant.EnumValue;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;

public class ValueAnnotationVisitor implements AnnotationVisitor
{
	private IValueConsumer consumer;

	public ValueAnnotationVisitor(IValueConsumer consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void visit(String key, Object value)
	{
		final IValue iValue = IValue.fromObject(value);
		if (iValue == null)
		{
			throw new BytecodeException("Cannot convert '" + value + "' into an IValue");
		}

		this.consumer.setValue(iValue);
	}

	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.extendedToType(enumClass);
		return new EnumValue(t, Name.fromRaw(name));
	}

	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.consumer.setValue(enumValue);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		Annotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		AnnotationValue value = new AnnotationValue(annotation);
		this.consumer.setValue(value);
		return new AnnotationReader(value, annotation);
	}

	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ArrayExpr valueList = new ArrayExpr();
		this.consumer.setValue(valueList);
		return new AnnotationValueReader(valueList);
	}

	@Override
	public void visitEnd()
	{
	}
}

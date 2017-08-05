package dyvilx.tools.compiler.backend.visitor;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.annotation.Annotation;
import dyvilx.tools.compiler.ast.annotation.AnnotationValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;

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
		final ArrayExpr valueList = new ArrayExpr();
		this.consumer.setValue(valueList);
		return new AnnotationValueReader(valueList.getValues());
	}

	@Override
	public void visitEnd()
	{
	}
}

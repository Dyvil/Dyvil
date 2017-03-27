package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.backend.ClassFormat;

public class AnnotationValueReader implements AnnotationVisitor
{
	IValueConsumer consumer;
	
	public AnnotationValueReader(IValueConsumer consumer)
	{
		this.consumer = consumer;
	}
	
	@Override
	public void visit(String key, Object obj)
	{
		this.consumer.setValue(IValue.fromObject(obj));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = AnnotationReader.getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.consumer.setValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
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
		return new AnnotationValueReader(valueList.getValues())
		{
			@Override
			public void visitEnd()
			{
				valueList.getType();
				this.consumer.setValue(valueList);
			}
		};
	}
	
	@Override
	public void visitEnd()
	{
	}
}

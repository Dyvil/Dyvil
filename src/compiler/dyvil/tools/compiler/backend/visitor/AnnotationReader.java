package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.constant.EnumValue;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.parameter.NamedArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.parsing.Name;

public class AnnotationReader implements AnnotationVisitor
{
	private IAnnotationConsumer consumer;
	private IAnnotation         annotation;
	private NamedArgumentList   arguments;
	
	public AnnotationReader(IAnnotationConsumer consumer, IAnnotation annotation)
	{
		this.consumer = consumer;
		this.annotation = annotation;
		this.annotation.setArguments(this.arguments = new NamedArgumentList());
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.arguments.add(Name.fromRaw(key), IValue.fromObject(value));
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
			this.arguments.add(Name.fromRaw(key), enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		Annotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		AnnotationValue value = new AnnotationValue(annotation);
		this.arguments.add(Name.fromRaw(key), value);
		return new AnnotationReader(value, annotation);
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ArrayExpr valueList = new ArrayExpr();
		this.arguments.add(Name.fromRaw(key), valueList);
		return new AnnotationValueReader(valueList.getValues());
	}
	
	@Override
	public void visitEnd()
	{
		if (this.consumer != null)
		{
			this.consumer.setAnnotation(this.annotation);
		}
	}
}

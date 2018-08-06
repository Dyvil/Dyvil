package dyvilx.tools.compiler.backend.annotation;

import dyvil.lang.Name;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.expression.AnnotationExpr;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.EnumValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.backend.ClassFormat;

import java.util.function.Consumer;

public class AnnotationReader implements AnnotationVisitor
{
	private Consumer<Annotation> consumer;
	private Annotation           annotation;
	private ArgumentList         arguments;

	public AnnotationReader(Consumer<Annotation> consumer, Annotation annotation)
	{
		this.consumer = consumer;
		this.annotation = annotation;
		this.annotation.setArguments(this.arguments = new ArgumentList());
	}

	@Override
	public void visit(String key, Object value)
	{
		this.arguments.add(Name.fromRaw(key), IValue.fromObject(value));
	}

	static IValue getEnumValue(String enumClass, String name)
	{
		return new EnumValue(ClassFormat.extendedToType(enumClass), Name.fromRaw(name));
	}

	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		this.arguments.add(Name.fromRaw(key), getEnumValue(enumClass, name));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		AnnotationExpr value = new AnnotationExpr(annotation);
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
			this.consumer.accept(this.annotation);
		}
	}
}

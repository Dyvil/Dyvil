package dyvilx.tools.compiler.backend.annotation;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.expression.AnnotationExpr;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.backend.ClassFormat;

import java.util.function.Consumer;

public class AnnotationValueReader implements AnnotationVisitor
{
	// =============== Fields ===============

	protected final Consumer<IValue> consumer;

	// =============== Constructors ===============

	public AnnotationValueReader(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
	}

	// =============== Methods ===============

	@Override
	public void visit(String key, Object obj)
	{
		this.consumer.accept(IValue.fromObject(obj));
	}

	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		this.consumer.accept(AnnotationReader.getEnumValue(enumClass, name));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		final Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		final AnnotationExpr value = new AnnotationExpr(annotation);
		return new AnnotationReader(annotation, result -> {
			value.setAnnotation(result);
			this.consumer.accept(value);
		});
	}

	@Override
	public AnnotationVisitor visitArray(String key)
	{
		final ArrayExpr arrayExpr = new ArrayExpr();
		return new AnnotationValueReader(arrayExpr.getValues()::add) {
			@Override
			public void visitEnd()
			{
				super.visitEnd();
				// refers to the outer consumer
				AnnotationValueReader.this.consumer.accept(arrayExpr);
			}
		};
	}

	@Override
	public void visitEnd()
	{
	}
}

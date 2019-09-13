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
	// =============== Fields ===============

	protected final Annotation   annotation;
	private final   ArgumentList arguments;

	protected final Consumer<Annotation> completion;

	// =============== Constructors ===============

	public AnnotationReader(Annotation annotation)
	{
		this(annotation, null);
	}

	public AnnotationReader(Annotation annotation, Consumer<Annotation> completion)
	{
		this.completion = completion;
		this.annotation = annotation;
		this.annotation.setArguments(this.arguments = new ArgumentList());
	}

	// =============== Static Methods ===============

	static IValue getEnumValue(String descriptor, String name)
	{
		return new EnumValue(ClassFormat.extendedToType(descriptor), Name.fromQualified(name));
	}

	// =============== Methods ===============

	@Override
	public void visit(String key, Object value)
	{
		this.arguments.add(Name.fromQualified(key), IValue.fromObject(value));
	}

	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		this.arguments.add(Name.fromQualified(key), getEnumValue(enumClass, name));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		Annotation annotation = new ExternalAnnotation(ClassFormat.extendedToType(desc));
		AnnotationExpr annotationExpr = new AnnotationExpr(annotation);
		return new AnnotationReader(annotation, result -> {
			annotationExpr.setAnnotation(result);
			this.arguments.add(Name.fromQualified(key), annotationExpr);
		});
	}

	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ArrayExpr arrayExpr = new ArrayExpr();
		return new AnnotationValueReader(arrayExpr.getValues()::add)
		{
			@Override
			public void visitEnd()
			{
				super.visitEnd();
				AnnotationReader.this.arguments.add(Name.fromQualified(key), arrayExpr);
			}
		};
	}

	@Override
	public void visitEnd()
	{
		if (this.completion != null)
		{
			this.completion.accept(this.annotation);
		}
	}
}

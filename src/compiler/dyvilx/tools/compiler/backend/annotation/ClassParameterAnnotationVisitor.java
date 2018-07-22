package dyvilx.tools.compiler.backend.annotation;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.backend.classes.ExternalClassVisitor;

public class ClassParameterAnnotationVisitor implements AnnotationVisitor
{
	private ExternalClassVisitor classVisitor;

	public ClassParameterAnnotationVisitor(ExternalClassVisitor classVisitor)
	{
		this.classVisitor = classVisitor;
	}

	@Override
	public void visit(String name, Object value)
	{
	}

	@Override
	public void visitEnum(String name, String desc, String value)
	{

	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		return null;
	}

	@Override
	public AnnotationVisitor visitArray(String name)
	{
		if (!"names".equals(name))
		{
			return null;
		}

		return new AnnotationVisitor()
		{
			@Override
			public void visit(String name, Object value)
			{
				if (value instanceof String)
				{
					ClassParameterAnnotationVisitor.this.classVisitor.classParameters.add((String) value);
				}
			}

			@Override
			public void visitEnum(String name, String desc, String value)
			{

			}

			@Override
			public AnnotationVisitor visitAnnotation(String name, String desc)
			{
				return null;
			}

			@Override
			public AnnotationVisitor visitArray(String name)
			{
				return null;
			}

			@Override
			public void visitEnd()
			{
			}
		};
	}

	@Override
	public void visitEnd()
	{
	}
}

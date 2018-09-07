package dyvilx.tools.compiler.backend.annotation;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.attribute.Attributable;

public class DyvilModifiersVisitor implements AnnotationVisitor
{
	private final Attributable attributable;

	public DyvilModifiersVisitor(Attributable attributable)
	{
		this.attributable = attributable;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name))
		{
			if (value instanceof Integer)
			{
				this.attributable.setDyvilFlags((int) value);
			}
			else if (value instanceof Long)
			{
				this.attributable.setDyvilFlags((long) value);
			}
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
}

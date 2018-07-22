package dyvilx.tools.compiler.backend.annotation;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.attribute.AttributeList;

public class ModifierVisitor implements AnnotationVisitor
{
	private final AttributeList attributes;

	public ModifierVisitor(AttributeList attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name))
		{
			if (value instanceof Integer)
			{
				this.attributes.addFlag((int) value);
			}
			else if (value instanceof Long)
			{
				this.attributes.addFlag((long) value);
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

package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.lang.Name;

public class DyvilNameVisitor implements AnnotationVisitor
{
	private final IMember member;

	public DyvilNameVisitor(IMember member)
	{
		this.member = member;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name) && value instanceof String)
		{
			this.member.setName(Name.fromQualified(value.toString()));
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

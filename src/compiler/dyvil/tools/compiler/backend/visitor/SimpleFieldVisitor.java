package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Attribute;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;

public class SimpleFieldVisitor implements FieldVisitor
{
	private IDataMember	field;
	
	public SimpleFieldVisitor(IDataMember field)
	{
		this.field = field;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String internal = ClassFormat.extendedToInternal(type);
		if (this.field.addRawAnnotation(internal))
		{
			Annotation annotation = new Annotation(new InternalType(internal));
			return new AnnotationVisitorImpl(this.field, annotation);
		}
		return null;
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr)
	{
	}

	@Override
	public void visitEnd()
	{
	}
}

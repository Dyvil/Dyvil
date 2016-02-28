package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Attribute;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;

public class SimpleFieldVisitor implements FieldVisitor
{
	private IDataMember field;
	
	public SimpleFieldVisitor(IDataMember field)
	{
		this.field = field;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		if (AnnotationUtil.DYVIL_MODIFIERS.equals(type))
		{
			return new ModifierVisitor(this.field.getModifiers());
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (this.field.addRawAnnotation(internal, null))
		{
			Annotation annotation = new Annotation(new InternalType(internal));
			return new AnnotationReader(this.field, annotation);
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

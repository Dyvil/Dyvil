package dyvilx.tools.compiler.backend.visitor;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.Attribute;
import dyvilx.tools.asm.FieldVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.annotation.Annotation;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.backend.ClassFormat;

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
		if (ModifierUtil.DYVIL_MODIFIERS.equals(type))
		{
			return new ModifierVisitor(this.field.getModifiers());
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (this.field.addRawAnnotation(internal, null))
		{
			Annotation annotation = new Annotation(new InternalType(internal));
			return new AnnotationReader(this.field.getAnnotations(), annotation);
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

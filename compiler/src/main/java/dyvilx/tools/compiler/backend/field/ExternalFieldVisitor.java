package dyvilx.tools.compiler.backend.field;

import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.Attribute;
import dyvilx.tools.asm.FieldVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.annotation.AnnotationReader;
import dyvilx.tools.compiler.backend.annotation.DyvilModifiersVisitor;

public class ExternalFieldVisitor implements FieldVisitor
{
	private IDataMember field;

	public ExternalFieldVisitor(IDataMember field)
	{
		this.field = field;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		if (ModifierUtil.DYVIL_MODIFIERS.equals(type))
		{
			return new DyvilModifiersVisitor(this.field);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (!this.field.skipAnnotation(internal, null))
		{
			Annotation annotation = new ExternalAnnotation(new InternalType(internal));
			return new AnnotationReader(annotation, this.field.annotationConsumer());
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

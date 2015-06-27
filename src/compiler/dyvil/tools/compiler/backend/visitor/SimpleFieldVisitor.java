package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

public class SimpleFieldVisitor extends FieldVisitor
{
	private IDataMember	field;
	
	public SimpleFieldVisitor(IDataMember field)
	{
		super(DyvilCompiler.asmVersion);
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
}

package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassFormat;

public class SimpleFieldVisitor extends FieldVisitor
{
	private IField	field;
	
	public SimpleFieldVisitor(IField field)
	{
		super(DyvilCompiler.asmVersion);
		this.field = field;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String packName = ClassFormat.extendedToPackage(type);
		if (this.field.addRawAnnotation(packName))
		{
			Annotation annotation = new Annotation(new Type(packName));
			return new AnnotationVisitorImpl(this.field, annotation);
		}
		return null;
	}
}

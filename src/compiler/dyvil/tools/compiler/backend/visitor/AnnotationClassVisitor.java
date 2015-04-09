package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassFormat;

public class AnnotationClassVisitor extends MethodVisitor
{
	private IParameter	parameter;
	
	public AnnotationClassVisitor(IParameter param)
	{
		super(DyvilCompiler.asmVersion);
		this.parameter = param;
	}
	
	@Override
	public AnnotationVisitor visitAnnotationDefault()
	{
		return new ValueAnnotationVisitor(this.parameter);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String type, boolean visible)
	{
		String internal = ClassFormat.extendedToInternal(type);
		if (this.parameter.addRawAnnotation(internal))
		{
			Annotation annotation = new Annotation(new Type(internal));
			return new AnnotationVisitorImpl(this.parameter, annotation);
		}
		return null;
	}
}

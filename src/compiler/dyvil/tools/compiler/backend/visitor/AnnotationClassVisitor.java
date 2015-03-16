package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.backend.ClassFormat;

public class AnnotationClassVisitor extends MethodVisitor
{
	private Parameter	parameter;
	
	public AnnotationClassVisitor(Parameter param)
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
		String packName = ClassFormat.internalToPackage2(type);
		if (this.parameter.addRawAnnotation(packName))
		{
			AnnotationType atype = new AnnotationType(packName);
			Annotation annotation = new Annotation(atype);
			return new AnnotationVisitorImpl(this.parameter, annotation);
		}
		return null;
	}
}

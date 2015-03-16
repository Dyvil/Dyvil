package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.member.IAnnotationList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.backend.ClassFormat;

public class AnnotationVisitorImpl extends AnnotationVisitor
{
	private IAnnotationList	annotated;
	private Annotation		annotation;
	
	public AnnotationVisitorImpl(IAnnotationList annotated, Annotation annotation)
	{
		super(Opcodes.ASM5);
		this.annotated = annotated;
		this.annotation = annotation;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.annotation.arguments.addValue(key, IValue.fromObject(value));
	}
	
	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.internalToType(enumClass);
		t.resolve(null, Package.rootPackage);
		return new EnumValue(t, name);
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.annotation.arguments.addValue(key, enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ValueList valueList = new ValueList(null, true);
		this.annotation.arguments.addValue(key, valueList);
		return new ArrayAnnotationVisitor(this.api, valueList);
	}
	
	@Override
	public void visitEnd()
	{
		this.annotated.addAnnotation(this.annotation);
	}
}

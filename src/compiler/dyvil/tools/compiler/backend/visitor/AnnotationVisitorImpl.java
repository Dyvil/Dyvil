package dyvil.tools.compiler.backend.visitor;


import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.ClassFormat;

public class AnnotationVisitorImpl implements AnnotationVisitor
{
	private IAnnotationConsumer consumer;
	private IAnnotation		annotation;
	private ArgumentMap		arguments;
	
	public AnnotationVisitorImpl(IAnnotationConsumer consumer, IAnnotation annotation)
	{
		this.consumer = consumer;
		this.annotation = annotation;
		this.annotation.setArguments(this.arguments = new ArgumentMap());
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.arguments.addValue(Name.getQualified(key), IValue.fromObject(value));
	}
	
	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.extendedToType(enumClass);
		t.resolve(null, Package.rootPackage, TypePosition.CLASS);
		return new EnumValue(t, Name.getQualified(name));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.arguments.addValue(Name.getQualified(key), enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		Annotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		AnnotationValue value = new AnnotationValue(annotation);
		this.arguments.addValue(Name.getQualified(key), value);
		return new AnnotationVisitorImpl(value, annotation);
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		Array valueList = new Array();
		this.arguments.addValue(Name.getQualified(key), valueList);
		return new ArrayAnnotationVisitor(valueList);
	}
	
	@Override
	public void visitEnd()
	{
		this.consumer.setAnnotation(this.annotation);
	}
}

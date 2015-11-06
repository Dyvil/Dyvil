package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.parsing.Name;

public class ValueAnnotationVisitor implements AnnotationVisitor
{
	private IValued valued;
	
	public ValueAnnotationVisitor(IValued valued)
	{
		this.valued = valued;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.valued.setValue(IValue.fromObject(value));
	}
	
	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.extendedToType(enumClass);
		return new EnumValue(t, Name.getQualified(name));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.valued.setValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		Annotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		AnnotationValue value = new AnnotationValue(annotation);
		this.valued.setValue(value);
		return new AnnotationVisitorImpl(value, annotation);
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		Array valueList = new Array();
		this.valued.setValue(valueList);
		return new ArrayAnnotationVisitor(valueList);
	}
	
	@Override
	public void visitEnd()
	{
	}
}

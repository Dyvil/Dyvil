package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationValue;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.backend.ClassFormat;

public final class ArrayAnnotationVisitor implements AnnotationVisitor
{
	private IValueList array;
	
	public ArrayAnnotationVisitor(IValueList array)
	{
		this.array = array;
	}
	
	@Override
	public void visit(String key, Object obj)
	{
		this.array.addValue(IValue.fromObject(obj));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = AnnotationVisitorImpl.getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.array.addValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String key, String desc)
	{
		Annotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		AnnotationValue value = new AnnotationValue(annotation);
		this.array.addValue(value);
		return new AnnotationVisitorImpl(value, annotation);
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		Array valueList = new Array(null);
		this.array.addValue(valueList);
		return new ArrayAnnotationVisitor(valueList);
	}
	
	@Override
	public void visitEnd()
	{
	}
}

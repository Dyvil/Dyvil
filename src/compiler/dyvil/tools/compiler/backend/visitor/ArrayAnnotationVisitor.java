package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;

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
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		// FIXME
		return null;
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

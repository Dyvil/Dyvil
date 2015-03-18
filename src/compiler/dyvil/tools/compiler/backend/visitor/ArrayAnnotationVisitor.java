package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.ValueList;

public final class ArrayAnnotationVisitor extends AnnotationVisitor
{
	private IValueList	array;
	
	public ArrayAnnotationVisitor(int api, IValueList array)
	{
		super(api);
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
	public AnnotationVisitor visitArray(String key)
	{
		ValueList valueList = new ValueList(null);
		this.array.addValue(valueList);
		return new ArrayAnnotationVisitor(this.api, valueList);
	}
}

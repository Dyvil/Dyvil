package dyvil.tools.compiler.ast.classes;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.value.IValue;

public class AnnotationVisitorImpl extends AnnotationVisitor
{
	private Annotation	annotation;
	
	public AnnotationVisitorImpl(int api, Annotation annotation)
	{
		super(api);
		this.annotation = annotation;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.annotation.addValue(key, IValue.fromObject(value));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		// TODO
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ValueList valueList = new ValueList(null);
		this.annotation.addValue(key, valueList);
		return new AnnotationVisitorArray(this.api, valueList);
	}
	
	public static final class AnnotationVisitorArray extends AnnotationVisitor
	{
		private IValueList array;
		
		public AnnotationVisitorArray(int api, IValueList array)
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
			// TODO
		}
		
		@Override
		public AnnotationVisitor visitArray(String key)
		{
			ValueList valueList = new ValueList(null);
			this.array.addValue(valueList);
			return new AnnotationVisitorArray(this.api, valueList);
		}
	}
}
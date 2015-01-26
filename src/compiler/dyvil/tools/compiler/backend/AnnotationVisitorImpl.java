package dyvil.tools.compiler.backend;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.member.IAnnotated;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.ValueList;

public class AnnotationVisitorImpl extends AnnotationVisitor
{
	private IAnnotated	annotated;
	private Annotation	annotation;
	
	public AnnotationVisitorImpl(int api, IAnnotated annotated, Annotation annotation)
	{
		super(api);
		this.annotated = annotated;
		this.annotation = annotation;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.annotation.addValue(key, IValue.fromObject(value));
	}
	
	private static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.internalToType(enumClass);
		t.resolve(Package.rootPackage);
		return new EnumValue(t, name);
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.annotation.addValue(key, enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ValueList valueList = new ValueList(null, true);
		this.annotation.addValue(key, valueList);
		return new AnnotationVisitorArray(this.api, valueList);
	}
	
	@Override
	public void visitEnd()
	{
		this.annotated.addAnnotation(this.annotation);
	}
	
	public static final class AnnotationVisitorArray extends AnnotationVisitor
	{
		private IValueList	array;
		
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
			IValue enumValue = getEnumValue(enumClass, name);
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
			return new AnnotationVisitorArray(this.api, valueList);
		}
	}
}

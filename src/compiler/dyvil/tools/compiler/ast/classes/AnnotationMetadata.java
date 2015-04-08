package dyvil.tools.compiler.ast.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.TreeSet;

import dyvil.tools.compiler.backend.ClassWriter;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class AnnotationMetadata implements IClassMetadata
{
	private IClass			theClass;
	public RetentionPolicy	retention;
	public Set<ElementType>	targets;
	
	public AnnotationMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public RetentionPolicy getRetention()
	{
		return this.retention;
	}
	
	@Override
	public Set<ElementType> getTargets()
	{
		return this.targets;
	}
	
	@Override
	public boolean isTarget(ElementType target)
	{
		if (this.targets == null || this.targets.isEmpty())
		{
			return true;
		}
		return this.targets.contains(target);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.theClass == null)
		{
			return;
		}
		if (this.retention == null)
		{
			Annotation retention = this.theClass.getAnnotation(Types.ARetention);
			if (retention != null)
			{
				EnumValue value = (EnumValue) retention.arguments.getValue(0, Annotation.VALUE);
				this.retention = RetentionPolicy.valueOf(value.name.qualified);
			}
		}
		if (this.targets != null)
		{
			return;
		}
		
		Annotation target = this.theClass.getAnnotation(Types.ATarget);
		if (target == null)
		{
			return;
		}
		
		this.targets = new TreeSet();
		IValueList values = (IValueList) target.arguments.getValue(0, Annotation.VALUE);
		if (values == null)
		{
			return;
		}
		
		int count = values.valueCount();
		for (int i = 0; i < count; i++)
		{
			EnumValue value = (EnumValue) values.getValue(i);
			this.targets.add(ElementType.valueOf(value.name.qualified));
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields)
	{
	}
}

package dyvil.tools.compiler.ast.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.TreeSet;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.value.EnumValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class AnnotationType extends Type
{
	public RetentionPolicy	retention;
	public Set<ElementType>	targets;
	
	public AnnotationType()
	{
		super();
	}
	
	public AnnotationType(String name)
	{
		super(name);
	}
	
	public AnnotationType(String name, IClass iclass)
	{
		super(name, iclass);
	}
	
	public AnnotationType(ICodePosition position, String name)
	{
		super(position, name);
	}
	
	public AnnotationType(ICodePosition position, String name, IClass iclass)
	{
		super(position, name, iclass);
	}
	
	@Override
	public AnnotationType resolve(IContext context)
	{
		if (this.theClass == null)
		{
			IClass iclass;
			if (context == Package.rootPackage)
			{
				iclass = context.resolveClass(this.qualifiedName);
			}
			else
			{
				iclass = context.resolveClass(this.name);
			}
			
			if (iclass != null)
			{
				this.theClass = iclass;
				this.qualifiedName = iclass.getQualifiedName();
			}
		}
		return this;
	}
	
	public void readMetaAnnotations()
	{
		if (this.theClass == null)
		{
			return;
		}
		if (this.retention == null)
		{
			Annotation retention = this.theClass.getAnnotation(Type.ARetention);
			if (retention != null)
			{
				EnumValue value = (EnumValue) retention.getValue("value");
				this.retention = RetentionPolicy.valueOf(value.name);
			}
		}
		if (this.targets == null)
		{
			this.targets = new TreeSet();
			Annotation target = this.theClass.getAnnotation(Type.ATarget);
			if (target != null)
			{
				IValueList values = (IValueList) target.getValue("value");
				if (values != null)
				{
					for (IValue v : values.getValues())
					{
						EnumValue value = (EnumValue) v;
						this.targets.add(ElementType.valueOf(value.name));
					}
				}
			}
		}
	}
	
	public boolean isTarget(ElementType target)
	{
		if (this.targets == null || this.targets.isEmpty())
		{
			return true;
		}
		return targets.contains(target);
	}
}

package dyvil.tools.compiler.ast.method;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;

public abstract class Member implements ITyped, IAnnotatable
{
	private int					modifiers;
	
	private Type				type;
	private String				name;
	private List<Annotation>	annotations;
	
	protected Member()
	{
	}
	
	public Member(String name, Type type, int modifiers)
	{
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public boolean addAnnotation(Annotation annotation)
	{
		return this.annotations.add(annotation);
	}
}

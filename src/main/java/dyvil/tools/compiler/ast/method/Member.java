package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.Modifiers;

public abstract class Member implements IASTObject, INamed, ITyped, IModified, IAnnotatable
{
	private int					modifiers;
	
	private Type				type;
	private String				name;
	private List<Annotation>	annotations = new ArrayList();
	
	protected Member()
	{
	}
	
	public Member(String name)
	{
		this.name = name;
	}
	
	public Member(String name, Type type)
	{
		this.name = name;
		this.type = type;
	}
	
	public Member(String name, Type type, int modifiers)
	{
		this(name, type);
		this.modifiers = modifiers;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
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
	public void toString(String prefix, StringBuilder buffer)
	{
		for (Annotation annotation : this.annotations)
		{
			annotation.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix);
		buffer.append(Modifiers.toString(this.getModifiers()));
		buffer.append(this.getType()).append(' ');
		buffer.append(this.getName());
	}
}

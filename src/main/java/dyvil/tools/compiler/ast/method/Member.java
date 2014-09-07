package dyvil.tools.compiler.ast.method;

import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.IModified;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.type.Type;

public abstract class Member implements INamed, ITyped, IModified, IAnnotatable
{
	private int					modifiers;
	
	private Type				type;
	private String				name;
	private List<Annotation>	annotations;
	
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
}

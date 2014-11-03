package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.Symbols;

public abstract class Member extends ASTObject implements IMember
{
	protected int				modifiers;
	
	public IClass				theClass;
	protected Type				type;
	protected String			name;
	protected String			qualifiedName;
	protected List<Annotation>	annotations	= new ArrayList(1);
	
	protected Member(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Member(IClass iclass, String name)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	public Member(IClass iclass, String name, Type type)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
		this.type = type;
	}
	
	public Member(IClass iclass, String name, Type type, int modifiers)
	{
		this(iclass, name, type);
		this.modifiers = modifiers;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
		this.name = Symbols.contract(name);
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
	public void addModifier(int mod)
	{
		this.modifiers |= mod;
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.modifiers &= ~mod;
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.annotations.add(annotation);
	}
	
	@Override
	public String getName()
	{
		return this.qualifiedName;
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
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (Annotation annotation : this.annotations)
		{
			buffer.append(prefix);
			annotation.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix);
	}
}

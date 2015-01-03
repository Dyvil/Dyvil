package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;

public abstract class Member extends ASTNode implements IMember
{
	public IClass				theClass;
	
	protected List<Annotation>	annotations;
	
	protected int				modifiers;
	
	protected Type				type;
	protected String			name;
	protected String			qualifiedName;
	
	protected Member(IClass iclass)
	{
		this.theClass = iclass;
		this.annotations = new ArrayList(1);
	}
	
	public Member(IClass iclass, String name)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
		this.annotations = new ArrayList(1);
	}
	
	public Member(IClass iclass, String name, Type type)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
		this.type = type;
		this.annotations = new ArrayList(1);
	}
	
	public Member(IClass iclass, String name, Type type, int modifiers, List<Annotation> annotations)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
		this.type = type;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public Annotation getAnnotation(Type type)
	{
		for (Annotation a : this.annotations)
		{
			if (a.type.equals(type))
			{
				return a;
			}
		}
		return null;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.annotations.add(annotation);
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		boolean flag = (this.modifiers & mod) != 0;
		this.modifiers |= mod;
		return flag;
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.modifiers &= ~mod;
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers & Modifiers.ACCESS_MODIFIERS;
	}
	
	@Override
	public byte getAccessibility()
	{
		return IContext.READ_WRITE_ACCESS;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	@Override
	public String getName()
	{
		return this.qualifiedName;
	}
	
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
		this.name = Symbols.contract(name);
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) == mod;
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

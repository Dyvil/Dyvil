package dyvil.tools.compiler.ast.member;

import java.util.ArrayList;
import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.transform.Symbols;

public abstract class Member extends ASTNode implements IMember
{
	public IClass				theClass;
	
	protected List<Annotation>	annotations;
	
	public int					modifiers;
	
	public IType				type;
	public String				name;
	public String				qualifiedName;
	
	protected Member(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Member(IClass iclass, String name)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	public Member(IClass iclass, String name, IType type)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
		this.type = type;
	}
	
	public Member(IClass iclass, String name, IType type, int modifiers, List<Annotation> annotations)
	{
		this.theClass = iclass;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
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
	public Annotation getAnnotation(IType type)
	{
		if (this.annotations == null)
		{
			return null;
		}
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
		if (this.annotations == null)
		{
			this.annotations = new ArrayList(2);
		}
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
		return (this.modifiers & Modifiers.FINAL) != 0 ? IContext.READ_ACCESS : IContext.READ_WRITE_ACCESS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IType getType(ITypeContext context)
	{
		return this.type.getConcreteType(context);
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			for (Annotation annotation : this.annotations)
			{
				buffer.append(prefix);
				annotation.toString(prefix, buffer);
				buffer.append('\n');
			}
		}
		
		buffer.append(prefix);
	}
}

package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.IModified;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.util.Classes;

public abstract class AbstractClass implements INamed, IModified, IAnnotatable
{
	private String				name;
	private int					type;
	private int					modifiers;
	private ClassBody			body;
	
	private List<String>		superClasses	= new ArrayList();
	private List<Annotation>	annotations		= new ArrayList();
	
	protected AbstractClass(int type, ClassBody body)
	{
		this.type = type;
		this.body = body;
		this.body.setTheClass(this);
	}
	
	public static AbstractClass create(int type)
	{
		if (type == Classes.CLASS)
		{
			return new DefaultClass();
		}
		else if (type == Classes.OBJECT)
		{
			return new ObjectClass();
		}
		else if (type == Classes.INTERFACE)
		{
			return new InterfaceClass();
		}
		else if (type == Classes.ENUM)
		{
			return new EnumClass();
		}
		else if (type == Classes.ANNOTATION)
		{
			return new AnnotationClass();
		}
		return null;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void addSuperClass(String superClass)
	{
		this.superClasses.add(superClass);
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
		this.annotations = annotations;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	public List<String> getSuperClasses()
	{
		return this.superClasses;
	}

	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	public ClassBody getBody()
	{
		return this.body;
	}
}

package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.IModified;
import dyvil.tools.compiler.ast.api.INamed;

public abstract class AbstractClass implements INamed, IModified, IAnnotatable
{
	public static final int		CLASS			= 1;
	public static final int		OBJECT			= 2;
	public static final int		INTERFACE		= 3;
	public static final int		ENUM			= 4;
	public static final int		ANNOTATION		= 5;
	
	private int					type;
	private int					modifiers;
	
	private String				name;
	
	private List<String>		superClasses	= new ArrayList();
	private List<Annotation>	annotations		= new ArrayList();
	
	private ClassBody			body;
	
	protected AbstractClass(int type)
	{
		this.type = type;
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
	
	public void setBody(ClassBody body)
	{
		this.body = body;
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

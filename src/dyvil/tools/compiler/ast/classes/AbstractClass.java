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

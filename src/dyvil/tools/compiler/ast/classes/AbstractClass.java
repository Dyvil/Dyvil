package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.ClassBody;
import dyvil.tools.compiler.ast.member.Annotatable;

public abstract class AbstractClass extends Annotatable
{
	public static final int	CLASS			= 1;
	public static final int	OBJECT			= 2;
	public static final int	INTERFACE		= 3;
	public static final int	ENUM			= 4;
	public static final int	ANNOTATION		= 5;
	
	private int				type;
	private int				modifiers;
	
	private String			name;
	
	private List<String>	superClasses	= new ArrayList();
	
	private ClassBody		body;
	
	protected AbstractClass(int type)
	{
		this.type = type;
	}
	
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void addSuperClass(String superClass)
	{
		this.superClasses.add(superClass);
	}
	
	public void setBody(ClassBody body)
	{
		this.body = body;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public List<String> getSuperClasses()
	{
		return this.superClasses;
	}
	
	public ClassBody getBody()
	{
		return this.body;
	}
}

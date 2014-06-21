package dyvil.tools.compiler.ast;

import java.util.LinkedList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;

public class ClassDecl
{
	public static final int		CLASS		= 1;
	public static final int		INTERFACE	= 2;
	public static final int		ENUM		= 3;
	public static final int		ANNOTATION	= 4;
	
	private int					type		= 0;
	private int					modifiers;
	
	private String				name;
	
	private String				superClass;
	private List<String>		interfaces	= new LinkedList();
	private List<Annotation>	annotations	= new LinkedList();
	
	private ClassBody			body;
	
	public void setType(int type)
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
	
	public void setSuperClass(String superClass)
	{
		this.superClass = superClass;
	}
	
	public void addInterface(String interFace)
	{
		this.interfaces.add(interFace);
	}
	
	public void addAnnotation(Annotation annotation)
	{
		this.annotations.add(annotation);
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
	
	public String getSuperClass()
	{
		return this.superClass;
	}
	
	public List<String> getInterfaces()
	{
		return this.interfaces;
	}
	
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	public ClassBody getBody()
	{
		return this.body;
	}
}

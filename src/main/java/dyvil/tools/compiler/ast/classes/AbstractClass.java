package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Classes;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;

public abstract class AbstractClass implements IClass
{
	private String				name;
	private int					type;
	private int					modifiers;
	private ClassBody			body;
	
	private List<IClass>		superClasses	= new ArrayList();
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
		else if (type == Classes.MODULE)
		{
			return new ModuleClass();
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
	
	@Override
	public String getGenericName()
	{
		return this.name;
	}
	
	@Override
	public List<IClass> getSuperClasses()
	{
		return this.superClasses;
	}

	@Override
	public List<Annotation> getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public ClassBody getBody()
	{
		return this.body;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		// FIXME
		return null;
	}
	
	@Override
	public IField resolveField(String name)
	{
		// FIXME
		return this.body.getField(name);
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		return this.body.getMethod(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		// FIXME
		return this.body.getMethod(name, args);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(Modifiers.toString(this.modifiers)).append(' ');
		buffer.append(Classes.toString(this.type)).append(' ');
		buffer.append(this.getGenericName());
		
		if (!this.superClasses.isEmpty())
		{
			buffer.append(" extends ");
			ParserUtil.toString(this.superClasses, (IClass i) -> i.getGenericName(), Formatting.Class.superClassesSeperator, buffer);
		}
		
		buffer.append(Formatting.Class.bodyStart);
		this.body.toString(Formatting.Class.bodyIndent, buffer);
		buffer.append(Formatting.Class.bodyEnd);
	}
}

package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;

public class AbstractClass extends ASTObject implements IClass
{
	protected String			name;
	protected int				type;
	protected int				modifiers;
	protected CompilationUnit	unit;
	protected ClassBody			body;
	
	protected List<IClass>		superClasses	= new ArrayList();
	protected List<Annotation>	annotations		= new ArrayList();
	
	public AbstractClass(ICodePosition position, CompilationUnit unit, int type, ClassBody body)
	{
		this.position = position;
		this.unit = unit;
		this.type = type;
		this.body = body;
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
	public void setBody(ClassBody body)
	{
		this.body = body;
	}
	
	@Override
	public ClassBody getBody()
	{
		return this.body;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.unit.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		// Own fields
		IField field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}
		
		// Inherited Fields
		for (IClass iclass : this.superClasses)
		{
			field = iclass.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		
		return null;
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		// Own methods
		IMethod method = this.body.getMethod(name);
		if (method != null)
		{
			return method;
		}
		
		for (IClass iclass : this.superClasses)
		{
			method = iclass.resolveMethodName(name);
			if (method != null)
			{
				return method;
			}
		}
		
		return null;
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		// Own methods
		List<IMethod> list = new ArrayList();
		this.body.getMethod(list, name, args);
		
		for (IClass iclass : this.superClasses)
		{
			IMethod method = iclass.resolveMethod(name, args);
			if (method != null)
			{
				list.add(method);
			}
		}
		
		// TODO Static, Accessibility, Ambiguity
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	@Override
	public AbstractClass applyState(CompilerState state, IContext context)
	{
		this.body = this.body.applyState(state, this);
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix);
		buffer.append(Modifiers.CLASS.toString(this.modifiers));
		buffer.append(Modifiers.CLASS_TYPE.toString(this.type));
		buffer.append(this.getGenericName());
		
		if (!this.superClasses.isEmpty())
		{
			buffer.append(" extends ");
			ParserUtil.toString(this.superClasses, (IClass o) -> o.getGenericName(), Formatting.Class.superClassesSeperator, buffer);
		}
		
		buffer.append(Formatting.Class.bodyStart);
		this.body.toString(Formatting.Class.bodyIndent, buffer);
		buffer.append(Formatting.Class.bodyEnd);
	}
}

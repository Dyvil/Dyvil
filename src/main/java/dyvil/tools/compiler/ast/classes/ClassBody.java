package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IMethod;
import dyvil.tools.compiler.ast.api.IProperty;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassBody extends ASTNode
{
	protected IClass			theClass;
	protected List<IField>		fields		= new ArrayList();
	protected List<IMethod>		methods		= new ArrayList();
	protected List<IProperty>	properties	= new ArrayList();
	
	public ClassBody(ICodePosition position)
	{
		this.position = position;
	}
	
	public ClassBody(ICodePosition position, IClass iclass)
	{
		this.position = position;
		this.theClass = iclass;
	}
	
	public void setTheClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	public void addField(IField field)
	{
		this.fields.add(field);
	}
	
	public IField getField(String name)
	{
		for (IField field : this.fields)
		{
			if (name.equals(field.getName()))
			{
				return field;
			}
		}
		return null;
	}
	
	public void addProperty(Property prop)
	{
		this.properties.add(prop);
	}
	
	public IProperty getProperty(String name)
	{
		for (IProperty prop : this.properties)
		{
			if (prop.isName(name))
			{
				return prop;
			}
		}
		return null;
	}
	
	public void addMethod(IMethod method)
	{
		this.methods.add(method);
	}
	
	public IMethod getMethod(String name)
	{
		for (IMethod method : this.methods)
		{
			if (name.equals(method.getName()))
			{
				return method;
			}
		}
		return null;
	}
	
	public IMethod getMethod(String name, List<Parameter> parameters)
	{
		outer:
		for (IMethod method : this.methods)
		{
			if (!name.equals(method.getName()))
			{
				continue;
			}
			
			List<Parameter> parameters2 = method.getParameters();
			int len = parameters.size();
			if (len != parameters2.size())
			{
				continue;
			}
			
			for (int i = 0; i < len; i++)
			{
				Parameter par1 = parameters.get(i);
				Parameter par2 = parameters2.get(i);
				if (!par1.getType().equals(par2.getType()))
				{
					continue outer;
				}
			}
			return method;
		}
		return null;
	}
	
	public void getMethodMatches(List<MethodMatch> list, Type type, String name, Type... argumentTypes)
	{
		for (IMethod method : this.methods)
		{
			int match = method.getSignatureMatch(name, type, argumentTypes);
			if (match > 0)
			{
				list.add(new MethodMatch(method, match));
			}
		}
	}
	
	@Override
	public ClassBody applyState(CompilerState state, IContext context)
	{
		for (IField field : this.fields)
		{
			field.applyState(state, context);
		}
		for (IProperty prop : this.properties)
		{
			prop.applyState(state, context);
		}
		for (IMethod method : this.methods)
		{
			method.applyState(state, context);
		}
		
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(Formatting.Class.bodyStart);
		String prefix1 = prefix + Formatting.Class.bodyIndent;
		
		if (!this.fields.isEmpty())
		{
			for (IField field : this.fields)
			{
				field.toString(prefix1, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
		if (!this.properties.isEmpty())
		{
			for (IProperty prop : this.properties)
			{
				prop.toString(prefix1, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
		if (!this.methods.isEmpty())
		{
			Iterator<IMethod> iterator = this.methods.iterator();
			while (true)
			{
				IMethod method = iterator.next();
				method.toString(prefix1, buffer);
				buffer.append('\n');
				
				if (iterator.hasNext())
				{
					buffer.append('\n');
				}
				else
				{
					break;
				}
			}
		}
		
		buffer.append(prefix).append(Formatting.Class.bodyEnd);
	}
}

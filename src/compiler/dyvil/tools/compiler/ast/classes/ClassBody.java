package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassBody extends ASTNode
{
	public IClass			theClass;
	public List<IClass>		classes		= new ArrayList();
	public List<IField>		fields		= new ArrayList();
	public List<IMethod>	methods		= new ArrayList();
	public List<IProperty>	properties	= new ArrayList();
	
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
	
	public void addClass(IClass clazz)
	{
		this.classes.add(clazz);
	}
	
	public IClass getClass(String name)
	{
		for (IClass c : this.classes)
		{
			if (c.isName(name))
			{
				return c;
			}
		}
		return null;
	}
	
	public void addField(IField field)
	{
		this.fields.add(field);
	}
	
	public IField getField(String name)
	{
		for (IField field : this.fields)
		{
			if (field.isName(name))
			{
				return field;
			}
		}
		return null;
	}
	
	public void addProperty(IProperty prop)
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
			if (method.isName(name))
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
			if (!method.isName(name))
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
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		for (IMethod method : this.methods)
		{
			int match = method.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(method, match));
			}
		}
	}
	
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (IClass clazz : this.classes)
		{
			clazz.resolveTypes(markers, context);
		}
		for (IField field : this.fields)
		{
			field.resolveTypes(markers, context);
		}
		for (IProperty prop : this.properties)
		{
			prop.resolveTypes(markers, context);
		}
		for (IMethod method : this.methods)
		{
			method.resolveTypes(markers, context);
		}
	}
	
	public void resolve(List<Marker> markers, IContext context)
	{
		for (IClass clazz : this.classes)
		{
			clazz.resolve(markers, context);
		}
		for (IField field : this.fields)
		{
			field.resolve(markers, context);
		}
		for (IProperty prop : this.properties)
		{
			prop.resolve(markers, context);
		}
		for (IMethod method : this.methods)
		{
			method.resolve(markers, context);
		}
	}
	
	public void check(List<Marker> markers, IContext context)
	{
		for (IClass clazz : this.classes)
		{
			clazz.check(markers, context);
		}
		for (IField field : this.fields)
		{
			field.check(markers, context);
		}
		for (IProperty prop : this.properties)
		{
			prop.check(markers, context);
		}
		for (IMethod method : this.methods)
		{
			method.check(markers, context);
		}
	}
	
	public void foldConstants()
	{
		for (IClass clazz : this.classes)
		{
			clazz.foldConstants();
		}
		for (IField field : this.fields)
		{
			field.foldConstants();
		}
		for (IProperty prop : this.properties)
		{
			prop.foldConstants();
		}
		for (IMethod method : this.methods)
		{
			method.foldConstants();
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(Formatting.Class.bodyStart);
		String prefix1 = prefix + Formatting.Class.bodyIndent;
		
		if (!this.classes.isEmpty())
		{
			for (IClass clazz : this.classes)
			{
				clazz.toString(prefix1, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
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

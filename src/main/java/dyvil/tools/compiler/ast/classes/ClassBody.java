package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassBody extends ASTObject
{
	protected IClass		theClass;
	protected List<IField>	fields	= new ArrayList();
	protected List<IMethod>	methods	= new ArrayList();
	
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
	
	public void addField(IField var)
	{
		this.fields.add(var);
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
		this.fields.replaceAll(f -> f.applyState(state, context));
		this.methods.replaceAll(m -> m.applyState(state, context));
		
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (!this.fields.isEmpty())
		{
			for (IField field : this.fields)
			{
				field.toString(prefix, buffer);
				buffer.append('\n');
			}
			buffer.append('\n');
		}
		
		if (!this.methods.isEmpty())
		{
			for (IMethod method : this.methods)
			{
				method.toString(prefix, buffer);
				buffer.append('\n');
				buffer.append('\n');
			}
		}
	}
}

package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.MatchExpression;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.ConstantFolder;

public final class MethodCall extends AbstractCall implements INamed
{
	public boolean	dotless;
	public Name		name;
	
	public MethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public MethodCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		
		this.name = name;
	}
	
	@Override
	public int valueTag()
	{
		return METHOD_CALL;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.genericData != null)
		{
			this.genericData.resolveTypes(markers, context);
		}
		
		super.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		int args = this.arguments.size();
		
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		if (args == 1 && this.name == Name.match)
		{
			MatchExpression me = Operators.getMatchExpression(this.instance, this.arguments.getFirstValue());
			if (me != null)
			{
				me.position = this.position;
				return me.resolve(markers, context);
			}
		}
		
		this.arguments.resolve(markers, context);
		
		if (args == 1)
		{
			IValue op;
			if (this.instance != null)
			{
				op = Operators.getPriority(this.instance, this.name, this.arguments.getFirstValue());
			}
			else
			{
				op = Operators.getPriority(this.name, this.arguments.getFirstValue());
			}
			
			if (op != null)
			{
				op.setPosition(this.position);
				return op;
			}
		}
		
		IMethod method = ICall.resolveMethod(context, this.instance, this.name, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		if (args == 1 && this.instance != null)
		{
			String qualified = this.name.qualified;
			if (qualified.endsWith("$eq"))
			{
				String unqualified = this.name.unqualified;
				Name name = Name.get(qualified.substring(0, qualified.length() - 3), unqualified.substring(0, unqualified.length() - 1));
				IMethod method1 = IContext.resolveMethod(this.instance.getType(), null, name, this.arguments);
				if (method1 != null)
				{
					CompoundCall call = new CompoundCall(this.position);
					call.method = method1;
					call.instance = this.instance;
					call.arguments = this.arguments;
					call.name = name;
					return call;
				}
			}
			
			IValue op = Operators.get(this.instance, this.name, this.arguments.getFirstValue());
			if (op != null)
			{
				op.setPosition(this.position);
				return op;
			}
		}
		
		// Resolve Apply Method
		if (this.instance == null)
		{
			IValue apply = this.resolveApply(markers, context);
			if (apply != null)
			{
				return apply;
			}
		}
		
		ICall.addResolveMarker(markers, position, instance, name, arguments);
		return this;
	}
	
	private IValue resolveApply(MarkerList markers, IContext context)
	{
		IValue instance;
		IMethod method;
		
		IDataMember field = context.resolveField(this.name);
		if (field == null)
		{
			// Find a type
			IClass iclass = context.resolveClass(this.name);
			if (iclass == null) {
				return null;
			}
			
			// Find the apply method of the type
			IMethod match = IContext.resolveMethod(iclass, null, Name.apply, this.arguments);
			if (match == null)
			{
				// No apply method found -> Not an apply method call
				return null;
			}
			method = match;
			instance = new ClassAccess(this.position, new ClassType(iclass));
		}
		else
		{
			FieldAccess access = new FieldAccess(this.position);
			access.field = field;
			access.name = this.name;
			access.dotless = this.dotless;
			
			// Find the apply method of the field type
			IMethod match = IContext.resolveMethod(field.getType(), access, Name.apply, this.arguments);
			if (match == null)
			{
				// No apply method found -> Not an apply method call
				return null;
			}
			method = match;
			instance = access;
		}
		
		ApplyMethodCall call = new ApplyMethodCall(this.position);
		call.method = method;
		call.instance = instance;
		call.arguments = this.arguments;
		call.genericData = this.genericData;
		
		return call;
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		if (this.arguments.size() == 1)
		{
			IValue argument = this.arguments.getFirstValue();
			if (argument.isConstant())
			{
				if (this.instance != null)
				{
					if (this.instance.isConstant())
					{
						IValue v1 = ConstantFolder.apply(this.instance, this.name, argument);
						return v1 == null ? this : v1;
					}
					
					this.instance = this.instance.foldConstants();
					return this;
				}
				
				IValue v1 = ConstantFolder.apply(this.name, argument);
				if (v1 != null)
				{
					return v1;
				}
			}
			
			if (this.instance != null)
			{
				this.instance = this.instance.foldConstants();
			}
			return this;
		}
		
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			if (this.dotless && !Formatting.Method.useJavaFormat)
			{
				buffer.append(Formatting.Method.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		buffer.append(this.name);
		
		if (this.genericData != null)
		{
			this.genericData.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}

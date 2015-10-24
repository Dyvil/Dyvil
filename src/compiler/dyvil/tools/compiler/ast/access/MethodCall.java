package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.ConstantFolder;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class MethodCall extends AbstractCall implements INamed
{
	protected Name		name;
	protected boolean	dotless;
	
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
	
	public MethodCall(ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.arguments = arguments;
	}
	
	public MethodCall(ICodePosition position, IValue instance, IMethod method, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
		this.name = method.getName();
		this.method = method;
		this.arguments = arguments;
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
	
	public boolean isDotless()
	{
		return this.dotless;
	}
	
	public void setDotless(boolean dotless)
	{
		this.dotless = dotless;
	}
	
	@Override
	public IValue toConstant(MarkerList markers)
	{
		int depth = DyvilCompiler.maxConstantDepth;
		IValue v = this;
		
		do
		{
			if (depth-- < 0)
			{
				return null;
			}
			
			v = v.foldConstants();
		}
		while (!v.isConstant());
		
		return v.toConstant(markers);
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
	protected IValue resolveCall(MarkerList markers, IContext context)
	{
		int args = this.arguments.size();
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
			this.checkArguments(markers, context);
			return this;
		}
		
		if (args == 1 && this.instance != null)
		{
			String qualified = this.name.qualified;
			if (qualified.endsWith("$eq"))
			{
				Name name = Util.stripEq(this.name);
				
				CompoundCall cc = new CompoundCall(this.position, this.instance, name, this.arguments);
				return cc.resolveCall(markers, context);
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
			AbstractCall apply = this.resolveApply(markers, context);
			if (apply != null)
			{
				apply.checkArguments(markers, context);
				return apply;
			}
		}
		
		ICall.addResolveMarker(markers, this.position, this.instance, this.name, this.arguments);
		return this;
	}
	
	private AbstractCall resolveApply(MarkerList markers, IContext context)
	{
		IValue instance;
		IMethod method;
		
		IDataMember field = context.resolveField(this.name);
		if (field == null)
		{
			// Find a type
			IType itype = IContext.resolveType(context, this.name);
			if (itype == null)
			{
				return null;
			}
			
			// Find the apply method of the type
			IMethod match = IContext.resolveMethod(itype, null, Names.apply, this.arguments);
			if (match == null)
			{
				// No apply method found -> Not an apply method call
				return null;
			}
			method = match;
			instance = new ClassAccess(this.position, itype);
		}
		else
		{
			FieldAccess access = new FieldAccess(this.position);
			access.field = field;
			access.name = this.name;
			access.dotless = this.dotless;
			
			// Find the apply method of the field type
			IMethod match = IContext.resolveMethod(field.getType(), access, Names.apply, this.arguments);
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
		if (!this.arguments.isEmpty())
		{
			if (this.instance != null)
			{
				if (this.instance.isConstant())
				{
					IValue argument;
					if (this.arguments.size() == 1 && (argument = this.arguments.getFirstValue()).isConstant())
					{
						IValue folded = ConstantFolder.apply(this.instance, this.name, argument);
						if (folded != null)
						{
							return folded;
						}
					}
				}
				else
				{
					this.instance = this.instance.foldConstants();
				}
			}
			this.arguments.foldConstants();
			return this;
		}
		
		// Prefix methods are transformed to postfix notation
		if (this.instance.isConstant())
		{
			IValue folded = ConstantFolder.apply(this.name, this.instance);
			if (folded != null)
			{
				return folded;
			}
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

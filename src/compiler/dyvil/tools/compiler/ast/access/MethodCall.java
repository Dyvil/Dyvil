package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.ConstantFolder;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class MethodCall extends AbstractCall implements INamed
{
	protected Name    name;
	protected boolean dotless;
	
	public MethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public MethodCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}
	
	public MethodCall(ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
		this.arguments = arguments;
	}
	
	public MethodCall(ICodePosition position, IValue instance, IMethod method, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
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
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		final int args = this.arguments.size();
		final IValue op1 = this.resolvePriorityOperator(markers, context, args);
		if (op1 != null)
		{
			return op1;
		}

		// Normal Method Resolution
		IMethod method = ICall.resolveMethod(context, this.receiver, this.name, this.arguments);
		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return this;
		}

		if (this.receiver != null)
		{
			final IValue op = this.resolveOperator(markers, context, args);
			if (op != null)
			{
				return op;
			}
		}
		else
		{
			// Implicit Calls
			final IValue implicit = context.getImplicit();
			if (implicit != null)
			{
				method = ICall.resolveMethod(context, implicit, this.name, this.arguments);
				if (method != null)
				{
					this.receiver = implicit;
					this.method = method;
					this.checkArguments(markers, context);
					return this;
				}
			}

			// Resolve Apply Method
			return ApplyMethodCall
					.resolveApply(markers, context, this.position, this.receiver, this.name, this.arguments,
					              this.genericData);
		}
		
		return null;
	}

	public IValue resolveOperator(MarkerList markers, IContext context, int args)
	{
		switch (args)
		{
		case 0:
		{
			// Postfix Operators
			final IValue op = Operators.getPostfix(this.receiver, this.name);
			if (op != null)
			{
				op.setPosition(this.position);
				return op.resolveOperator(markers, context);
			}
			break;
		}
		case 1:
		{
			// Infix Operators
			final IValue op = Operators.getInfix(this.receiver, this.name, this.arguments.getFirstValue());
			if (op != null)
			{
				op.setPosition(this.position);
				return op.resolveOperator(markers, context);
			}

			// Compound Operators
			if (Util.hasEq(this.name))
			{
				return CompoundCall
						.resolveCall(markers, context, this.position, this.receiver, Util.removeEq(this.name),
						             this.arguments);
			}
			break;
		}
		}
		return null;
	}

	public IValue resolvePriorityOperator(MarkerList markers, IContext context, int args)
	{
		if (args == 1)
		{
			final IValue op;
			if (this.receiver == null)
			{
				// Prefix Operators (! and *)
				op = Operators.getPrefix(this.name, this.arguments.getFirstValue());
			}
			else
			{
				// Prioritized Infix Operators (namely ==, ===, != and !== for null)
				op = Operators.getInfix_Priority(this.receiver, this.name, this.arguments.getFirstValue());
			}

			if (op != null)
			{
				op.setPosition(this.position);
				return op.resolveOperator(markers, context);
			}
		}
		return null;
	}
	
	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
		ICall.addResolveMarker(markers, this.position, this.receiver, this.name, this.arguments);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (!this.arguments.isEmpty())
		{
			IValue argument;
			if (this.receiver != null)
			{
				if (this.receiver.isConstant())
				{
					if (this.arguments.size() == 1 && (argument = this.arguments.getFirstValue()).isConstant())
					{
						// Binary Infix Operators
						final IValue folded = ConstantFolder.applyInfix(this.receiver, this.name, argument);
						if (folded != null)
						{
							return folded;
						}
					}
				}
				else
				{
					this.receiver = this.receiver.foldConstants();
				}
			}
			else if (this.arguments.size() == 1 && (argument = this.arguments.getFirstValue()).isConstant())
			{
				// Unary Prefix Operators
				final IValue folded = ConstantFolder.applyUnary(this.name, argument);
				if (folded != null)
				{
					return folded;
				}
			}

			this.arguments.foldConstants();
			return this;
		}
		
		if (this.receiver != null)
		{
			if (this.receiver.isConstant())
			{
				// Unary Postfix Operators (and some Prefix Operators)
				final IValue folded = ConstantFolder.applyUnary(this.name, this.receiver);
				if (folded != null)
				{
					return folded;
				}
			}
			
			this.receiver = this.receiver.foldConstants();
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
			if (this.dotless && !Formatting.getBoolean("method.access.java_format"))
			{
				buffer.append(' ');
			}
			else if (this.genericData == null)
			{
				buffer.append('.');
			}
		}
		
		if (this.genericData != null)
		{
			this.genericData.toString(prefix, buffer);
		}
		
		buffer.append(this.name);
		
		this.arguments.toString(prefix, buffer);
	}
}

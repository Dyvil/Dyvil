package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.reference.IReference;
import dyvil.tools.compiler.ast.reference.PropertyReference;
import dyvil.tools.compiler.transform.ConstantFolder;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class MethodCall extends AbstractCall implements INamed
{
	protected Name name;

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

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		final IValue value = this.foldConstants().foldConstants();
		if (value == this)
		{
			return null;
		}

		return value.toAnnotationConstant(markers, context, depth - 1);
	}

	@Override
	public IValue toAssignment(IValue rhs, ICodePosition position)
	{
		final FieldAccess access = new FieldAccess(this.position, this.receiver, this.name);
		return new UpdateMethodCall(this.position.to(position), access, this.arguments, rhs);
	}

	@Override
	public IReference toReference()
	{
		if (!this.arguments.isEmpty())
		{
			return null;
		}

		return new PropertyReference(this.receiver, this.method);
	}

	@Override
	public IValue toReferenceValue(MarkerList markers, IContext context)
	{
		final Name newName = Name.get(this.name.unqualified + "_&", this.name.qualified + "_$amp");
		return AbstractCall.toReferenceValue(this, newName, markers, context);
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		// Normal Method Resolution
		if (this.resolveMethodCall(markers, context))
		{
			return this;
		}

		// Implicit Resolution
		if (this.receiver == null && this.resolveImplicitCall(markers, context))
		{
			return this;
		}

		// Apply Method Resolution
		return ApplyMethodCall.resolveApply(markers, context, this.position, this.receiver, this.name, this.arguments,
		                                    this.genericData);
	}

	protected boolean resolveMethodCall(MarkerList markers, IContext context)
	{
		final IMethod method = ICall.resolveMethod(context, this.receiver, this.name, this.arguments);
		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return true;
		}
		return false;
	}

	protected boolean resolveImplicitCall(MarkerList markers, IContext context)
	{
		final IValue implicit = context.getImplicit();
		if ((implicit) == null)
		{
			return false;
		}

		final IMethod method = ICall.resolveMethod(context, implicit, this.name, this.arguments);
		if (method != null)
		{
			this.receiver = implicit;
			this.method = method;
			this.checkArguments(markers, context);
			return true;
		}
		return false;
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
			buffer.append('.');
		}

		buffer.append(this.name);

		if (this.genericData != null)
		{
			this.genericData.toString(prefix, buffer);
		}

		this.arguments.toString(prefix, buffer);
	}
}

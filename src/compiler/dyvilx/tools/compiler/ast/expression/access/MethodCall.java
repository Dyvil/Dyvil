package dyvilx.tools.compiler.ast.expression.access;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.INamed;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.reference.PropertyReference;
import dyvilx.tools.compiler.ast.reference.ReferenceOperator;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.generic.NamedGenericType;
import dyvilx.tools.compiler.transform.ConstantFolder;
import dyvilx.tools.parsing.marker.MarkerList;

public class MethodCall extends AbstractCall implements INamed
{
	protected Name name;

	public MethodCall(SourcePosition position)
	{
		this.position = position;
		this.arguments = ArgumentList.EMPTY;
	}

	public MethodCall(SourcePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
		this.arguments = ArgumentList.EMPTY;
	}

	public MethodCall(SourcePosition position, IValue instance, Name name, ArgumentList arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
		this.arguments = arguments;
	}

	public MethodCall(SourcePosition position, IValue instance, IMethod method, ArgumentList arguments)
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
	protected Name getReferenceName()
	{
		return Name.from(this.name.unqualified + "_&", this.name.qualified + "_$amp");
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
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		final FieldAccess access = new FieldAccess(this.position, this.receiver, this.name);
		return new ApplyAssignment(this.position.to(position), access, this.arguments, rhs);
	}

	@Override
	public IValue toReferenceValue(MarkerList markers, IContext context)
	{
		if (!this.arguments.isEmpty())
		{
			return null;
		}

		return new ReferenceOperator(this, new PropertyReference(this.receiver, this.method));
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context, boolean report)
	{
		// Implicit Resolution
		if (this.receiver == null)
		{
			final IValue implicitCall = this.resolveImplicitCall(markers, context);
			if (implicitCall != null)
			{
				return implicitCall;
			}
		}

		// Normal Method Resolution
		final MatchList<IMethod> candidates = this.resolveCandidates(context);
		if (candidates.hasCandidate())
		{
			return this.checkArguments(markers, context, candidates.getBestMember());
		}
		else if (candidates.isEmpty())
		{
			final IValue call = this.resolveAlternative(markers, context, report);
			if (call != null)
			{
				return call;
			}
		}

		if (report)
		{
			this.reportResolve(markers, candidates);
			return this;
		}
		return null;
	}

	protected IValue resolveAlternative(MarkerList markers, IContext context, boolean report)
	{
		if (this.genericData != null && this.arguments == ArgumentList.EMPTY)
		{
			final IType parentType;
			if (this.receiver == null)
			{
				parentType = null;
			}
			else if (this.receiver.isClassAccess())
			{
				parentType = this.receiver.getType();
			}
			else
			{
				return null;
			}

			final IType type = new NamedGenericType(this.position, parentType, this.name, this.genericData.getTypes())
				                   .resolveType(report ? markers : null, context);
			if (type == null)
			{
				return null;
			}

			return new ClassAccess(this.position, type);
		}

		final IValue access = new FieldAccess(this.position, this.receiver, this.name).resolveAccess(markers, context);
		if (access == null)
		{
			return null;
		}

		// Field or Class Access available, try to resolve an apply method

		final ApplyAccess call = new ApplyAccess(this.position, access, this.arguments);
		call.genericData = this.genericData;
		return call.resolveCall(markers, context, report);
	}

	protected IValue resolveImplicitCall(MarkerList markers, IContext context)
	{
		final IValue implicit = context.resolveImplicit(null);
		if (implicit == null)
		{
			return null;
		}

		final IMethod method = ICall.resolveMethod(context, implicit, this.name, this.arguments);
		if (method == null)
		{
			return null;
		}

		this.receiver = implicit;
		return this.checkArguments(markers, context, method);
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
					if (this.arguments.size() == 1 && (argument = this.arguments.getFirst()).isConstant())
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
			else if (this.arguments.size() == 1 && (argument = this.arguments.getFirst()).isConstant())
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

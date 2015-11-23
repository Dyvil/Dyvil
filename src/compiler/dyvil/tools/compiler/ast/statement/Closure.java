package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpr;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class Closure extends StatementList
{
	private IParameter implicitParameter;
	private boolean    resolved;
	
	public Closure()
	{
	}
	
	public Closure(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == Names.$it)
		{
			return this.implicitParameter;
		}
		
		return super.resolveField(name);
	}
	
	@Override
	public IAccessible getAccessibleImplicit()
	{
		return this.implicitParameter;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (!this.resolved)
		{
			return true;
		}
		return super.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (!this.resolved)
		{
			return 1;
		}
		return super.getTypeMatch(type);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.resolved)
		{
			return this;
		}
		
		IContext combinedContext = new CombiningContext(this, context);
		
		if (type.typeTag() == IType.LAMBDA && (this.valueCount == 0 || !this.values[this.valueCount - 1].isType(type)))
		{
			LambdaType lt = (LambdaType) type;
			IType returnType = lt.getType().getParameterType();
			int parameterCount = lt.typeCount();

			if (parameterCount == 1)
			{
				this.implicitParameter = new MethodParameter(Names.$it, lt.getType(0));
			}

			IValue resolved = super.resolve(markers, context);
			this.resolved = true;

			if (resolved.isType(type))
			{
				if (resolved == this)
				{
					return super.withType(type, typeContext, markers, combinedContext);
				}

				return resolved.withType(type, typeContext, markers, combinedContext);
			}

			IValue typed = this.typed(resolved, returnType, typeContext, markers, combinedContext);

			switch (parameterCount)
			{
			case 0:
			{
				return lt.wrapLambda(typed, typeContext);
			}
			case 1:
			{
				returnType = typed.getType();
				LambdaExpr le = new LambdaExpr(null, this.implicitParameter);
				le.setType(type);
				le.setReturnType(returnType);
				le.setMethod(lt.getFunctionalMethod());
				le.setValue(typed);
				le.inferReturnType(type, typeContext, returnType);
				return le;
			}
			}

			return null;
		}

		IValue resolved = super.resolve(markers, context);
		this.resolved = true;

		return this.typed(resolved, type, typeContext, markers, context);
	}

	private IValue typed(IValue resolved, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IValue typed;
		if (resolved == this)
		{
			this.returnType = null;
			typed = super.withType(type, typeContext, markers, context);
		}
		else
		{
			typed = resolved.withType(type, typeContext, markers, context);
		}

		if (typed == null)
		{
			Util.createTypeError(markers, resolved, type, typeContext, "closure.type.incompatible");
			return resolved;
		}
		return typed;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Do this in withType
		return this;
	}
}

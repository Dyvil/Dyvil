package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LambdaExpression;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class AppliedStatementList extends StatementList
{
	private IParameter	implicitParameter;
	private boolean		resolved;
	
	public AppliedStatementList()
	{
	}
	
	public AppliedStatementList(ICodePosition position)
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.resolved)
		{
			return this;
		}
		
		IContext context1 = new CombiningContext(this, context);
		
		if (type.typeTag() == IType.LAMBDA)
		{
			LambdaType lt = (LambdaType) type;
			IType returnType = lt.getType();
			int parameterCount = lt.typeCount();
			
			switch (parameterCount)
			{
			case 0:
			{
				IValue resolved = super.resolve(markers, context1);
				this.resolved = true;
				
				IValue typed;
				if (resolved == this)
				{
					typed = super.withType(returnType, typeContext, markers, context1);
				}
				else
				{
					typed = resolved.withType(returnType, typeContext, markers, context1);
				}
				
				return lt.wrapLambda(typed, typeContext);
			}
			case 1:
			{
				this.implicitParameter = new MethodParameter(Names.$it, lt.getType(0));
				IValue resolved = super.resolve(markers, context1);
				this.resolved = true;
				
				IValue typed = resolved.withType(returnType, typeContext, markers, context1);
				if (typed == null)
				{
					return null; // TODO Better error
				}
				
				returnType = typed.getType();
				LambdaExpression le = new LambdaExpression(null, this.implicitParameter);
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
		
		super.resolve(markers, context1);
		this.resolved = true;
		
		return super.withType(type, typeContext, markers, context1);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Do this in withType
		return this;
	}
}

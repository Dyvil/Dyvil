package dyvil.tools.compiler.ast.expression;

import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassAccess extends ASTObject implements IValue, IAccess
{
	public Type	type;
	
	public ClassAccess(ICodePosition position, Type type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
		}
		else if (state == CompilerState.RESOLVE)
		{
			if (!this.type.isResolved())
			{
				String name = this.type.name;
				IField field = context.resolveField(name);
				if (field != null)
				{
					FieldAccess access = new FieldAccess(this.position, null, name);
					access.field = field;
					return access;
				}
				IMethod method = context.resolveMethod(name, Type.EMPTY_TYPES);
				if (method != null)
				{
					MethodCall call = new MethodCall(this.position, null, name);
					call.method = method;
					return call;
				}
				
				state.addMarker(new SemanticError(this.position, "'" + name + "' cannot be resolved to a type"));
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
	}

	@Override
	public void setName(String name)
	{}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public void setValue(IValue value)
	{}

	@Override
	public void setValues(List<IValue> list)
	{}

	@Override
	public void setIsArray(boolean isArray)
	{}

	@Override
	public boolean isArray()
	{
		return false;
	}

	@Override
	public boolean resolve(IContext context)
	{
		return true;
	}

	@Override
	public IAccess resolve2(IContext context)
	{
		return this;
	}

	@Override
	public Marker getResolveError()
	{
		return null;
	}

	@Override
	public IValue getValue()
	{
		return null;
	}

	@Override
	public List<IValue> getValues()
	{
		return null;
	}
}

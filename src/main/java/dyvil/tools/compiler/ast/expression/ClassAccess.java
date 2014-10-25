package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassAccess extends ASTObject implements IValue
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
				
				state.addMarker(new SemanticError(this.position, "The type '" + name + "' could not be resolved."));
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
	}
}

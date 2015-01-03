package dyvil.tools.compiler.ast.expression;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassAccess extends ASTNode implements IValue, IAccess
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
	public void setName(String name)
	{
	}
	
	@Override
	public String getName()
	{
		return this.type.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.type.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.type.qualifiedName.equals(name);
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
	}
	
	@Override
	public void addValue(IValue value)
	{
	}
	
	@Override
	public List<IValue> getValues()
	{
		return null;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return null;
	}
	
	@Override
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
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
				IValue v = this.resolve2(context, null);
				if (v != null)
				{
					return v;
				}
				state.addMarker(this.getResolveError());
			}
		}
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		return this.type.isResolved();
	}
	
	@Override
	public IAccess resolve2(IContext context, IContext context1)
	{
		String name = this.type.qualifiedName;
		FieldMatch f = context.resolveField(context1, name);
		if (f != null)
		{
			FieldAccess access = new FieldAccess(this.position, null, name);
			access.field = f.theField;
			return access;
		}
		
		MethodMatch m = context.resolveMethod(context1, name, Type.EMPTY_TYPES);
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position, null, name);
			call.method = m.theMethod;
			call.dotless = true;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		String name = this.type.name;
		MethodMatch m = context.resolveMethod(null, name, next.getType());
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position, null, name);
			call.addValue(next);
			call.method = m.theMethod;
			call.dotless = true;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SyntaxError(this.position, "'" + this.type.name + "' could not be resolved to a type or field");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
	}
}

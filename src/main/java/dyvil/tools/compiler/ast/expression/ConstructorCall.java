package dyvil.tools.compiler.ast.expression;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ConstructorCall extends Call implements ITyped
{
	protected Type		type;
	protected boolean	isCustom;
	
	public ConstructorCall(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
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
		return "<init>";
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
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public IAccess applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
		}
		else if (state == CompilerState.RESOLVE)
		{
			this.arguments.replaceAll(a -> a.applyState(state, context));
			if (!this.resolve(context, null))
			{
				state.addMarker(new SemanticError(this.position, "The constructor '' could not be resolved"));
			}
			return this;
		}
		
		this.arguments.replaceAll(a -> a.applyState(state, context));
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		if (!this.type.isResolved())
		{
			return false;
		}
		
		MethodMatch match = this.type.resolveMethod(context1, "new", this.getTypes());
		if (match != null)
		{
			this.method = match.theMethod;
			this.isCustom = true;
			return true;
		}
		
		match = this.type.resolveMethod(context1, "<init>", this.getTypes());
		if (match != null)
		{
			this.method = match.theMethod;
			return true;
		}
		return false;
	}
	
	@Override
	public IAccess resolve2(IContext context, IContext context1)
	{
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		if (!this.type.isResolved())
		{
			return new SemanticError(this.type.getPosition(), "'" + this.type.name + "' could not be resolved to a type");
		}
		return new SemanticError(this.position, "'' could not be resolved to a constructor");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int opcode;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
			writer.visitInsn(Opcodes.DUP);
		}
		
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = this.method.getName();
		String desc = this.method.getDescriptor();
		writer.visitMethodInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall && !Formatting.Method.useJavaFormat)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			Util.parametersToString(this.arguments, buffer, true);
		}
	}
}

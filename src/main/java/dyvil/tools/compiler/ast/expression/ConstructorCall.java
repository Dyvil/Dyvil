package dyvil.tools.compiler.ast.expression;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IMethod;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ConstructorCall extends Call implements ITyped
{
	protected Type		type;
	protected IMethod	method;
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
	{}
	
	@Override
	public String getName()
	{
		return "<init>";
	}
	
	@Override
	public void setValue(IValue value)
	{}

	@Override
	public IValue getValue()
	{
		return null;
	}

	@Override
	public void setArray(boolean array)
	{}

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
		return this;
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
	public void write(MethodVisitor visitor)
	{
		int opcode;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			
			String name = this.type.getInternalName();
			visitor.visitTypeInsn(Opcodes.NEW, name);
			visitor.visitInsn(Opcodes.DUP);
		}
		
		for (IValue arg : this.arguments)
		{
			arg.write(visitor);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = this.method.getName();
		String desc = this.method.getDescriptor();
		visitor.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			Util.parametersToString(this.arguments, buffer, true);
		}
	}
}

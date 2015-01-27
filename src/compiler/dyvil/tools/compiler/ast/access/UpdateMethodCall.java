package dyvil.tools.compiler.ast.access;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.AccessResolver;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class UpdateMethodCall extends ASTNode implements IAccess
{
	public IValue		instance;
	public List<IValue>	arguments;
	
	public IMethod		method;
	
	public UpdateMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public UpdateMethodCall(ICodePosition position, IValue instance)
	{
		this.position = position;
		this.instance = instance;
		this.arguments = new ArrayList(3);
	}
	
	@Override
	public int getValueType()
	{
		return UPDATE_METHOD_CALL;
	}
	
	@Override
	public IType getType()
	{
		return this.method == null ? Type.NONE : this.method.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		return this.isType(type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Type.NONE || type == Type.VOID)
		{
			return true;
		}
		return this.method == null ? false : Type.isSuperType(type, this.method.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.method == null)
		{
			return 0;
		}
		
		IType type1 = this.method.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		else if (type1.isSuperType(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return AccessResolver.resolve(markers, context, this);
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.instance, this.arguments);
			
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.method.deprecated", "update"));
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(Markers.create(this.position, "access.method.instance", "update"));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.method.sealed", "update"));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.method.invisible", "update"));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, List<Marker> markers)
	{
		IMethod method = IAccess.resolveMethod(context, this.instance, "update", this.arguments);
		if (method != null)
		{
			this.method = method;
			return true;
		}
		return false;
	}
	
	@Override
	public IValue resolve2(IContext context)
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
		Marker marker = Markers.create(this.position, "resolve.method", "update");
		
		if (this.instance != null)
		{
			IType vtype = this.instance.getType();
			marker.addInfo("Instance Type: " + (vtype == null ? "unknown" : vtype));
		}
		StringBuilder builder = new StringBuilder("Argument Types: [");
		Util.typesToString(this.arguments, ", ", builder);
		marker.addInfo(builder.append(']').toString());
		return marker;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		// Writes the prefix opcodes if a @Bytecode annotation is present.
		this.method.writePrefixBytecode(writer);
		
		// Writes the instance (the first operand).
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		// Writes the infix opcodes if a @Bytecode annotation is present.
		this.method.writeInfixBytecode(writer);
		
		// Writes the arguments (the second operand).
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		// Apply special compilation when dealing with boolean types
		// Writes the postfix opcodes if a @Bytecode annotation is present.
		
		Label ifEnd = new Label();
		// Condition
		if (this.method.writePostfixBytecode(writer, ifEnd))
		{
			Label elseEnd = new Label();
			
			// If Block
			writer.visitLdcInsn(1);
			writer.pop();
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			writer.visitLabel(ifEnd);
			// Else Block
			writer.visitLdcInsn(0);
			writer.visitLabel(elseEnd);
			return;
		}
		
		// If no @Bytecode annotation is present, write a normal invokation.
		this.method.writeCall(writer, this.instance == null ? false : this.instance.getValueType() == SUPER);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		
		if (this.method.getType() != Type.VOID)
		{
			writer.visitInsn(Opcodes.POP);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		// Writes the prefix opcodes if a @Bytecode annotation is present.
		this.method.writePrefixBytecode(writer, dest);
		
		// Writes the instance (the first operand).
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		// Writes the infix opcodes if a @Bytecode annotation is present.
		this.method.writeInfixBytecode(writer, dest);
		
		// Writes the arguments (the second operand).
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		// Writes the postfix opcodes if a @Bytecode annotation is present.
		if (this.method.writePostfixBytecode(writer, dest))
		{
			return;
		}
		
		// If no @Bytecode annotation is present, write a normal invocation.
		this.method.writeCall(writer, this.instance.getValueType() == SUPER);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		
		buffer.append(Formatting.Method.parametersStart);
		Iterator<? extends IASTNode> iterator = this.arguments.iterator();
		while (true)
		{
			IASTNode value = iterator.next();
			if (iterator.hasNext())
			{
				value.toString("", buffer);
				buffer.append(Formatting.Method.parameterSeperator);
			}
			else
			{
				int len = buffer.length();
				buffer.delete(len - 2, len);
				buffer.append(Formatting.Method.parametersEnd).append(Formatting.Field.keyValueSeperator).append(' ');
				value.toString("", buffer);
				break;
			}
		}
	}
}

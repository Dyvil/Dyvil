package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class SyncStatement extends Value implements IStatement
{
	protected IValue	lock;
	protected IValue	action;
	
	public SyncStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public SyncStatement(ICodePosition position, IValue lock, IValue block)
	{
		this.lock = lock;
		this.action = block;
	}
	
	@Override
	public int valueTag()
	{
		return SYNCHRONIZED;
	}
	
	public IValue getLock()
	{
		return this.lock;
	}
	
	public void setLock(IValue lock)
	{
		this.lock = lock;
	}
	
	public IValue getAction()
	{
		return this.action;
	}
	
	public void setAction(IValue action)
	{
		this.action = action;
	}
	
	@Override
	public IType getType()
	{
		return this.action.getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.action = this.action.withType(type, typeContext, markers, context);
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.action.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return this.action.getTypeMatch(type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.lock.resolveTypes(markers, context);
		this.action.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.lock.resolve(markers, context);
		this.action.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.lock.checkTypes(markers, context);
		this.action.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.lock.check(markers, context);
		this.action.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.lock = this.lock.foldConstants();
		this.action = this.action.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.lock = this.lock.cleanup(context, compilableList);
		this.action = this.action.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.write(writer, true);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.write(writer, false);
	}
	
	private void write(MethodWriter writer, boolean expression) throws BytecodeException
	{
		dyvil.tools.asm.Label start = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label end = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label handlerStart = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label throwLabel = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label handlerEnd = new dyvil.tools.asm.Label();
		
		this.lock.writeExpression(writer);
		writer.writeInsn(Opcodes.DUP);
		
		int varIndex = writer.startSync();
		writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		writer.writeInsn(Opcodes.MONITORENTER);
		
		writer.writeLabel(start);
		if (expression)
		{
			this.action.writeExpression(writer);
		}
		else
		{
			this.action.writeStatement(writer);
		}
		writer.endSync();
		
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeInsn(Opcodes.MONITOREXIT);
		writer.writeLabel(end);
		
		writer.writeJumpInsn(Opcodes.GOTO, handlerEnd);
		
		writer.writeLabel(handlerStart);
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeInsn(Opcodes.MONITOREXIT);
		writer.writeLabel(throwLabel);
		writer.writeInsn(Opcodes.ATHROW);
		if (expression)
		{
			this.action.getType().writeDefaultValue(writer);
		}
		
		writer.resetLocals(varIndex);
		writer.writeLabel(handlerEnd);
		
		writer.writeFinallyBlock(start, end, handlerStart);
		writer.writeFinallyBlock(handlerStart, throwLabel, handlerStart);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.syncStart);
		if (this.lock != null)
		{
			this.lock.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.syncEnd);
		this.action.toString(prefix, buffer);
	}
}

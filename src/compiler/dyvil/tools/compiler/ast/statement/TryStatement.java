package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class TryStatement extends ASTNode implements IStatement
{
	public IValue				action;
	private CatchBlock[]		catchBlocks	= new CatchBlock[1];
	private int					catchBlockCount;
	public IValue				finallyBlock;
	
	private IType				commonType;
	
	private IStatement			parent;
	
	public TryStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return TRY;
	}
	
	@Override
	public IType getType()
	{
		if (this.commonType != null)
		{
			return this.commonType;
		}
		
		if (this.finallyBlock != null)
		{
			return this.commonType = this.finallyBlock.getType();
		}
		if (this.action == null)
		{
			return Types.UNKNOWN;
		}
		IType type = this.action.getType();
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			IType t1 = this.catchBlocks[i].action.getType();
			type = Type.findCommonSuperType(type, t1);
			if (type == null)
			{
				return this.commonType = Types.ANY;
			}
		}
		return this.commonType = type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (this.finallyBlock != null)
		{
			IValue value1 = this.finallyBlock.withType(type);
			if (value1 == null)
			{
				return null;
			}
			this.finallyBlock = value1;
			this.commonType = type;
			return this;
		}
		
		if (this.action != null)
		{
			IValue value1 = this.action.withType(type);
			if (value1 == null)
			{
				return null;
			}
			this.action = value1;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			IValue value1 = block.action.withType(type);
			if (value1 == null)
			{
				return null;
			}
			block.action = value1;
		}
		
		this.commonType = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		if (this.finallyBlock != null)
		{
			return this.finallyBlock.isType(type);
		}
		if (this.action != null && !this.action.isType(type))
		{
			return false;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (!this.catchBlocks[i].action.isType(type))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	public void addCatchBlock(CatchBlock block)
	{
		int index = this.catchBlockCount++;
		if (index >= this.catchBlocks.length)
		{
			CatchBlock[] temp = new CatchBlock[this.catchBlockCount];
			System.arraycopy(this.catchBlocks, 0, temp, 0, this.catchBlocks.length);
			this.catchBlocks = temp;
		}
		
		this.catchBlocks[index] = block;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		return this.parent == null ? this.parent.resolveLabel(name) : null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.type = block.type.resolve(markers, context);
			block.action.resolveTypes(markers, context);
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, context);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.context = context;
			block.action = block.action.resolve(markers, block);
			block.context = null;
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.checkTypes(markers, context);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.context = context;
			block.action.checkTypes(markers, block);
			block.context = null;
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			if (!Types.THROWABLE.isSuperTypeOf(block.type))
			{
				Marker marker = markers.create(block.type.getPosition(), "try.catch.type");
				marker.addInfo("Exception Type: " + block.type);
			}
			
			block.action.check(markers, context);
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.action = block.action.foldConstants();
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		// FIXME
		this.commonType.writeDefaultValue(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label tryStart = new org.objectweb.asm.Label();
		org.objectweb.asm.Label tryEnd = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = new org.objectweb.asm.Label();
		
		writer.writeTargetLabel(tryStart);
		if (this.action != null)
		{
			this.action.writeStatement(writer);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
		}
		writer.writeLabel(tryEnd);
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			org.objectweb.asm.Label handlerLabel = new org.objectweb.asm.Label();
			String type = block.type.getInternalName();
			
			writer.writeTargetLabel(handlerLabel);
			writer.startCatchBlock(type);
			// We need a NOP here so the MethodWriter creates a StackMapFrame
			// that does *not* include the variable that is about to be
			// registered.
			writer.writeInsn(Opcodes.NOP);
			
			// Check if the block's variable is actually used
			if (block.variable != null)
			{
				// If yes register a new local variable for the exception and
				// store it.
				int localCount = writer.localCount();
				block.variable.writeInit(writer, null);
				block.action.writeStatement(writer);
				writer.resetLocals(localCount);
			}
			// Otherwise pop the exception from the stack
			else
			{
				writer.writeInsn(Opcodes.POP);
				block.action.writeStatement(writer);
			}
			
			writer.writeCatchBlock(tryStart, tryEnd, handlerLabel, type);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
		}
		
		if (this.finallyBlock != null)
		{
			org.objectweb.asm.Label finallyLabel = new org.objectweb.asm.Label();
			
			writer.writeLabel(finallyLabel);
			writer.startCatchBlock("java/lang/Throwable");
			writer.writeInsn(Opcodes.POP);
			writer.writeLabel(endLabel);
			this.finallyBlock.writeStatement(writer);
			writer.writeFinallyBlock(tryStart, tryEnd, finallyLabel);
		}
		else
		{
			writer.writeLabel(endLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.tryStart);
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			
			buffer.append('\n').append(prefix).append(Formatting.Statements.catchStart);
			block.type.toString(prefix, buffer);
			buffer.append(' ').append(block.varName).append(Formatting.Statements.catchEnd);
			block.action.toString(prefix, buffer);
		}
		if (this.finallyBlock != null)
		{
			buffer.append('\n').append(prefix).append(Formatting.Statements.tryFinally);
			this.finallyBlock.toString(prefix, buffer);
		}
	}
}

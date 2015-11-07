package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TryStatement extends Value implements IStatement, IDefaultContext
{
	protected IValue		action;
	protected CatchBlock[]	catchBlocks	= new CatchBlock[1];
	protected int			catchBlockCount;
	protected IValue		finallyBlock;
	
	// Metadata
	private IType commonType;
	
	public TryStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return TRY;
	}
	
	public void setAction(IValue action)
	{
		this.action = action;
	}
	
	public IValue getAction()
	{
		return this.action;
	}
	
	public void setFinallyBlock(IValue finallyBlock)
	{
		this.finallyBlock = finallyBlock;
	}
	
	public IValue getFinallyBlock()
	{
		return this.finallyBlock;
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
			type = Types.combine(type, t1);
			if (type == null)
			{
				return this.commonType = Types.ANY;
			}
		}
		return this.commonType = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.finallyBlock != null)
		{
			IValue value1 = this.finallyBlock.withType(type, typeContext, markers, context);
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
			IValue value1 = this.action.withType(type, typeContext, markers, context);
			if (value1 == null)
			{
				return null;
			}
			this.action = value1;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			IValue value1 = block.action.withType(type, typeContext, markers, context);
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
	public float getTypeMatch(IType type)
	{
		return 0;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.type = block.type.resolveType(markers, context);
			block.action.resolveTypes(markers, context);
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.action != null)
		{
			this.action.resolveStatement(context, markers);
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].action.resolveStatement(context, markers);
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock.resolveStatement(context, markers);
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
			block.type.resolve(markers, context);
			block.action = block.action.resolve(markers, new CombiningContext(block, context));
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
			this.action.checkTypes(markers, new CombiningContext(this, context));
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.type.checkType(markers, context, TypePosition.RETURN_TYPE);
			block.action.checkTypes(markers, new CombiningContext(block, context));
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
			this.action.check(markers, new CombiningContext(this, context));
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.type.check(markers, context);
			
			if (!Types.THROWABLE.isSuperTypeOf(block.type))
			{
				Marker marker = I18n.createMarker(block.position, "try.catch.type");
				marker.addInfo("Exception Type: " + block.type);
				markers.add(marker);
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
			block.type.foldConstants();
			block.action = block.action.foldConstants();
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.action != null)
		{
			this.action = this.action.cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.type.cleanup(context, compilableList);
			block.action = block.action.cleanup(new CombiningContext(block, context), compilableList);
		}
		
		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public boolean handleException(IType type)
	{
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (this.catchBlocks[i].type.isSuperTypeOf(type))
			{
				return true;
			}
		}
		return false;
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
		dyvil.tools.asm.Label tryStart = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label tryEnd = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = new dyvil.tools.asm.Label();
		
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
			dyvil.tools.asm.Label handlerLabel = new dyvil.tools.asm.Label();
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
			dyvil.tools.asm.Label finallyLabel = new dyvil.tools.asm.Label();
			
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

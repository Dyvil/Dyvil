package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class TryStatement extends ASTNode implements IStatement
{
	private static IType	THROWABLE;
	
	public IValue			action;
	private CatchBlock[]	catchBlocks	= new CatchBlock[1];
	private int				catchBlockCount;
	public IValue			finallyBlock;
	
	private IStatement		parent;
	
	public TryStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	private static IType getThrowableType()
	{
		if (THROWABLE == null)
		{
			THROWABLE = new Type(Package.javaLang.resolveClass("Throwable"));
		}
		return THROWABLE;
	}
	
	@Override
	public int getValueType()
	{
		return TRY;
	}
	
	@Override
	public IType getType()
	{
		return Type.NONE;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.VOID || type == Type.NONE;
	}
	
	@Override
	public int getTypeMatch(IType type)
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
	public Label resolveLabel(String name)
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
	public void check(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action.check(markers, context);
		}
		
		IType throwable = getThrowableType();
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			block.action.check(markers, context);
			
			if (!throwable.isSuperTypeOf(block.type))
			{
				markers.add(block.type.getPosition(), "try.catch.type");
			}
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
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		org.objectweb.asm.Label tryStart = new org.objectweb.asm.Label();
		org.objectweb.asm.Label tryEnd = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = new org.objectweb.asm.Label();
		
		writer.writeLabel(tryStart);
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
			int varIndex;
			
			writer.push(block.type);
			// Check if the block's variable is actually used
			if (block.variable != null)
			{
				// If yes register a new local variable for the exception and
				// store it.
				writer.writeLabel(handlerLabel);
				writer.writeFrame();
				varIndex = block.variable.index = writer.registerLocal(block.type);
				writer.writeVarInsn(Opcodes.ASTORE, varIndex);
				block.action.writeStatement(writer);
				writer.removeLocals(1);
			}
			// Otherwise pop the exception from the stack
			else
			{
				varIndex = -1;
				writer.writeFrameLabel(handlerLabel);
				writer.writeInsn(Opcodes.POP);
				block.action.writeStatement(writer);
			}
			
			writer.writeTryCatchBlock(tryStart, tryEnd, handlerLabel, block.type);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
		}
		
		if (this.finallyBlock != null)
		{
			org.objectweb.asm.Label finallyLabel = new org.objectweb.asm.Label();
			
			writer.push("java/lang/Throwable");
			writer.writeLabel(finallyLabel);
			writer.writeInsn(Opcodes.POP);
			writer.writeFrameLabel(endLabel);
			this.finallyBlock.writeExpression(writer);
			writer.writeFinallyBlock(tryStart, tryEnd, finallyLabel);
		}
		else
		{
			writer.writeFrameLabel(endLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("try");
		if (this.action != null)
		{
			Formatting.appendValue(this.action, prefix, buffer);
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];
			
			buffer.append('\n').append(prefix).append(Formatting.Statements.catchStart);
			block.type.toString(prefix, buffer);
			buffer.append(' ').append(block.varName).append(Formatting.Statements.catchEnd);
			Formatting.appendValue(block.action, prefix, buffer);
		}
		if (this.finallyBlock != null)
		{
			buffer.append('\n').append(prefix).append("finally");
			Formatting.appendValue(this.finallyBlock, prefix, buffer);
		}
	}
}

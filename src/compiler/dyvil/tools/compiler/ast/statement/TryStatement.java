package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class TryStatement extends ASTNode implements IStatement
{
	public IValue			action;
	private CatchBlock[]	catchBlocks	= new CatchBlock[1];
	private int				catchBlockCount;
	public IValue			finallyBlock;
	
	private IStatement		parent;
	
	public TryStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return TRY;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	public void addCatchBlock(CatchBlock block)
	{
		int index = this.catchBlockCount++;
		if (index > this.catchBlocks.length)
		{
			CatchBlock[] temp = new CatchBlock[index];
			System.arraycopy(catchBlocks, 0, temp, 0, catchBlocks.length);
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
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

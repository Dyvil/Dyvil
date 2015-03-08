package dyvil.tools.compiler.ast.pattern;

import java.util.List;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;

public class MatchExpression extends ASTNode implements IValue
{
	private IValue	value;
	private ICase[]	cases;
	private int		caseCount;
	
	public MatchExpression(IValue value, ICase[] cases)
	{
		this.value = value;
		this.cases = cases;
		this.caseCount = cases.length;
	}
	
	@Override
	public int getValueType()
	{
		return MATCH;
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
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i] = this.cases[i].resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.value.check(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		IType type = this.value.getType();
		int var = writer.registerLocal(type);
		int loadOpcode = type.getLoadOpcode();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), var);
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; i < this.caseCount; )
		{
			writer.writeVarInsn(loadOpcode, var);
			this.cases[i].writeExpression(writer, elseLabel);
			writer.writeJump(Opcodes.GOTO, endLabel);
			writer.writeFrameLabel(elseLabel);
			
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
		}
		
		writer.writeFrameLabel(elseLabel);
		this.writeError(writer, type, loadOpcode, var);
		writer.removeLocals(1);
		writer.writeFrameLabel(endLabel);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		IType type = this.value.getType();
		int var = writer.registerLocal(type);
		int loadOpcode = type.getLoadOpcode();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), var);
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; i < this.caseCount;)
		{
			writer.writeVarInsn(loadOpcode, var);
			this.cases[i].writeStatement(writer, elseLabel);
			writer.writeJump(Opcodes.GOTO, endLabel);
			writer.writeFrameLabel(elseLabel);
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
		}
		
		// MatchError
		writer.writeFrameLabel(elseLabel);
		this.writeError(writer, type, loadOpcode, var);
		writer.removeLocals(1);
		writer.writeFrameLabel(endLabel);
	}
	
	private void writeError(MethodWriter writer, IType type, int loadOpcode, int var)
	{
		writer.writeTypeInsn(Opcodes.NEW, "dyvil/lang/MatchError");
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(loadOpcode, var);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/lang/MatchError", "<init>", "(" + type.getExtendedName() + ")V", 2, null);
		writer.writeInsn(Opcodes.ATHROW);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		if (this.caseCount == 1)
		{
			buffer.append(" match ");
			this.cases[0].toString(prefix, buffer);
			return;
		}
		
		buffer.append(" match {\n");
		String prefix1 = prefix + Formatting.Method.indent;
		for (int i = 0; i < this.caseCount; i++)
		{
			buffer.append(prefix1);
			this.cases[i].toString(prefix, buffer);
			buffer.append('\n');
		}
		buffer.append(prefix).append('}');
	}
}

package dyvil.tools.compiler.ast.match;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class MatchExpression extends ASTNode implements IValue
{
	private IValue	value;
	private ICase[]	cases;
	private int		caseCount;
	private boolean	exhaustive;
	
	private IType	type;
	
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
	public boolean isPrimitive()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			if (this.cases[i].getValue().isPrimitive())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}
		
		int len = this.caseCount;
		if (len == 0)
		{
			this.type = Type.VOID;
			return this.type;
		}
		
		IType t = this.cases[0].getValue().getType();
		for (int i = 1; i < len; i++)
		{
			IType t1 = this.cases[i].getValue().getType();
			t = Type.findCommonSuperType(t, t1);
			if (t == null)
			{
				return this.type = Type.VOID;
			}
		}
		
		return this.type = t;
	}
	
	@Override
	public IValue withType(IType type)
	{
		this.type = type;
		return IValue.super.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].getValue().isType(type))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].getValue().isType(type))
			{
				return 0;
			}
		}
		return 3;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IType type = this.value.getType();
		
		this.value = this.value.resolve(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i] = this.cases[i].resolve(markers, context);
		}
		
		if (type == Type.BOOLEAN && this.caseCount >= 2)
		{
			this.exhaustive = true;
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			ICase c = this.cases[i];
			if (this.exhaustive)
			{
				markers.add(c.getPosition(), "pattern.dead");
			}
			
			IPattern pattern = c.getPattern();
			if (pattern.getPatternType() == IPattern.WILDCARD)
			{
				if (c.getCondition() == null)
				{
					this.exhaustive = true;
				}
			}
			else if (!pattern.isType(this.type))
			{
				Marker marker = markers.create(pattern.getPosition(), "pattern.type");
				marker.addInfo("Pattern Type: " + pattern.getType());
				marker.addInfo("Value Type: " + this.type);
			}
			
			this.cases[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
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
		int varIndex = writer.localCount();
		
		IType type = this.value.getType();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), varIndex);
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; i < this.caseCount;)
		{
			this.cases[i].writeExpression(writer, varIndex, elseLabel);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
			writer.writeFrameLabel(elseLabel);
			
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
		}
		
		writer.writeFrameLabel(elseLabel);
		this.writeError(writer, type, type.getLoadOpcode(), varIndex);
		writer.resetLocals(varIndex);
		writer.writeFrameLabel(endLabel);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		int varIndex = writer.localCount();
		
		IType type = this.value.getType();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), varIndex);
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; i < this.caseCount;)
		{
			this.cases[i].writeStatement(writer, varIndex, elseLabel);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
			writer.writeFrameLabel(elseLabel);
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
		}
		
		// MatchError
		writer.writeFrameLabel(elseLabel);
		this.writeError(writer, type, type.getLoadOpcode(), varIndex);
		writer.resetLocals(varIndex);
		writer.writeFrameLabel(endLabel);
	}
	
	private void writeError(MethodWriter writer, IType type, int loadOpcode, int var)
	{
		if (this.exhaustive)
		{
			return;
		}
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

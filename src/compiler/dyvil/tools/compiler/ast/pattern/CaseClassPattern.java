package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

import org.objectweb.asm.Label;

public class CaseClassPattern extends ASTNode implements IPattern, IPatternList
{
	private IType		type;
	private IPattern[]	patterns	= new IPattern[2];
	private int			patternCount;
	
	public CaseClassPattern(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return CASE_CLASS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type.equals(this.type))
		{
			return this;
		}
		if (type.isSuperTypeOf(this.type))
		{
			return new TypeCheckPattern(this, this.type);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}
	
	@Override
	public int patternCount()
	{
		return 0;
	}
	
	@Override
	public void setPattern(int index, IPattern pattern)
	{
		this.patterns[index] = pattern;
	}
	
	@Override
	public void addPattern(IPattern pattern)
	{
		int index = this.patternCount++;
		if (index >= this.patterns.length)
		{
			IPattern[] temp = new IPattern[index + 1];
			System.arraycopy(this.patterns, 0, temp, 0, this.patterns.length);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
	}
	
	@Override
	public IPattern getPattern(int index)
	{
		return this.patterns[index];
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			IDataMember f = this.patterns[i].resolveField(name);
			if (f != null)
			{
				return f;
			}
		}
		return null;
	}
	
	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		
		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass == null)
		{
			return; // skip
		}
		
		int paramCount = iclass.parameterCount();
		if (this.patternCount != paramCount)
		{
			Marker m = markers.create(this.position, "pattern.class.count", this.type.toString());
			m.addInfo("Pattern Count: " + this.patternCount);
			m.addInfo("Class Parameter Count: " + paramCount);
			return;
		}
		
		for (int i = 0; i < paramCount; i++)
		{
			IParameter param = iclass.getParameter(i);
			IType type = param.getType().getConcreteType(this.type);
			IPattern pattern = this.patterns[i];
			IPattern pattern1 = pattern.withType(type);
			if (pattern1 == null)
			{
				Marker m = markers.create(this.position, "pattern.class.type", param.getName());
				m.addInfo("Pattern Type: " + pattern.getType());
				m.addInfo("Parameter Type: " + type);
			}
			else
			{
				this.patterns[i] = pattern = pattern1;
			}
			
			pattern.checkTypes(markers, context);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		IClass iclass = this.type.getTheClass();
		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}
			
			IDataMember field = iclass.getParameter(i);
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			field.writeGet(writer, null);
			this.patterns[i].writeInvJump(writer, -1, elseLabel);
		}
		
		writer.resetLocals(varIndex);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('(');
		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(')');
	}
}

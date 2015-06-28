package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

import org.objectweb.asm.Label;

public final class TuplePattern extends ASTNode implements IPattern, IPatternList
{
	private IPattern[]	patterns	= new IPattern[3];
	private int			patternCount;
	private IType		tupleType;
	
	public TuplePattern(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return TUPLE;
	}
	
	@Override
	public IType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType(this.patternCount);
		for (int i = 0; i < this.patternCount; i++)
		{
			t.addType(this.patterns[i].getType());
		}
		return this.tupleType = t;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (!TupleType.tupleClasses[this.patternCount].isSubTypeOf(type))
		{
			return null;
		}
		int typeTag = type.typeTag();
		if (typeTag != IType.GENERIC && typeTag != IType.TUPLE)
		{
			return null;
		}
		
		ITypeList typeList = (ITypeList) type;
		
		for (int i = 0; i < this.patternCount; i++)
		{
			if (!this.patterns[i].isType(typeList.getType(i)))
			{
				return null;
			}
		}
		this.tupleType = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (TupleType.isSuperType(type, this.patterns, this.patternCount))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int patternCount()
	{
		return this.patternCount;
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
		if (this.patternCount > this.patterns.length)
		{
			IPattern[] temp = new IPattern[this.patternCount];
			System.arraycopy(this.patterns, 0, temp, 0, index);
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
		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		ITypeList typeList = (ITypeList) this.tupleType;
		
		for (int i = 0; i < this.patternCount; i++)
		{
			IType type = typeList.getType(i);
			IPattern pattern = this.patterns[i];
			IPattern pattern1 = pattern.withType(type);
			if (pattern1 == null)
			{
				Marker m = markers.create(pattern.getPosition(), "tuple.pattern.type");
				m.addInfo("Pattern Type: " + pattern.getType());
				m.addInfo("Tuple Type: " + type);
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
		ITypeList typeList = (ITypeList) this.tupleType;
		String internal = this.tupleType.getInternalName();
		Label target = new Label();
		
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard Patterns
				continue;
			}
			
			// Copy below
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeFieldInsn(Opcodes.GETFIELD, internal, "_" + (i + 1), "Ljava/lang/Object;");
			writer.writeTypeInsn(Opcodes.CHECKCAST, typeList.getType(i).getInternalName());
			this.patterns[i].writeInvJump(writer, -1, target);
		}
		
		writer.resetLocals(varIndex);
		writer.writeJumpInsn(Opcodes.GOTO, elseLabel);
		writer.writeLabel(target);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		ITypeList typeList = (ITypeList) this.tupleType;
		String internal = this.tupleType.getInternalName();
		
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}
			
			// Copy above
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeFieldInsn(Opcodes.GETFIELD, internal, "_" + (i + 1), "Ljava/lang/Object;");
			writer.writeTypeInsn(Opcodes.CHECKCAST, typeList.getType(i).getInternalName());
			this.patterns[i].writeInvJump(writer, -1, elseLabel);
		}
		
		writer.resetLocals(varIndex);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}

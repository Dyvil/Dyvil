package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TuplePattern extends Pattern implements IPatternList
{
	private IPattern[] patterns = new IPattern[3];
	private int   patternCount;
	private IType tupleType;
	
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
	public IPattern withType(IType type, MarkerList markers)
	{
		IClass tupleClass = TupleType.getTupleClass(this.patternCount);
		if (tupleClass == null || !tupleClass.isSubTypeOf(type))
		{
			return null;
		}
		
		this.tupleType = type;
		for (int i = 0; i < this.patternCount; i++)
		{
			IType elementType = type.resolveTypeSafely(tupleClass.getTypeParameter(i));
			IPattern pattern = this.patterns[i];
			IPattern typedPattern = pattern.withType(elementType, markers);
			if (typedPattern == null)
			{
				Marker m = Markers.semantic(pattern.getPosition(), "pattern.tuple.element.type");
				m.addInfo(Markers.getSemantic("pattern.type", pattern.getType()));
				m.addInfo(Markers.getSemantic("tuple.element.type", elementType));
				markers.add(m);
			}
			else
			{
				this.patterns[i] = typedPattern;
			}
		}
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return TupleType.isSuperType(type, this.patterns, this.patternCount);
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
		if (this.patternCount == 1)
		{
			return this.patterns[0].resolve(markers, context);
		}

		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		varIndex = IPattern.ensureVar(writer, varIndex, matchedType);

		final int lineNumber = this.getLineNumber();
		final IClass tupleClass = this.tupleType.getTheClass();
		final String internalTupleClassName = this.tupleType.getInternalName();

		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}

			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			matchedType.writeCast(writer, this.tupleType, lineNumber);
			writer.visitFieldInsn(Opcodes.GETFIELD, internalTupleClassName, "_" + (i + 1), "Ljava/lang/Object;");
			final IType targetType = this.tupleType.resolveTypeSafely(tupleClass.getTypeParameter(i));

			Types.OBJECT.writeCast(writer, targetType, lineNumber);
			this.patterns[i].writeInvJump(writer, -1, targetType, elseLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.patternCount == 0)
		{
			if (Formatting.getBoolean("tuple.empty.space_between"))
			{
				buffer.append("( )");
			}
			else
			{
				buffer.append("()");
			}
			return;
		}

		buffer.append('(');
		if (Formatting.getBoolean("tuple.open_paren.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.getSeparator("tuple.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("tuple.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}

package dyvilx.tools.compiler.ast.pattern.object;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.PatternList;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.TupleType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Arrays;

public final class TuplePattern extends AbstractPattern implements PatternList
{
	private Pattern[] patterns;
	private int       patternCount;

	// Metadata
	private IType tupleType;

	public TuplePattern(SourcePosition position)
	{
		this.position = position;
		this.patterns = new Pattern[3];
	}

	@Override
	public int getPatternType()
	{
		return TUPLE;
	}

	@Override
	public int patternCount()
	{
		return this.patternCount;
	}

	@Override
	public Pattern get(int index)
	{
		return this.patterns[index];
	}

	@Override
	public void set(int index, Pattern pattern)
	{
		this.patterns[index] = pattern;
	}

	@Override
	public void add(Pattern pattern)
	{
		int index = this.patternCount++;
		if (this.patternCount > this.patterns.length)
		{
			Pattern[] temp = new Pattern[this.patternCount];
			System.arraycopy(this.patterns, 0, temp, 0, index);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
	}

	@Override
	public boolean isExhaustive()
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			if (!this.patterns[i].isExhaustive())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}

		final TupleType tupleType = new TupleType(this.patternCount);
		final TypeList arguments = tupleType.getArguments();
		for (int i = 0; i < this.patternCount; i++)
		{
			arguments.add(this.patterns[i].getType());
		}
		return this.tupleType = tupleType;
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		final IClass tupleClass = TupleType.getTupleClass(this.patternCount);
		if (tupleClass == null)
		{
			return null;
		}

		final IType tupleType = tupleClass.getClassType();
		if (!Types.isSuperClass(type, tupleType))
		{
			return null;
		}

		this.tupleType = null; // reset type to recompute it later
		final TypeParameterList typeParameters = tupleClass.getTypeParameters();

		for (int i = 0; i < this.patternCount; i++)
		{
			final IType elementType = Types.resolveTypeSafely(type, typeParameters.get(i));
			final Pattern pattern = this.patterns[i];
			final Pattern typedPattern = pattern.withType(elementType, markers);

			if (typedPattern == null)
			{
				final Marker marker = Markers.semanticError(pattern.getPosition(), "pattern.tuple.element.type");
				marker.addInfo(Markers.getSemantic("pattern.type", pattern.getType()));
				marker.addInfo(Markers.getSemantic("tuple.element.type", elementType));
				markers.add(marker);
			}
			else
			{
				this.patterns[i] = typedPattern;
			}
		}

		if (!Types.isSuperClass(tupleType, type))
		{
			return new TypeCheckPattern(this, type, tupleType);
		}
		return this;
	}

	@Override
	public Object getConstantValue()
	{
		final Object[] subValues = new Object[this.patternCount];
		for (int i = 0; i < this.patternCount; i++)
		{
			final Object subValue = this.get(i).getConstantValue();
			if (subValue == null)
			{
				return null;
			}

			subValues[i] = subValue;
		}

		return new TupleSurrogate(subValues);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			final IDataMember field = this.patterns[i].resolveField(name);
			if (field != null)
			{
				return field;
			}
		}

		return null;
	}

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
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
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		varIndex = Pattern.ensureVar(writer, varIndex);

		final int lineNumber = this.lineNumber();
		final IType tupleType = this.getType();
		final IClass tupleClass = tupleType.getTheClass();
		final TypeParameterList typeParameters = tupleClass.getTypeParameters();
		final String internalTupleClassName = tupleType.getInternalName();

		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].isWildcard())
			{
				// Skip wildcard patterns
				continue;
			}

			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			writer.visitFieldInsn(Opcodes.GETFIELD, internalTupleClassName, "_" + (i + 1), "Ljava/lang/Object;");

			final IType targetType = Types.resolveTypeSafely(tupleType, typeParameters.get(i));

			Types.OBJECT.writeCast(writer, targetType, lineNumber);
			this.patterns[i].writeJumpOnMismatch(writer, -1, target);
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
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

		Util.astToString(indent, this.patterns, this.patternCount, Formatting.getSeparator("tuple.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("tuple.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}

class TupleSurrogate
{
	private final Object[] values;

	public TupleSurrogate(Object[] values)
	{
		this.values = values;
	}

	@Override
	public boolean equals(Object o)
	{
		return this == o || o != null && this.getClass() == o.getClass() //
		                    && Arrays.equals(this.values, ((TupleSurrogate) o).values);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(this.values);
	}

	@Override
	public String toString()
	{
		return dyvil.collection.immutable.ArrayList.apply(this.values).toString("(", ", ", ")");
	}
}

package dyvilx.tools.compiler.ast.pattern.object;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.DummyValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.ClassAccess;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.PatternList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.ast.type.compound.TupleType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class UnapplyPattern extends AbstractPattern implements PatternList
{
	protected IType type;

	protected Pattern[] patterns = new Pattern[2];
	protected int patternCount;

	// Metadata
	protected IValue  unapplyCall;
	private   Integer switchValue;

	public UnapplyPattern(SourcePosition position)
	{
		this.position = position;
	}

	public UnapplyPattern(SourcePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	@Override
	public int getPatternType()
	{
		return CASE_CLASS;
	}

	@Override
	public boolean isExhaustive()
	{
		if (this.unapplyCall == null || NullableType.isNullable(this.unapplyCall.getType()))
		{
			return false;
		}

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
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		// PatternType.unapply(_ : MatchedType)

		final IClass matchClass = this.type.getTheClass();
		if (matchClass == null)
		{
			return null;
		}

		final MethodCall methodCall = new MethodCall(this.position, new ClassAccess(this.type), Names.unapply,
		                                             new ArgumentList(new DummyValue(type)));
		final IValue resolvedCall = methodCall.resolveCall(MarkerList.BLACKHOLE, matchClass, false);
		return resolvedCall != null && this.withMethod(type, resolvedCall, markers) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		return true;
	}

	protected boolean withMethod(IType matchedType, IValue methodCall, MarkerList markers)
	{
		final IType type = NullableType.unapply(methodCall.getType());
		final String className = type.getInternalName();
		if (!className.startsWith("dyvil/tuple/Tuple$Of")) // return type must be a tuple type
		{
			return false;
		}

		final TupleType tupleType = type.extract(TupleType.class);
		final TypeList typeArguments = tupleType.getArguments();

		if (this.patternCount != typeArguments.size())
		{
			final Marker marker = Markers.semanticError(this.position, "pattern.unapply.count", this.type.toString());
			marker.addInfo(Markers.getSemantic("pattern.unapply.count.pattern", this.patternCount));
			marker.addInfo(Markers.getSemantic("pattern.unapply.count.class", typeArguments.size()));
			markers.add(marker);
			return true;
		}

		this.unapplyCall = methodCall;

		for (int i = 0; i < this.patternCount; i++)
		{
			final IType subType = typeArguments.get(i);

			final Pattern pattern = this.patterns[i];
			final Pattern typedPattern = pattern.withType(subType, markers);

			if (typedPattern == null)
			{
				final Marker marker = Markers.semanticError(this.position, "pattern.unapply.type");
				marker.addInfo(Markers.getSemantic("pattern.type", pattern.getType()));
				marker.addInfo(Markers.getSemantic("classparameter.type", subType));
				markers.add(marker);
			}
			else
			{
				this.patterns[i] = typedPattern;
			}
		}

		if (Types.isSuperClass(matchedType, this.type) // matched type is super-type of enclosing type
		    && this.type.getAnnotation(Types.SWITCHOPTIMIZED_CLASS) != null // and both types are @SwitchOptimized
		    && matchedType.getAnnotation(Types.SWITCHOPTIMIZED_CLASS) != null)
		{
			// Compute the switch value from the hash code of the class name
			this.switchValue = ClassFormat.internalToPackage(this.type.getInternalName()).hashCode();
		}

		return true;
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
		final int index = this.patternCount++;
		if (index >= this.patterns.length)
		{
			final Pattern[] temp = new Pattern[index + 1];
			System.arraycopy(this.patterns, 0, temp, 0, this.patterns.length);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
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
		this.type = this.type.resolveType(markers, context);

		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}

		return this;
	}

	// Switch Resolution

	@Override
	public boolean isSwitchable()
	{
		return this.switchValue != null;
	}

	@Override
	public boolean switchCheck()
	{
		return true;
	}

	@Override
	public int switchValue()
	{
		return this.switchValue;
	}

	// Compilation

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);

		final int lineNumer = this.lineNumber();
		final int tupleVarIndex = writer.localCount();

		this.unapplyCall.writeExpression(writer, null);
		final IType methodType = this.unapplyCall.getType();

		final String internalType = methodType.getInternalName();
		final TupleType tupleType = NullableType.unapply(methodType).extract(TupleType.class);
		final TypeList typeArgs = tupleType.getArguments();

		if (methodType != tupleType) // nullable
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitVarInsn(Opcodes.ASTORE, tupleVarIndex);
			writer.visitJumpInsn(Opcodes.IFNULL, target);
		}
		else
		{
			writer.visitVarInsn(Opcodes.ASTORE, tupleVarIndex);
		}

		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].isWildcard())
			{
				// Skip wildcard patterns
				continue;
			}

			final IType targetType = typeArgs.get(i);

			writer.visitVarInsn(Opcodes.ALOAD, tupleVarIndex);

			// Get and cast the tuple element
			// FIXME not ready for Tuple.OfN
			writer.visitFieldInsn(Opcodes.GETFIELD, internalType, "_" + (i + 1), "Ljava/lang/Object;");
			Types.OBJECT.writeCast(writer, targetType, lineNumer);

			// Check the pattern
			this.patterns[i].writeJumpOnMismatch(writer, -1, target);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.getSeparator("parameters.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("parameters.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}

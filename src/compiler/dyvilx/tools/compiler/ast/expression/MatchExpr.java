package dyvilx.tools.compiler.ast.expression;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.collection.mutable.HashSet;
import dyvil.lang.Formattable;
import dyvil.math.MathUtils;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.consumer.ICaseConsumer;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Arrays;

public final class MatchExpr implements IValue, ICaseConsumer, IValueConsumer
{
	protected SourcePosition position;

	protected IValue matchedValue;
	protected MatchCase[] cases = new MatchCase[3];
	protected int caseCount;

	// Metadata
	private boolean exhaustive;
	private IType   returnType;

	public MatchExpr(SourcePosition position)
	{
		this.position = position;
	}

	public MatchExpr(SourcePosition position, IValue matchedValue)
	{
		this.position = position;
		this.matchedValue = matchedValue;
	}

	public MatchExpr(IValue matchedValue, MatchCase[] cases)
	{
		this.matchedValue = matchedValue;
		this.cases = cases;
		this.caseCount = cases.length;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return MATCH;
	}

	public IValue getValue()
	{
		return this.matchedValue;
	}

	@Override
	public void setValue(IValue value)
	{
		this.matchedValue = value;
	}

	@Override
	public void addCase(MatchCase matchCase)
	{
		int index = this.caseCount++;
		if (index >= this.cases.length)
		{
			MatchCase[] temp = new MatchCase[this.caseCount];
			System.arraycopy(this.cases, 0, temp, 0, index);
			this.cases = temp;
		}
		this.cases[index] = matchCase;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null && !action.isUsableAsStatement())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isResolved()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].action.isResolved())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public IType getType()
	{
		if (this.returnType != null)
		{
			return this.returnType;
		}

		int len = this.caseCount;
		if (len == 0)
		{
			this.returnType = Types.VOID;
			return this.returnType;
		}

		IType t = null;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.cases[i].action;
			if (v == null)
			{
				continue;
			}
			IType t1 = v.getType();
			if (t == null)
			{
				t = t1;
				continue;
			}

			t = Types.combine(t, t1);
		}

		if (t == null)
		{
			return this.returnType = Types.VOID;
		}
		return this.returnType = t;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final IValue action = matchCase.action;

			if (action == null)
			{
				continue;
			}

			matchCase.action = TypeChecker.convertValue(action, type, typeContext, markers, context,
			                                            TypeChecker.markerSupplier("match.value.type.incompatible"));
		}

		return Types.isVoid(type) || Types.isSuperType(type, this.getType()) ? this : null;
	}

	@Override
	public boolean isType(IType type)
	{
		if (Types.isVoid(type))
		{
			return true;
		}

		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null && !action.isType(type))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.caseCount == 0)
		{
			return MISMATCH;
		}

		int min = Integer.MAX_VALUE;
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action == null)
			{
				continue;
			}

			final int match = TypeChecker.getTypeMatch(action, type, implicitContext);
			if (match == MISMATCH)
			{
				return MISMATCH;
			}
			if (match < min)
			{
				min = match;
			}
		}

		// min might be unchanged in case all actions were null
		return min == Integer.MAX_VALUE ? MISMATCH : min;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.matchedValue != null)
		{
			this.matchedValue.resolveTypes(markers, context);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			final IValue action = this.cases[i].action;
			if (action != null)
			{
				action.resolveStatement(context, markers);
			}
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IType type;
		if (this.matchedValue != null)
		{
			this.matchedValue = this.matchedValue.resolve(markers, context);
			type = this.matchedValue.getType();

			this.matchedValue = this.matchedValue.withType(type, type, markers, context);
		}
		else
		{
			type = Types.ANY;
			markers.add(Markers.semantic(this.position, "match.invalid"));
		}

		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase c = this.cases[i];
			if (this.exhaustive)
			{
				markers.add(Markers.semantic(c.getPattern().getPosition(), "pattern.dead"));
			}

			c.resolve(markers, type, context);
			if (c.pattern != null && c.isExhaustive())
			{
				this.exhaustive = true;
			}
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.matchedValue != null)
		{
			this.matchedValue.checkTypes(markers, context);
		}

		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.matchedValue != null)
		{
			this.matchedValue.check(markers, context);
		}

		final Set<Object> values = new HashSet<>(this.caseCount);
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final Pattern pattern = matchCase.pattern;

			matchCase.check(markers, context);

			for (int j = 0, subPatterns = pattern.subPatterns(); j < subPatterns; j++)
			{
				final Pattern subPattern = pattern.subPattern(j);
				final Object constantValue = subPattern.constantValue();

				if (constantValue != null && !values.add(constantValue))
				{
					markers.add(Markers.semanticError(subPattern.getPosition(), "match.case.duplicate", constantValue));
				}
			}
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.matchedValue != null)
		{
			this.matchedValue = this.matchedValue.foldConstants();
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.matchedValue != null)
		{
			this.matchedValue = this.matchedValue.cleanup(compilableList, classCompilableList);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.returnType;
		}

		// type.getFrameType() returns null for void. This is specially handled in the implementations, so we don't need
		// to handle void here

		if (this.canGenerateSwitch())
		{
			this.generateSwitch(writer, type.getFrameType());
		}
		else
		{
			this.generateBranched(writer, type.getFrameType());
		}
	}

	private boolean canGenerateSwitch()
	{
		// First run: Determine if a switch instruction can be generated
		for (int i = 0; i < this.caseCount; i++)
		{
			if (!this.cases[i].pattern.isSwitchable())
			{
				return false;
			}
		}

		return true;
	}

	private void generateBranched(MethodWriter writer, Object frameType) throws BytecodeException
	{
		final boolean expr = frameType != null;
		final int varIndex = writer.localCount();
		final IType matchedType = this.matchedValue.getType();

		this.matchedValue.writeExpression(writer, null);
		writer.visitVarInsn(matchedType.getStoreOpcode(), varIndex);

		final int localCount = writer.localCount();

		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; ; )
		{
			MatchCase c = this.cases[i];
			IValue condition = c.condition;

			c.pattern.writeJumpOnMismatch(writer, varIndex, elseLabel);
			if (condition != null)
			{
				condition.writeInvJump(writer, elseLabel);
			}

			this.writeAction(writer, expr, frameType, c.action);

			writer.resetLocals(localCount);

			if (!writer.hasReturn())
			{
				writer.visitJumpInsn(Opcodes.GOTO, endLabel);
			}

			writer.visitTargetLabel(elseLabel);
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
			else
			{
				break;
			}
		}

		// MatchError
		writer.visitLabel(elseLabel);
		if (!this.exhaustive)
		{
			this.writeMatchError(writer, varIndex, matchedType);
		}

		writer.visitLabel(endLabel);
		writer.resetLocals(varIndex);
	}

	private void writeMatchError(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		final int lineNumber = this.lineNumber();

		writer.visitTypeInsn(Opcodes.NEW, "dyvil/util/MatchError");

		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(matchedType.getLoadOpcode(), varIndex);
		matchedType.writeCast(writer, Types.OBJECT, lineNumber);

		writer.visitLineNumber(lineNumber);
		writer
			.visitMethodInsn(Opcodes.INVOKESPECIAL, "dyvil/util/MatchError", "<init>", "(Ljava/lang/Object;)V", false);
		writer.visitInsn(Opcodes.ATHROW);
		writer.setHasReturn(false);
	}

	private void writeAction(MethodWriter writer, boolean expr, Object frameType, IValue value) throws BytecodeException
	{
		if (expr)
		{
			if (value != null)
			{
				value.writeExpression(writer, this.returnType);
			}
			else
			{
				this.returnType.writeDefaultValue(writer);
			}

			if (!writer.hasReturn())
			{
				writer.getFrame().set(frameType);
			}
		}
		else if (value != null)
		{
			value.writeExpression(writer, Types.VOID);
		}
	}

	private void generateSwitch(MethodWriter writer, Object frameType) throws BytecodeException
	{
		MatchCase defaultCase = null;
		Label defaultLabel = null;
		int cases = 0;
		int low = Integer.MAX_VALUE; // the minimum int
		int high = Integer.MIN_VALUE; // the maximum int
		boolean switchVar = false; // Do we need to store the value in a variable (for string equality checks later)

		// Second run: count the number of total cases, the minimum and maximum
		// int value, find the default case, and find out if a variable needs to
		// generated.
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase matchCase = this.cases[i];
			Pattern pattern = matchCase.pattern;

			if (switchVar || pattern.switchCheck())
			{
				switchVar = true;
			}

			if (pattern.isExhaustive())
			{
				defaultCase = matchCase;
				defaultLabel = new Label();
				continue;
			}

			int min = pattern.minValue();
			if (min < low)
			{
				low = min;
			}

			int max = pattern.maxValue();
			if (max > high)
			{
				high = max;
			}

			cases += pattern.subPatterns();
		}

		// Check if a match error should be generated - Non-exhaustive pattern
		// and no default label
		final Label endLabel = new Label();
		Label matchErrorLabel = null;

		if (!this.exhaustive)
		{
			// Need a variable for MatchError
			switchVar = true;
			matchErrorLabel = new Label();

			if (defaultLabel == null)
			{
				defaultLabel = matchErrorLabel;
			}
		}
		else if (defaultLabel == null)
		{
			// Exhaustive pattern - default label is end label
			defaultLabel = endLabel;
		}

		final boolean expr = frameType != null;

		// Write the value
		final IType matchedType = this.matchedValue.getType();
		this.matchedValue.writeExpression(writer, null);

		int varIndex = -1;
		if (switchVar)
		{
			varIndex = writer.localCount();
			// Need a variable - store and load the value
			writer.visitVarInsn(matchedType.getStoreOpcode(), varIndex);
			writer.visitVarInsn(matchedType.getLoadOpcode(), varIndex);
		}

		if (Types.isSuperClass(Types.ENUM, matchedType))
		{
			// Enum - we need the name and the hashCode of that
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Enum", "name", "()Ljava/lang/String;", false);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}
		else if (Types.isSuperType(Types.STRING, matchedType))
		{
			// String - we need the hashCode
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}

		final int localCount = writer.localCount();

		final KeyCache keyCache = new KeyCache(cases);

		// Third run: fill the Key Cache
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final Pattern pattern = matchCase.pattern;
			final int subPatterns = pattern.subPatterns();

			for (int j = 0; j < subPatterns; j++)
			{
				final Pattern subPattern = pattern.subPattern(j);
				if (subPattern.isExhaustive())
				{
					continue;
				}

				final int switchValue = subPattern.switchValue();
				keyCache.add(switchValue, matchCase, subPattern);
			}
		}

		final Collection<KeyCache.Entry> entries = keyCache.uniqueEntries();

		// Generate switch target labels
		for (KeyCache.Entry topEntry : entries)
		{
			for (KeyCache.Entry entry = topEntry; entry != null; entry = entry.next)
			{
				entry.switchLabel = new Label();
			}
		}

		// Choose and generate the appropriate instruction
		if (useTableSwitch(low, high, cases))
		{
			this.writeTableSwitch(writer, entries, defaultLabel, low, high);
		}
		else
		{
			this.writeLookupSwitch(writer, entries, defaultLabel, cases);
		}

		// Fourth run: generate the target labels
		for (KeyCache.Entry topEntry : entries)
		{
			KeyCache.Entry entry = topEntry;
			final int key = entry.key;

			do
			{
				final KeyCache.Entry next = entry.next;
				final MatchCase matchCase = entry.matchCase;
				final Pattern pattern = entry.pattern;

				Label elseLabel;
				if (next != null && next.key == key)
				{
					elseLabel = next.switchLabel;
					if (elseLabel == null)
					{
						elseLabel = next.switchLabel = new Label();
					}
				}
				else
				{
					elseLabel = defaultLabel;
				}

				writer.visitTargetLabel(entry.switchLabel);

				if (pattern.switchCheck())
				{
					pattern.writeJumpOnMismatch(writer, varIndex, elseLabel);
				}

				if (matchCase.condition != null)
				{
					matchCase.condition.writeInvJump(writer, elseLabel);
				}

				this.writeAction(writer, expr, frameType, matchCase.action);

				writer.resetLocals(localCount);

				if (!writer.hasReturn())
				{
					writer.visitJumpInsn(Opcodes.GOTO, endLabel);
				}

				entry = next;
			}
			while (entry != null && entry.key == key);
		}

		// Default Case
		if (defaultCase != null)
		{
			writer.visitTargetLabel(defaultLabel);

			if (defaultCase.pattern.switchCheck())
			{
				defaultCase.pattern.writeJumpOnMismatch(writer, varIndex, matchErrorLabel);
			}

			if (defaultCase.condition != null)
			{
				defaultCase.condition.writeInvJump(writer, matchErrorLabel);
			}

			this.writeAction(writer, expr, frameType, defaultCase.action);

			writer.resetLocals(localCount);

			if (!writer.hasReturn())
			{
				writer.visitJumpInsn(Opcodes.GOTO, endLabel);
			}
		}

		// Generate Match Error
		if (matchErrorLabel != null)
		{
			writer.visitLabel(matchErrorLabel);
			this.writeMatchError(writer, varIndex, matchedType);
		}

		writer.visitLabel(endLabel);

		if (switchVar)
		{
			writer.resetLocals(varIndex);
		}
	}

	/**
	 * Determines whether to generate a {@code tableswitch} or a {@code lookupswitch} instruction, and returns {@code
	 * true} when a {@code tableswitch} should be generated.
	 *
	 * @param low
	 * 	the lowest value
	 * @param high
	 * 	the highest value
	 * @param count
	 * 	the number of cases
	 *
	 * @return true, if a tableswitch instruction should be used
	 */
	private static boolean useTableSwitch(int low, int high, int count)
	{
		// this calculation can cause integer overflow with string hash codes of large absolute value
		final long tableSpace = 4L + (long) high - (long) low + 1L;
		final int tableTime = 3; // constant time
		final int lookupSpace = 3 + 2 * count;
		final int lookupTime = MathUtils.log2(count); // binary search O(log n)
		return count > 0 && tableSpace + 3 * tableTime <= lookupSpace + 3 * lookupTime;
	}

	/**
	 * Generates a {@code lookupswitch} instruction
	 */
	private void writeLookupSwitch(MethodWriter writer, Collection<KeyCache.Entry> entries, Label defaultLabel,
		                              int cases) throws BytecodeException
	{
		if (cases <= 0)
		{
			return;
		}

		final int length = entries.size();

		// Generate a LOOKUPSWITCH instruction
		int[] keys = new int[length];
		Label[] handlers = new Label[length];
		int index = 0;

		for (KeyCache.Entry entry : entries)
		{
			keys[index] = entry.key;
			handlers[index] = entry.switchLabel;
			index++;
		}

		writer.visitLookupSwitchInsn(defaultLabel, keys, handlers);
	}

	/**
	 * Generates a {@code tableswitch} instruction
	 */
	private void writeTableSwitch(MethodWriter writer, Collection<KeyCache.Entry> entries, Label defaultLabel, int low,
		                             int high) throws BytecodeException
	{
		assert defaultLabel != null;

		// this calculation can cause integer overflow with string hash codes of large absolute value
		final int size = (int) ((long) high - (long) low + 1L);
		final Label[] handlers = new Label[size];
		Arrays.fill(handlers, defaultLabel);

		for (KeyCache.Entry entry : entries)
		{
			handlers[entry.key - low] = entry.switchLabel;
		}

		writer.visitTableSwitchInsn(low, high, defaultLabel, handlers);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.matchedValue.toString(prefix, buffer);
		if (this.caseCount == 1 && Formatting.getBoolean("match.convert_single"))
		{
			buffer.append(" match ");
			this.cases[0].toString(prefix, buffer);
			return;
		}

		if (Formatting.getBoolean("match.newline_after"))
		{
			buffer.append(" match\n").append(prefix).append("{\n");
		}
		else
		{
			buffer.append(" match {\n");
		}

		String casePrefix = Formatting.getIndent("match.indent", prefix);
		for (int i = 0; i < this.caseCount; i++)
		{
			buffer.append(casePrefix);
			this.cases[i].toString(casePrefix, buffer);
			buffer.append('\n');
		}
		buffer.append(prefix).append('}');
	}
}

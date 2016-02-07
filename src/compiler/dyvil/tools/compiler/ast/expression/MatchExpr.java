package dyvil.tools.compiler.ast.expression;

import dyvil.collection.Collection;
import dyvil.math.MathUtils;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.util.Arrays;

public final class MatchExpr implements IValue
{
	protected ICodePosition position;
	
	protected IValue matchedValue;
	protected MatchCase[] cases = new MatchCase[3];
	protected int caseCount;
	
	// Metadata
	private boolean exhaustive;
	private IType   returnType;
	
	public MatchExpr(ICodePosition position, IValue matchedValue)
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
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return MATCH;
	}
	
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
	public boolean isPrimitive()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i].action;
			if (v != null && !v.isPrimitive())
			{
				return false;
			}
		}
		return true;
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
		return this.returnType != null && this.returnType.isResolved();
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
			MatchCase matchCase = this.cases[i];
			IValue action = matchCase.action;
			
			if (action == null)
			{
				continue;
			}
			
			IValue value = IType.convertValue(action, type, typeContext, markers, context);
			if (value == null)
			{
				Marker marker = Markers.semantic(action.getPosition(), "match.value.type.incompatible");
				marker.addInfo(Markers.getSemantic("type.expected", type.getConcreteType(typeContext)));
				marker.addInfo(Markers.getSemantic("value.type", action.getType()));
				markers.add(marker);
			}
			else
			{
				matchCase.action = value;
			}
		}
		
		return type == Types.VOID || type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i].action;
			if (v != null && !v.isType(type))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.caseCount == 0)
		{
			return 0;
		}
		
		float total = 0F;
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i].action;
			if (v == null)
			{
				continue;
			}
			
			float f = v.getTypeMatch(type);
			if (f == 0)
			{
				return 0;
			}
			total += f;
		}
		return 1 + total / this.caseCount;
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

		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].check(markers, context);
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.matchedValue != null)
		{
			this.matchedValue = this.matchedValue.cleanup(context, compilableList);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].cleanup(context, compilableList);
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
		writer.writeVarInsn(matchedType.getStoreOpcode(), varIndex);

		final int localCount = writer.localCount();
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0; ; )
		{
			MatchCase c = this.cases[i];
			IValue condition = c.condition;
			
			c.pattern.writeInvJump(writer, varIndex, matchedType, elseLabel);
			if (condition != null)
			{
				condition.writeInvJump(writer, elseLabel);
			}
			
			this.writeAction(writer, expr, frameType, c.action);
			
			writer.resetLocals(localCount);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
			writer.writeLabel(elseLabel);
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
		writer.writeLabel(elseLabel);
		if (!this.exhaustive)
		{
			this.writeMatchError(writer, varIndex, matchedType);
		}

		writer.writeLabel(endLabel);
		writer.resetLocals(varIndex);
	}
	
	private void writeMatchError(MethodWriter writer, int varIndex, IType matchedType) throws BytecodeException
	{
		String desc = matchedType.isPrimitive() ? "(" + matchedType.getExtendedName() + ")V" : "(Ljava/lang/Object;)V";
		
		writer.writeTypeInsn(Opcodes.NEW, "dyvil/util/MatchError");
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(matchedType.getLoadOpcode(), varIndex);
		writer.writeLineNumber(this.getLineNumber());
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/util/MatchError", "<init>", desc, false);
		writer.writeInsn(Opcodes.ATHROW);
		writer.setHasReturn(false);
	}
	
	private void writeAction(MethodWriter writer, boolean expr, Object frameType, IValue value) throws BytecodeException
	{
		if (value != null)
		{
			if (expr)
			{
				value.writeExpression(writer, this.returnType);
				writer.getFrame().set(frameType);
			}
			else
			{
				value.writeExpression(writer, Types.VOID);
			}
		}
		else if (expr)
		{
			this.returnType.writeDefaultValue(writer);
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
			IPattern pattern = matchCase.pattern;
			
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
			writer.writeVarInsn(matchedType.getStoreOpcode(), varIndex);
			writer.writeVarInsn(matchedType.getLoadOpcode(), varIndex);
		}
		
		// Not a primitive type (String) - we need the hashCode
		if (!matchedType.isPrimitive())
		{
			writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}
		
		final int localCount = writer.localCount();

		final KeyCache keyCache = new KeyCache(cases);

		// Third run: fill the Key Cache
		for (int i = 0; i < this.caseCount; i++)
		{
			final MatchCase matchCase = this.cases[i];
			final IPattern pattern = matchCase.pattern;
			final int subPatterns = pattern.subPatterns();

			for (int j = 0; j < subPatterns; j++)
			{
				final IPattern subPattern = pattern.subPattern(j);
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
				final IPattern pattern = entry.pattern;

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

				writer.writeTargetLabel(entry.switchLabel);

				if (pattern.switchCheck())
				{
					pattern.writeInvJump(writer, varIndex, matchedType, elseLabel);
				}

				if (matchCase.condition != null)
				{
					matchCase.condition.writeInvJump(writer, elseLabel);
				}

				this.writeAction(writer, expr, frameType, matchCase.action);

				writer.resetLocals(localCount);
				writer.writeJumpInsn(Opcodes.GOTO, endLabel);

				entry = next;
			}
			while (entry != null && entry.key == key);
		}

		// Default Case
		if (defaultCase != null)
		{
			writer.writeTargetLabel(defaultLabel);

			if (defaultCase.condition != null)
			{
				assert matchErrorLabel != null;
				defaultCase.condition.writeInvJump(writer, matchErrorLabel);
			}

			this.writeAction(writer, expr, frameType, defaultCase.action);

			writer.resetLocals(localCount);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
		}
		
		// Generate Match Error
		if (matchErrorLabel != null)
		{
			writer.writeLabel(matchErrorLabel);
			this.writeMatchError(writer, varIndex, matchedType);
		}
		
		writer.writeLabel(endLabel);
		
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
	 * 		the lowest value
	 * @param high
	 * 		the highest value
	 * @param count
	 * 		the number of cases
	 *
	 * @return true, if a tableswitch instruction should be used
	 */
	private static boolean useTableSwitch(int low, int high, int count)
	{
		int tableSpace = 4 + high - low + 1;
		int tableTime = 3; // constant time
		int lookupSpace = 3 + 2 * count;
		int lookupTime = MathUtils.logBaseTwo(count); // binary search O(log n)
		return count > 0 && tableSpace + 3 * tableTime <= lookupSpace + 3 * lookupTime;
	}
	
	/**
	 * Generates a {@code lookupswitch} instruction
	 */
	private void writeLookupSwitch(MethodWriter writer, Collection<KeyCache.Entry> entries, Label defaultLabel, int cases)
			throws BytecodeException
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

		writer.writeLookupSwitch(defaultLabel, keys, handlers);
	}
	
	/**
	 * Generates a {@code tableswitch} instruction
	 */
	private void writeTableSwitch(MethodWriter writer, Collection<KeyCache.Entry> entries, Label defaultLabel, int low, int high)
			throws BytecodeException
	{
		assert defaultLabel != null;

		final Label[] handlers = new Label[high - low + 1];
		Arrays.fill(handlers, defaultLabel);

		for (KeyCache.Entry entry : entries)
		{
			handlers[entry.key - low] = entry.switchLabel;
		}
		
		writer.writeTableSwitch(defaultLabel, low, high, handlers);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
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

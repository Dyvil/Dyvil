package dyvil.tools.compiler.ast.expression;

import java.util.Arrays;

import dyvil.math.MathUtils;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.IASTNode;
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
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class MatchExpression implements IValue
{
	protected ICodePosition position;
	
	protected IValue		value;
	protected MatchCase[]	cases	= new MatchCase[3];
	protected int			caseCount;
	
	// Metadata
	private boolean	exhaustive;
	private IType	type;
	
	public MatchExpression(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	public MatchExpression(IValue value, MatchCase[] cases)
	{
		this.value = value;
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
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}
		
		int len = this.caseCount;
		if (len == 0)
		{
			this.type = Types.VOID;
			return this.type;
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
			if (t == null)
			{
				return this.type = Types.VOID;
			}
		}
		
		if (t == null)
		{
			return this.type = Types.VOID;
		}
		return this.type = t;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : IValue.autoBox(this, this.getType(), type);
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
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
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
			this.cases[i].action.resolveStatement(context, markers);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IType type;
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
			type = this.value.getType();
			
			this.value = this.value.withType(type, type, markers, context);
		}
		else
		{
			type = Types.ANY;
			markers.add(this.position, "match.invalid");
		}
		
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase c = this.cases[i];
			if (this.exhaustive)
			{
				markers.add(c.getPattern().getPosition(), "pattern.dead");
			}
			
			c.resolve(markers, type, context);
			if (c.isExhaustive())
			{
				this.exhaustive = true;
			}
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
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
		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.write(writer, true);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.write(writer, false);
	}
	
	private void write(MethodWriter writer, boolean expr) throws BytecodeException
	{
		int varIndex = writer.localCount();
		Object frameType = expr ? this.type.getFrameType() : null;
		
		if (this.generateSwitch(writer, expr, frameType, varIndex))
		{
			writer.resetLocals(varIndex);
			return;
		}
		
		IType type = this.value.getType();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), varIndex);
		int localCount = writer.localCount();
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		for (int i = 0;;)
		{
			MatchCase c = this.cases[i];
			IValue condition = c.condition;
			
			c.pattern.writeInvJump(writer, varIndex, elseLabel);
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
			this.writeMatchError(writer, varIndex, type);
		}
		writer.writeLabel(endLabel);
		writer.resetLocals(varIndex);
	}
	
	private void writeMatchError(MethodWriter writer, int varIndex, IType type) throws BytecodeException
	{
		String desc = type.isPrimitive() ? "(" + type.getExtendedName() + ")V" : "(Ljava/lang/Object;)V";
		
		writer.writeTypeInsn(Opcodes.NEW, "dyvil/util/MatchError");
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(type.getLoadOpcode(), varIndex);
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
				value.writeExpression(writer);
				writer.getFrame().set(frameType);
			}
			else
			{
				value.writeStatement(writer);
			}
		}
		else if (expr)
		{
			this.type.writeDefaultValue(writer);
		}
	}
	
	private static class IntAndLabel implements Comparable<IntAndLabel>
	{
		int		integer;
		Label	label;
		
		public IntAndLabel(int integer, Label label)
		{
			this.integer = integer;
			this.label = label;
		}
		
		@Override
		public int compareTo(IntAndLabel o)
		{
			return Integer.compare(this.integer, o.integer);
		}
	}
	
	private boolean generateSwitch(MethodWriter writer, boolean expr, Object frameType, int varIndex) throws BytecodeException
	{
		int cases = 0;
		// Determine if a switch instruction can be generated, and count the
		// number of cases in total
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase matchCase = this.cases[i];
			if (matchCase.pattern.isExhaustive())
			{
				continue;
			}
			
			int count = matchCase.pattern.switchCases();
			if (count <= 0)
			{
				return false;
			}
			
			cases += count;
		}
		
		Label defaultLabel = null;
		IntAndLabel[] intLabels = new IntAndLabel[cases];
		int low = Integer.MAX_VALUE; // the minimum int
		int high = Integer.MIN_VALUE; // the maximum int
		int arrayIndex = 0; // counter for the intLabels array
		boolean switchVar = false; // Do we need to store the value in a
									// variable (for string equality checks
									// later)
		
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase matchCase = this.cases[i];
			IPattern pattern = matchCase.pattern;
			Label label = matchCase.switchLabel = new Label();
			
			if (pattern.isExhaustive())
			{
				defaultLabel = label;
				continue;
			}
			
			if (switchVar || pattern.switchCheck())
			{
				switchVar = true;
			}
			
			int count = pattern.switchCases();
			
			for (int j = 0; j < count; j++)
			{
				int caseValue = pattern.intValue(j);
				
				if (caseValue < low)
				{
					low = caseValue;
				}
				if (caseValue > high)
				{
					high = caseValue;
				}
				
				intLabels[arrayIndex++] = new IntAndLabel(caseValue, label);
			}
		}
		
		Label endLabel = new Label();
		boolean autoDefault = false;
		if (defaultLabel == null)
		{
			if (!this.exhaustive)
			{
				// Need a variable for MatchError
				switchVar = true;
			}
			
			defaultLabel = new Label();
			autoDefault = true;
		}
		
		IType type = this.value.getType();
		this.value.writeExpression(writer);
		
		if (switchVar)
		{
			writer.writeVarInsn(type.getStoreOpcode(), varIndex);
			writer.writeVarInsn(type.getLoadOpcode(), varIndex);
		}
		
		if (!type.isPrimitive())
		{
			writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}
		
		int localCount = writer.localCount();
		
		if (useTableSwitch(low, high, cases))
		{
			// Generate a TABLESWITCH instruction
			Label[] handlers = new Label[high - low + 1];
			Arrays.fill(handlers, defaultLabel);
			for (int i = 0; i < cases; i++)
			{
				IntAndLabel intAndLabel = intLabels[i];
				handlers[intAndLabel.integer - low] = intAndLabel.label;
			}
			
			writer.writeTableSwitch(defaultLabel, low, high, handlers);
		}
		else
		{
			// Generate a LOOKUPSWITCH instruction
			// Sort the Int-Label associations
			Arrays.sort(intLabels);
			
			Label[] handlers = new Label[cases];
			int[] keys = new int[cases];
			for (int i = 0; i < cases; i++)
			{
				IntAndLabel intAndLabel = intLabels[i];
				keys[i] = intAndLabel.integer;
				handlers[i] = intAndLabel.label;
			}
			
			writer.writeLookupSwitch(defaultLabel, keys, handlers);
		}
		
		for (int i = 0; i < this.caseCount; i++)
		{
			MatchCase matchCase = this.cases[i];
			
			writer.writeTargetLabel(matchCase.switchLabel);
			if (matchCase.condition != null)
			{
				matchCase.condition.writeInvJump(writer, defaultLabel);
			}
			
			if (matchCase.pattern.switchCheck())
			{
				matchCase.pattern.writeInvJump(writer, varIndex, defaultLabel);
			}
			
			this.writeAction(writer, expr, frameType, matchCase.action);
			
			writer.resetLocals(localCount);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
		}
		
		if (autoDefault && !this.exhaustive)
		{
			writer.writeLabel(defaultLabel);
			this.writeMatchError(writer, varIndex, type);
		}
		
		writer.writeLabel(endLabel);
		return true;
	}
	
	/**
	 * Determines whether to generate a {@code tableswitch} or a
	 * {@code lookupswitch} instruction, and returns {@code true} when a
	 * {@code tableswitch} should be generated.
	 * 
	 * @param low
	 *            the lowest value
	 * @param high
	 *            the highest value
	 * @param count
	 *            the number of cases
	 * @return true, if a tableswitch instruction should be used
	 */
	private static boolean useTableSwitch(int low, int high, int count)
	{
		int tableSpace = 4 + (high - low + 1);
		int tableTime = 3; // constant time
		int lookupSpace = 3 + 2 * count;
		int lookupTime = MathUtils.logBaseTwo(count); // binary search - O(log
														// n)
		return count > 0 && tableSpace + 3 * tableTime <= lookupSpace + 3 * lookupTime;
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
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
			this.cases[i].toString(prefix1, buffer);
			buffer.append('\n');
		}
		buffer.append(prefix).append('}');
	}
}

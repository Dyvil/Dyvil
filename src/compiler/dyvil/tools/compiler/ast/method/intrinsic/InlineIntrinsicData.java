package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.bytecode.IInstruction;
import dyvil.tools.compiler.ast.bytecode.InstructionList;
import dyvil.tools.compiler.ast.bytecode.VarInstruction;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class InlineIntrinsicData extends InstructionList implements IntrinsicData
{
	protected final IMethod method;

	private boolean preProcessed;

	private int storedParameters;
	private int returnIndex;
	private int maxLocals;
	private int parameterSlots;

	public InlineIntrinsicData(IMethod method)
	{
		this.method = method;
	}

	public void setMaxLocals(int maxLocals)
	{
		this.maxLocals = maxLocals;
	}

	private void preWrite(MethodWriter writer, IValue instance, IArguments arguments, int localCount)
	{
		if (!this.preProcessed)
		{
			this.preProcessed = true;

			final int[] accessCounts = new int[this.maxLocals];
			int lastStoredIndex = -1;

			for (int i = 0; i < this.instructionCount; i++)
			{
				final IInstruction instruction = this.instructions[i];
				final int opcode = instruction.getOpcode();

				if (Opcodes.isLoadOpcode(opcode))
				{
					final int varIndex = ((VarInstruction) instruction).getIndex();

					if (++accessCounts[varIndex] < 2)
					{
						// Local Variable loaded for the first time -> might not need to store it
						continue;
					}

					// Local Variable loaded at least two times -> need to store it and all parameters before
					if (varIndex > lastStoredIndex)
					{
						lastStoredIndex = varIndex;
					}
				}
				else if (Opcodes.isReturnOpcode(opcode))
				{
					this.returnIndex = i;
				}
			}

			this.storedParameters = lastStoredIndex + 1;

			int parameterSlots = 0;
			for (int i = 0, parameterCount = (this.method.parameterCount()); i < parameterCount; i++)
			{
				parameterSlots += this.method.getParameter(i).getInternalParameterType().getLocalSlots();
			}
			this.parameterSlots = parameterSlots;
		}

		for (int i = 0; i < this.storedParameters; i++)
		{
			final IType type = IntrinsicData.writeArgument(writer, this.method, i, instance, arguments);
			writer.visitVarInsn(type.getStoreOpcode(), localCount);
			localCount = writer.localCount();
		}
	}

	private void writeInstruction(IInstruction instruction, MethodWriter writer, IValue instance, IArguments arguments, int localCount)
	{
		final int opcode = instruction.getOpcode();
		if (Opcodes.isLoadOpcode(opcode))
		{
			final int varIndex = ((VarInstruction) instruction).getIndex();
			if (varIndex >= this.storedParameters && varIndex < this.parameterSlots)
			{
				// Accessing Parameter, not stored -> load it
				IntrinsicData.writeArgument(writer, this.method, varIndex, instance, arguments);
				return;
			}

			writer.visitVarInsn(instruction.getOpcode(), localCount + varIndex);
			return;
		}
		if (Opcodes.isStoreOpcode(opcode))
		{
			final int varIndex = ((VarInstruction) instruction).getIndex();
			writer.visitVarInsn(instruction.getOpcode(), localCount + varIndex);
			return;
		}

		instruction.write(writer);
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber)
		throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, instance, arguments, localCount);

		for (int i = 0; i < this.returnIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, instance, arguments, localCount);
		}

		writer.resetLocals(localCount);
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
		throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, instance, arguments, localCount);

		// Write all except the last Instruction
		final int lastIndex = this.returnIndex - 1;
		for (int i = 0; i < lastIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, instance, arguments, localCount);
		}

		// Write the last Instruction as a jump instruction
		final IInstruction lastInstruction = this.instructions[lastIndex];
		final int jumpOpcode = Opcodes.getJumpOpcode(lastInstruction.getOpcode());
		if (jumpOpcode >= 0)
		{
			writer.visitJumpInsn(jumpOpcode, dest);
		}
		else
		{
			this.writeInstruction(lastInstruction, writer, instance, arguments, localCount);
		}

		writer.resetLocals(localCount);
	}

	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
		throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, instance, arguments, localCount);

		// Write all except the last Instruction
		final int lastIndex = this.returnIndex - 1;
		for (int i = 0; i < lastIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, instance, arguments, localCount);
		}

		// Write the last Instruction as an inverse jump instruction
		final IInstruction lastInstruction = this.instructions[lastIndex];
		final int jumpOpcode = Opcodes.getInvJumpOpcode(lastInstruction.getOpcode());
		if (jumpOpcode >= 0)
		{
			writer.visitJumpInsn(jumpOpcode, dest);
		}
		else
		{
			this.writeInstruction(lastInstruction, writer, instance, arguments, localCount);
		}

		writer.resetLocals(localCount);
	}
}

package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.bytecode.IInstruction;
import dyvil.tools.compiler.ast.bytecode.InstructionList;
import dyvil.tools.compiler.ast.bytecode.VarInstruction;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameterList;
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
			this.preProcess();
		}

		for (int i = 0; i < this.storedParameters; i++)
		{
			final IType type = IntrinsicData.writeArgument(writer, this.method, i, instance, arguments);
			writer.visitVarInsn(type.getStoreOpcode(), localCount);
			localCount = writer.localCount();
		}
	}

	private void preProcess()
	{
		final IParameterList parameterList = this.method.getParameterList();
		int parameterSlots = 0;

		for (int i = 0, parameterCount = parameterList.size(); i < parameterCount; i++)
		{
			parameterSlots += parameterList.get(i).getInternalType().getLocalSlots();
		}
		this.parameterSlots = parameterSlots;

		final int[] accessCounts = new int[this.maxLocals];
		int lastStoredIndex = -1;

		for (int i = 0; i < this.instructionCount; i++)
		{
			final IInstruction instruction = this.instructions[i];
			final int opcode = instruction.getOpcode();

			if (Opcodes.isLoadOpcode(opcode))
			{
				final int varIndex = ((VarInstruction) instruction).getIndex();

				if (accessCounts[varIndex]++ == 0)
				{
					// Local Variable loaded for the first time -> might not need to store it
					continue;
				}

				// Local Variable loaded at least two times -> need to store it and all parameters before
				if (varIndex > lastStoredIndex && varIndex < parameterSlots)
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
	}

	private void writeInstruction(IInstruction instruction, MethodWriter writer, IValue instance, IArguments arguments,
		                             int localCount)
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
	public void writeIntrinsic(MethodWriter writer, IValue receiver, IArguments arguments, int lineNumber)
		throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, receiver, arguments, localCount);

		for (int i = 0; i < this.returnIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, receiver, arguments, localCount);
		}

		writer.resetLocals(localCount);
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue receiver, IArguments arguments, int lineNumber)
		throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, receiver, arguments, localCount);

		// Write all except the last Instruction
		final int lastIndex = this.returnIndex - 1;
		for (int i = 0; i < lastIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, receiver, arguments, localCount);
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
			this.writeInstruction(lastInstruction, writer, receiver, arguments, localCount);
			writer.visitJumpInsn(Opcodes.IFNE, dest);
		}

		writer.resetLocals(localCount);
	}

	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue receiver, IArguments arguments,
		                             int lineNumber) throws BytecodeException
	{
		final int localCount = writer.localCount();
		this.preWrite(writer, receiver, arguments, localCount);

		// Write all except the last Instruction
		final int lastIndex = this.returnIndex - 1;
		for (int i = 0; i < lastIndex; i++)
		{
			this.writeInstruction(this.instructions[i], writer, receiver, arguments, localCount);
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
			this.writeInstruction(lastInstruction, writer, receiver, arguments, localCount);
			writer.visitJumpInsn(Opcodes.IFEQ, dest);
		}

		writer.resetLocals(localCount);
	}
}

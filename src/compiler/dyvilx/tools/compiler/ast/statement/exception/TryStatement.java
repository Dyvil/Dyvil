package dyvilx.tools.compiler.ast.statement.exception;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.AbstractValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.statement.IStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.function.Consumer;

public final class TryStatement extends AbstractValue implements IDefaultContext
{
	// =============== Constants ===============

	public static final TypeChecker.MarkerSupplier CATCH_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"try.catch.type.incompatible", "type.expected", "try.catch.type");
	public static final TypeChecker.MarkerSupplier TRY_MARKER_SUPPLIER   = TypeChecker.markerSupplier(
		"try.action.type.incompatible", "type.expected", "try.action.type");

	// =============== Fields ===============

	protected IValue action;

	protected CatchBlock[] catchBlocks = new CatchBlock[1];
	protected int          catchBlockCount;

	protected IValue finallyBlock;

	// --------------- Metadata ---------------

	private IType commonType;

	// =============== Constructors ===============

	public TryStatement(SourcePosition position)
	{
		this.position = position;
	}

	// =============== Properties ===============

	public IValue getAction()
	{
		return this.action;
	}

	public void setAction(IValue action)
	{
		this.action = action;
	}

	public void addCatchBlock(CatchBlock block)
	{
		int index = this.catchBlockCount++;
		if (index >= this.catchBlocks.length)
		{
			CatchBlock[] temp = new CatchBlock[this.catchBlockCount];
			System.arraycopy(this.catchBlocks, 0, temp, 0, this.catchBlocks.length);
			this.catchBlocks = temp;
		}

		this.catchBlocks[index] = block;
	}

	public IValue getFinallyBlock()
	{
		return this.finallyBlock;
	}

	public void setFinallyBlock(IValue finallyBlock)
	{
		this.finallyBlock = finallyBlock;
	}

	@Override
	public boolean isResolved()
	{
		if (!this.action.isResolved())
		{
			return false;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (!this.catchBlocks[i].action.isResolved())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.commonType != null)
		{
			return this.commonType;
		}

		if (this.action == null)
		{
			return Types.UNKNOWN;
		}

		IType combinedType = this.action.getType();
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final IType catchBlockType = this.catchBlocks[i].action.getType();
			combinedType = Types.combine(combinedType, catchBlockType);
			if (combinedType == null)
			{
				return this.commonType = Types.ANY;
			}
		}
		return this.commonType = combinedType;
	}

	// =============== Methods ===============

	@Override
	public int valueTag()
	{
		return TRY;
	}

	// --------------- Typing ---------------

	@Override
	public boolean isType(IType type)
	{
		if (Types.isVoid(type))
		{
			return true;
		}
		if (this.action != null && !this.action.isType(type))
		{
			return false;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (!this.catchBlocks[i].action.isType(type))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			this.action = TypeChecker
				              .convertValue(this.action, type, typeContext, markers, context, TRY_MARKER_SUPPLIER);
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final CatchBlock block = this.catchBlocks[i];
			block.action = TypeChecker
				               .convertValue(block.action, type, typeContext, markers, context, CATCH_MARKER_SUPPLIER);
		}

		this.commonType = type;
		return this;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		int min = TypeChecker.getTypeMatch(this.action, type, implicitContext);
		if (min == MISMATCH)
		{
			return MISMATCH;
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final int blockMatch = TypeChecker.getTypeMatch(this.catchBlocks[i].action, type, implicitContext);
			if (blockMatch == MISMATCH)
			{
				return MISMATCH;
			}
			if (blockMatch < min)
			{
				min = blockMatch;
			}
		}

		return min;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			context = context.push(this);
			this.action.resolveTypes(markers, context);
			context = context.pop();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].resolveTypes(markers, context);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.action != null)
		{
			this.action.resolveStatement(context, markers);
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].action.resolveStatement(context, markers);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock.resolveStatement(context, markers);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			context = context.push(this);
			this.action = this.action.resolve(markers, context);
			context = context.pop();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].resolve(markers, context);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.resolve(markers, context);
			this.finallyBlock = IStatement.checkStatement(markers, context, this.finallyBlock, "try.finally.type");
		}

		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			context = context.push(this);
			this.action.checkTypes(markers, context);
			context = context.pop();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].checkTypes(markers, context);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.action != null)
		{
			context = context.push(this);
			this.action.check(markers, context);
			context = context.pop();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].check(markers, context);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock.check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].foldConstants();
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.action != null)
		{
			this.action = this.action.cleanup(compilableList, classCompilableList);
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].cleanup(compilableList, classCompilableList);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	// --------------- Context ---------------

	@Override
	public byte checkException(IType type)
	{
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (Types.isSuperType(this.catchBlocks[i].getType(), type))
			{
				return TRUE;
			}
		}
		return PASS;
	}

	// --------------- Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.getType();
		}

		final Label tryStart = new Label();
		final Label tryEnd = new Label();
		final Label endLabel = new Label();

		final int localCount = writer.localCount();

		// store everything on the stack into variables
		final int stackCount = writer.getFrame().stackCount();
		final int[] stack = new int[stackCount];
		int nextIndex = localCount;
		for (int i = 0; i < stackCount; i++)
		{
			writer.visitVarInsn(Opcodes.AUTO_STORE, nextIndex);
			stack[i] = nextIndex;
			nextIndex = writer.localCount();
		}

		// for some reason we need to store a (default) value into our result variable
		// TODO investigate why
		if (!Types.isVoid(type))
		{
			if (!type.hasDefaultValue())
			{
				writer.visitInsn(Opcodes.ACONST_NULL);
			}
			else
			{
				type.writeDefaultValue(writer);
			}
			writer.visitVarInsn(Opcodes.AUTO_STORE, nextIndex);
		}

		final Consumer<? super MethodWriter> handler;
		if (this.finallyBlock != null)
		{
			handler = writer1 -> this.finallyBlock.writeExpression(writer1, Types.VOID);
			writer.addPreReturnHandler(handler);
		}
		else
		{
			handler = null;
		}

		writer.visitTargetLabel(tryStart);
		if (this.action != null)
		{
			this.action.writeExpression(writer, type);
			if (!Types.isVoid(type))
			{
				writer.visitVarInsn(Opcodes.AUTO_STORE, nextIndex);
			}
		}

		writer.visitTargetLabel(tryEnd);
		writer.visitJumpInsn(Opcodes.GOTO, endLabel);

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final CatchBlock block = this.catchBlocks[i];
			final Label handlerLabel = new Label();
			final String handlerType = block.getType().getInternalName();

			writer.visitTargetLabel(handlerLabel);
			writer.startCatchBlock(handlerType);

			// Check if the block's variable is actually used
			if (block.variable != null)
			{
				// If yes register a new local variable for the exception and
				// store it.
				final int localCount1 = writer.localCount();
				block.variable.writeInit(writer, null);
				block.action.writeExpression(writer, type);
				writer.resetLocals(localCount1);
			}
			// Otherwise pop the exception from the stack
			else
			{
				writer.visitInsn(Opcodes.POP);
				block.action.writeExpression(writer, type);
			}

			if (!Types.isVoid(type))
			{
				writer.visitVarInsn(Opcodes.AUTO_STORE, nextIndex);
			}

			writer.visitTryCatchBlock(tryStart, tryEnd, handlerLabel, handlerType);
			writer.visitJumpInsn(Opcodes.GOTO, endLabel);
		}

		if (this.finallyBlock != null)
		{
			writer.removePreReturnHandler(handler);

			final Label finallyLabel = new Label();

			writer.visitTargetLabel(finallyLabel);
			writer.startCatchBlock("java/lang/Throwable");
			writer.visitInsn(Opcodes.POP);

			writer.visitTargetLabel(endLabel);
			this.finallyBlock.writeExpression(writer, Types.VOID);
			writer.visitFinallyBlock(tryStart, tryEnd, finallyLabel);
		}
		else
		{
			writer.visitTargetLabel(endLabel);
		}

		// retrieve whatever was on the stack
		for (int i = stackCount - 1; i >= 0; i--)
		{
			writer.visitVarInsn(Opcodes.AUTO_LOAD, stack[i]);
		}

		// load result
		if (!Types.isVoid(type))
		{
			writer.visitVarInsn(Opcodes.AUTO_LOAD, nextIndex);
		}

		writer.resetLocals(localCount);
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("try");

		if (this.action != null && !Util.formatStatementList(prefix, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("try.indent", prefix);
			if (Formatting.getBoolean("try.newline_after"))
			{
				buffer.append('\n').append(actionPrefix);
			}
			else if (Formatting.getBoolean("try.space_after"))
			{
				buffer.append(' ');
			}

			this.action.toString(actionPrefix, buffer);
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			CatchBlock block = this.catchBlocks[i];

			if (Formatting.getBoolean("try.catch.newline_before"))
			{
				buffer.append('\n').append(prefix);
			}

			buffer.append("catch");

			Formatting.appendSeparator(buffer, "try.catch.open_paren", '(');

			block.variable.toString(prefix, buffer);

			if (Formatting.getBoolean("try.catch.close_paren.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(')');

			if (Util.formatStatementList(prefix, buffer, block.action))
			{
				continue;
			}

			String actionIndent = Formatting.getIndent("try.catch.indent", prefix);
			if (Formatting.getBoolean("try.catch.close_paren.newline_after"))
			{
				buffer.append('\n').append(actionIndent);
			}
			else if (Formatting.getBoolean("try.catch.close_paren.space_after"))
			{
				buffer.append(' ');
			}

			block.action.toString(prefix, buffer);
		}
		if (this.finallyBlock != null)
		{
			if (Formatting.getBoolean("try.finally.newline_before"))
			{
				buffer.append('\n').append(prefix);
			}

			buffer.append("finally");

			if (Util.formatStatementList(prefix, buffer, this.finallyBlock))
			{
				return;
			}

			String actionIndent = Formatting.getIndent("try.finally.indent", prefix);
			if (Formatting.getBoolean("try.finally.newline_after"))
			{
				buffer.append('\n').append(actionIndent);
			}
			else
			{
				buffer.append(' ');
			}

			this.finallyBlock.toString(prefix, buffer);
		}
	}
}

package dyvil.tools.compiler.ast.statement.exception;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.position.ICodePosition;

public final class TryStatement extends AbstractValue implements IDefaultContext
{
	private static final boolean DISALLOW_EXPRESSIONS = true;

	public static final TypeChecker.MarkerSupplier CATCH_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"try.catch.type.incompatible", "type.expected", "try.catch.type");
	public static final TypeChecker.MarkerSupplier TRY_MARKER_SUPPLIER   = TypeChecker.markerSupplier(
		"try.action.type.incompatible", "type.expected", "try.action.type");

	protected IValue action;
	protected CatchBlock[] catchBlocks = new CatchBlock[1];
	protected int    catchBlockCount;
	protected IValue finallyBlock;

	// Metadata
	private IType commonType;

	public TryStatement(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return TRY;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
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

	public void setAction(IValue action)
	{
		this.action = action;
	}

	public IValue getAction()
	{
		return this.action;
	}

	public void setFinallyBlock(IValue finallyBlock)
	{
		this.finallyBlock = finallyBlock;
	}

	public IValue getFinallyBlock()
	{
		return this.finallyBlock;
	}

	@Override
	public IType getType()
	{
		if (DISALLOW_EXPRESSIONS)
		{
			return Types.VOID;
		}

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
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
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
	public int getTypeMatch(IType type)
	{
		if (DISALLOW_EXPRESSIONS)
		{
			return 0;
		}

		int total = this.action.getTypeMatch(type);
		if (total <= 0F)
		{
			return 0;
		}
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final int blockMatch = this.catchBlocks[i].action.getTypeMatch(type);
			if (blockMatch <= 0F)
			{
				return 0;
			}
			total += blockMatch;
		}

		return total / (1 + this.catchBlockCount);
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

			final IValue typedFinally = this.finallyBlock.withType(Types.VOID, Types.VOID, markers, context);
			if (typedFinally != null && typedFinally.isUsableAsStatement())
			{
				this.finallyBlock = typedFinally;
			}
			else if (this.finallyBlock.isResolved())
			{
				final Marker marker = Markers
					                      .semanticError(this.finallyBlock.getPosition(), "try.finally.type.invalid");
				marker.addInfo(Markers.getSemantic("try.finally.type", this.finallyBlock.getType().toString()));
				markers.add(marker);
			}
		}

		if (DISALLOW_EXPRESSIONS && this.commonType != null && this.commonType != Types.VOID)
		{
			markers.add(new SemanticError(this.position, "Try Statements cannot currently be used as expressions"));
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.action != null)
		{
			context = context.push(this);
			this.action = this.action.cleanup(context, compilableList);
			context = context.pop();
		}

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			this.catchBlocks[i].cleanup(context, compilableList);
		}

		if (this.finallyBlock != null)
		{
			this.finallyBlock = this.finallyBlock.cleanup(context, compilableList);
		}
		return this;
	}

	@Override
	public byte checkException(IType type)
	{
		for (int i = 0; i < this.catchBlockCount; i++)
		{
			if (this.catchBlocks[i].type.isSuperTypeOf(type))
			{
				return TRUE;
			}
		}
		return PASS;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.getType();
		}

		final boolean expression;
		final int storeInsn;
		final int localIndex;

		if (type != Types.VOID)
		{
			storeInsn = type.getStoreOpcode();
			localIndex = writer.localCount();
			expression = true;
		}
		else
		{
			storeInsn = 0;
			localIndex = -1;
			expression = false;
		}

		final dyvil.tools.asm.Label tryStart = new dyvil.tools.asm.Label();
		final dyvil.tools.asm.Label tryEnd = new dyvil.tools.asm.Label();
		final dyvil.tools.asm.Label endLabel = new dyvil.tools.asm.Label();

		writer.visitTargetLabel(tryStart);
		if (this.action != null)
		{
			this.action.writeExpression(writer, type);
			if (expression)
			{
				writer.visitVarInsn(storeInsn, localIndex);
				writer.resetLocals(localIndex);
			}

			writer.visitJumpInsn(Opcodes.GOTO, endLabel);
		}
		writer.visitLabel(tryEnd);

		for (int i = 0; i < this.catchBlockCount; i++)
		{
			final CatchBlock block = this.catchBlocks[i];
			final dyvil.tools.asm.Label handlerLabel = new dyvil.tools.asm.Label();
			final String handlerType = block.type.getInternalName();

			writer.visitTargetLabel(handlerLabel);
			writer.startCatchBlock(handlerType);

			// Check if the block's variable is actually used
			if (block.variable != null)
			{
				// If yes register a new local variable for the exception and
				// store it.
				final int localCount = writer.localCount();
				block.variable.writeInit(writer, null);
				block.action.writeExpression(writer, type);
				writer.resetLocals(localCount);
			}
			// Otherwise pop the exception from the stack
			else
			{
				writer.visitInsn(Opcodes.POP);
				block.action.writeExpression(writer, type);
			}

			if (expression)
			{
				writer.visitVarInsn(storeInsn, localIndex);
				writer.resetLocals(localIndex);
			}

			writer.visitTryCatchBlock(tryStart, tryEnd, handlerLabel, handlerType);
			writer.visitJumpInsn(Opcodes.GOTO, endLabel);
		}

		if (this.finallyBlock != null)
		{
			final dyvil.tools.asm.Label finallyLabel = new dyvil.tools.asm.Label();

			writer.visitLabel(finallyLabel);
			writer.startCatchBlock("java/lang/Throwable");
			writer.visitInsn(Opcodes.POP);

			writer.visitLabel(endLabel);
			this.finallyBlock.writeExpression(writer, Types.VOID);
			writer.visitFinallyBlock(tryStart, tryEnd, finallyLabel);
		}
		else
		{
			writer.visitLabel(endLabel);
		}

		if (expression)
		{
			writer.setLocalType(localIndex, type.getFrameType());

			writer.visitVarInsn(type.getLoadOpcode(), localIndex);
			writer.resetLocals(localIndex);
		}
	}

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

			block.type.toString(prefix, buffer);
			buffer.append(' ').append(block.varName);

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

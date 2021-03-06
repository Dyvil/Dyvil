package dyvilx.tools.compiler.ast.statement;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

public class IfStatement implements IValue
{
	protected IValue condition;
	protected IValue then;
	protected IValue elseThen;

	// Metadata
	private SourcePosition position;
	private IType          commonType;

	public IfStatement(SourcePosition position)
	{
		this.position = position;
	}

	public IfStatement(IValue condition, IValue then, IValue elseThen)
	{
		this.condition = condition;
		this.then = then;
		this.elseThen = elseThen;
	}

	public IValue getCondition()
	{
		return this.condition;
	}

	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}

	public IValue getThen()
	{
		return this.then;
	}

	public void setThen(IValue then)
	{
		this.then = then;
		this.commonType = null; // invalidate type cache
	}

	public IValue getElse()
	{
		return this.elseThen;
	}

	public void setElse(IValue elseThen)
	{
		this.elseThen = elseThen;
		this.commonType = null; // invalidate type cache
	}

	@Override
	public int valueTag()
	{
		return IF;
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
	public boolean isUsableAsStatement()
	{
		return (this.then == null || this.then.isUsableAsStatement()) //
		       && (this.elseThen == null || this.elseThen.isUsableAsStatement());
	}

	@Override
	public boolean isResolved()
	{
		return this.then != null && this.then.isResolved() && (this.elseThen == null || this.elseThen.isResolved());
	}

	@Override
	public IType getType()
	{
		if (this.commonType != null)
		{
			return this.commonType;
		}

		if (this.then != null && this.elseThen != null)
		{
			return this.commonType = Types.combine(this.then.getType(), this.elseThen.getType());
		}
		return this.commonType = Types.VOID;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if ((this.then == null || this.elseThen == null) && !Types.isVoid(type))
		{
			return null;
		}

		this.commonType = type;

		if (this.then != null)
		{
			this.then = TypeChecker.convertValue(this.then, type, typeContext, markers, this.thenContext(context),
			                                     TypeChecker.markerSupplier("if.then.type"));
		}

		if (this.elseThen != null)
		{
			this.elseThen = TypeChecker.convertValue(this.elseThen, type, typeContext, markers, context,
			                                         TypeChecker.markerSupplier("if.else.type"));
		}

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		// requires both branches to be present and conforming if the type is not void
		return Types.isVoid(type) || this.then != null && this.elseThen != null && this.then.isType(type)
		                             && this.elseThen.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.then == null || this.elseThen == null)
		{
			return IValue.MISMATCH;
		}

		return Math.min(TypeChecker.getTypeMatch(this.then, type, implicitContext),
		                TypeChecker.getTypeMatch(this.elseThen, type, implicitContext));
	}

	protected IContext thenContext(IContext context)
	{
		return context;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.resolveConditionTypes(markers, context);
		if (this.then != null)
		{
			this.then.resolveTypes(markers, this.thenContext(context));
		}
		if (this.elseThen != null)
		{
			this.elseThen.resolveTypes(markers, context);
		}
	}

	protected void resolveConditionTypes(MarkerList markers, IContext context)
	{
		if (this.condition == null)
		{
			this.condition = BooleanValue.TRUE;
			markers.add(Markers.semanticError(this.position, "if.condition.missing"));
		}

		this.condition.resolveTypes(markers, context);
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.then != null)
		{
			this.then.resolveStatement(context, markers);
		}
		if (this.elseThen != null)
		{
			this.elseThen.resolveStatement(context, markers);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveCondition(markers, context);
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, this.thenContext(context));
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.resolve(markers, context);
		}
		return this;
	}

	protected void resolveCondition(MarkerList markers, IContext context)
	{
		this.condition = this.condition.resolve(markers, context);
		this.condition = TypeChecker.convertValue(this.condition, Types.BOOLEAN, null, markers, context,
		                                          TypeChecker.markerSupplier("if.condition.type"));
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.checkConditionTypes(markers, context);
		if (this.then != null)
		{
			this.then.checkTypes(markers, this.thenContext(context));
		}
		if (this.elseThen != null)
		{
			this.elseThen.checkTypes(markers, context);
		}
	}

	protected void checkConditionTypes(MarkerList markers, IContext context)
	{
		this.condition.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.checkCondition(markers, context);
		if (this.then != null)
		{
			this.then.check(markers, this.thenContext(context));
		}
		else
		{
			markers.add(Markers.semanticError(this.position, "if.then.missing"));
		}

		if (this.elseThen != null)
		{
			this.elseThen.check(markers, context);
		}
	}

	protected void checkCondition(MarkerList markers, IContext context)
	{
		this.condition.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.condition = this.condition.foldConstants();
		if (this.then != null)
		{
			this.then = this.then.foldConstants();
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.foldConstants();
		}
		return this.foldWithCondition();
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.condition = this.condition.cleanup(compilableList, classCompilableList);
		if (this.then != null)
		{
			this.then = this.then.cleanup(compilableList, classCompilableList);
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.cleanup(compilableList, classCompilableList);
		}
		return this.foldWithCondition();
	}

	protected IValue foldWithCondition()
	{
		if (this.condition.valueTag() == BOOLEAN)
		{
			if (this.condition.booleanValue())
			{
				// Condition is true -> Return the action
				return this.then;
			}
			else if (this.elseThen != null)
			{
				// Condition is false, else clause exists -> Return else
				// clause
				return this.elseThen;
			}
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.getType();
		}

		if (Types.isVoid(type))
		{
			this.writeStatement(writer);
			return;
		}

		final dyvilx.tools.asm.Label elseStart = new dyvilx.tools.asm.Label();
		final dyvilx.tools.asm.Label elseEnd = new dyvilx.tools.asm.Label();
		final Object commonFrameType = type.getFrameType();
		final int varCount = writer.localCount();

		// Condition
		this.writeCondition(writer, elseStart);
		// If Block
		this.then.writeExpression(writer, type);

		if (!writer.hasReturn())
		{
			writer.getFrame().set(commonFrameType);
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
		}

		writer.visitTargetLabel(elseStart);

		// Else Block
		if (this.elseThen == null)
		{
			type.writeDefaultValue(writer);
		}
		else
		{
			this.elseThen.writeExpression(writer, type);
		}

		if (!writer.hasReturn())
		{
			writer.getFrame().set(commonFrameType);
		}

		writer.visitTargetLabel(elseEnd);
		writer.resetLocals(varCount);
	}

	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		final int varCount = writer.localCount();
		final dyvilx.tools.asm.Label elseStart = new dyvilx.tools.asm.Label();

		if (this.elseThen != null)
		{
			final dyvilx.tools.asm.Label elseEnd = new dyvilx.tools.asm.Label();

			// Condition
			this.writeCondition(writer, elseStart);
			// If Block
			this.then.writeExpression(writer, Types.VOID);

			if (!writer.hasReturn())
			{
				writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			}

			writer.visitTargetLabel(elseStart);
			// Else Block
			this.elseThen.writeExpression(writer, Types.VOID);
			writer.visitTargetLabel(elseEnd);
		}
		else
		{
			// Condition
			this.writeCondition(writer, elseStart);
			// If Block
			this.then.writeExpression(writer, Types.VOID);
			writer.visitTargetLabel(elseStart);
		}

		writer.resetLocals(varCount);
	}

	protected void writeCondition(MethodWriter writer, Label elseStart)
	{
		this.condition.writeInvJump(writer, elseStart);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder buffer)
	{
		buffer.append("if");

		Formatting.appendSeparator(buffer, "if.open_paren", '(');
		this.conditionToString(indent, buffer);
		Formatting.appendClose(buffer, "if.close_paren", ')');

		if (this.then != null && !Util.formatStatementList(indent, buffer, this.then))
		{
			final String thenIndent = Formatting.getIndent("if.indent", indent);
			if (Formatting.getBoolean("if.close_paren.newline_after"))
			{
				buffer.append('\n').append(thenIndent);
			}
			else if (Formatting.getBoolean("if.close_paren.space_after"))
			{
				buffer.append(' ');
			}

			this.then.toString(thenIndent, buffer);
		}

		if (this.elseThen != null)
		{
			if (Formatting.getBoolean("if.else.newline_before"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("if.else.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append("else");

			if (Util.formatStatementList(indent, buffer, this.elseThen))
			{
				return;
			}

			if (Formatting.getBoolean("if.else.newline_after"))
			{
				buffer.append('\n').append(indent);
			}
			else if (Formatting.getBoolean("if.else.space_after"))
			{
				buffer.append(' ');
			}

			this.elseThen.toString(indent, buffer);
		}
	}

	protected void conditionToString(String indent, StringBuilder buffer)
	{
		this.condition.toString(indent, buffer);
	}
}

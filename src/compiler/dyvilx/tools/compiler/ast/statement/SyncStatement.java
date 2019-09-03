package dyvilx.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.AbstractValue;
import dyvilx.tools.compiler.ast.expression.DummyValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.VoidValue;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.statement.exception.TryStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

public class SyncStatement extends AbstractValue implements IStatement
{
	// =============== Fields ===============

	protected IValue lock;
	protected IValue action;

	// =============== Constructors ===============

	public SyncStatement(SourcePosition position)
	{
		this.position = position;
	}

	public SyncStatement(SourcePosition position, IValue lock, IValue block)
	{
		this.position = position;
		this.lock = lock;
		this.action = block;
	}

	// =============== Properties ===============

	public IValue getLock()
	{
		return this.lock;
	}

	public void setLock(IValue lock)
	{
		this.lock = lock;
	}

	public IValue getAction()
	{
		return this.action;
	}

	public void setAction(IValue action)
	{
		this.action = action;
	}

	@Override
	public IType getType()
	{
		return this.action.getType();
	}

	// =============== Methods ===============

	@Override
	public int valueTag()
	{
		return SYNCHRONIZED;
	}

	// --------------- Typing ---------------

	@Override
	public boolean isType(IType type)
	{
		return this.action.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.action.getTypeMatch(type, implicitContext);
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.action = this.action.withType(type, typeContext, markers, context);
		return this;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.lock == null)
		{
			this.lock = DummyValue.INSTANCE;
		}
		if (this.action == null)
		{
			this.action = new VoidValue();
		}

		this.lock.resolveTypes(markers, context);
		this.action.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.lock = this.lock.resolve(markers, context);
		this.action = this.action.resolve(markers, context);

		/*
		synchonized (obj) {
			statements...
		}
		->
		Object lock = obj
		_monitorEnter(lock)
		try {
			statements...
		}
		finally {
			_monitorExit(lock)
		}
		 */

		final StatementList list = new StatementList(this.position);
		final Variable var = new Variable(null, Types.OBJECT, this.lock);
		list.add(new VariableStatement(var));
		list.add(new DummyValue(() -> Types.VOID, (writer, type) -> {
			var.writeGet(writer);
			writer.visitInsn(Opcodes.MONITORENTER);
		}));

		final TryStatement tryStatement = new TryStatement(this.position);
		tryStatement.setAction(this.action);
		tryStatement.setFinallyBlock(new DummyValue(() -> Types.VOID, (writer, type) -> {
			var.writeGet(writer);
			writer.visitInsn(Opcodes.MONITOREXIT);
		}));
		list.add(tryStatement);

		return list;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IValue foldConstants()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		throw new UnsupportedOperationException();
	}

	// --------------- Compilation ---------------

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		throw new UnsupportedOperationException();
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("synchronized");

		Formatting.appendSeparator(buffer, "sync.open_paren", '(');
		if (this.lock != null)
		{
			this.lock.toString(prefix, buffer);
		}

		if (Formatting.getBoolean("sync.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		if (this.action == null)
		{
			return;
		}

		if (Util.formatStatementList(prefix, buffer, this.action))
		{
			return;
		}

		String valuePrefix = Formatting.getIndent("sync.indent", prefix);

		if (Formatting.getBoolean("sync.close_paren.newline_after"))
		{
			buffer.append('\n').append(valuePrefix);
		}
		else if (Formatting.getBoolean("sync.close_paren.space_after"))
		{
			buffer.append(' ');
		}

		this.action.toString(prefix, buffer);
	}
}

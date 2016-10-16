package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.ast.consumer.IParameterConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public interface IParameterList extends IParameterConsumer, IResolvable
{
	IParameterList EMPTY = new ParameterList(0);

	int size();

	default boolean isEmpty()
	{
		return this.size() <= 0;
	}

	IParameter get(int index);

	IParameter[] getParameterArray();

	@Override
	void addParameter(IParameter parameter);

	void set(int index, IParameter parameter);

	void setParameterArray(IParameter[] parameters, int parameterCount);

	default void copyTo(IParameterList other)
	{
		other.setParameterArray(this.getParameterArray(), this.size());
	}

	// Resolution

	IParameter resolveParameter(Name name);

	boolean isParameter(IVariable variable);

	boolean matches(IParameterList other);

	// Compiler Phases

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	void check(MarkerList markers, IContext context);

	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	// Compilation

	void appendDescriptor(StringBuilder builder);

	boolean needsSignature();

	void appendSignature(StringBuilder builder);

	void writeLocals(MethodWriter writer, Label start, Label end);

	void writeInit(MethodWriter writer);

	// Formatting

	void toString(String prefix, StringBuilder buffer);

	void signatureToString(StringBuilder buffer, ITypeContext typeContext);
}

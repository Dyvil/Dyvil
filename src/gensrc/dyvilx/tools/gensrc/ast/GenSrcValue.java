package dyvilx.tools.gensrc.ast;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;

public interface GenSrcValue extends IValue
{
	int PROCESSED_TEXT = 512;
	int SCOPE_DIRECTIVE = 513;
	int CALL_DIRECTIVE = 514;
	int WRITE_CALL = 515;

	@Override
	default boolean isResolved()
	{
		return true;
	}

	@Override
	default IType getType()
	{
		return Types.VOID;
	}
}

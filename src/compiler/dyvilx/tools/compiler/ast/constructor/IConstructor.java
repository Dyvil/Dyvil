package dyvilx.tools.compiler.ast.constructor;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.access.InitializerCall;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public interface IConstructor extends ICallableMember, IContext
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.CONSTRUCTOR;
	}

	InitializerCall getInitializer();

	void setInitializer(InitializerCall initializer);

	void checkMatch(MatchList<IConstructor> list, ArgumentList arguments);
	
	IType checkArguments(MarkerList markers, SourcePosition position, IContext context, IType type, ArgumentList arguments);
	
	void checkCall(MarkerList markers, SourcePosition position, IContext context, ArgumentList arguments);

	// Compilation
	
	void writeCall(MethodWriter writer, ArgumentList arguments, IType type, int lineNumber) throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;
	
	void writeArguments(MethodWriter writer, ArgumentList arguments) throws BytecodeException;
}

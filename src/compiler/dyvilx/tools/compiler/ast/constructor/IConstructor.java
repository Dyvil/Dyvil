package dyvilx.tools.compiler.ast.constructor;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.access.InitializerCall;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IConstructor extends ICallableMember, IContext
{
	// --------------- Getters and Setters ---------------

	// - - - - - - - - Initializer - - - - - - - -

	InitializerCall getInitializer();

	void setInitializer(InitializerCall initializer);

	// --------------- Attributes ---------------

	@Override
	default MemberKind getKind()
	{
		return MemberKind.CONSTRUCTOR;
	}

	// --------------- Context ---------------

	@Override
	default boolean isConstructor()
	{
		return true;
	}

	// --------------- Constructor Matching ---------------

	void checkMatch(MatchList<IConstructor> list, ArgumentList arguments);

	// --------------- Call Checking ---------------

	IType checkArguments(MarkerList markers, SourcePosition position, IContext context, IType type, ArgumentList arguments);

	void checkCall(MarkerList markers, SourcePosition position, IContext context, ArgumentList arguments);

	// --------------- Call Compilation ---------------

	void writeCall(MethodWriter writer, ArgumentList arguments, IType type, int lineNumber) throws BytecodeException;

	void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;

	void writeArguments(MethodWriter writer, ArgumentList arguments) throws BytecodeException;
}

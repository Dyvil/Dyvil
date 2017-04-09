package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	
	IType checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, ArgumentList arguments);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, ArgumentList arguments);

	// Compilation
	
	void writeCall(MethodWriter writer, ArgumentList arguments, IType type, int lineNumber) throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;
	
	void writeArguments(MethodWriter writer, ArgumentList arguments) throws BytecodeException;
}

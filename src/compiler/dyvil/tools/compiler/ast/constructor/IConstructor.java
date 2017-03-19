package dyvil.tools.compiler.ast.constructor;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
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

	void checkMatch(MatchList<IConstructor> list, IArguments arguments);
	
	IType checkArguments(MarkerList markers, ICodePosition position, IContext context, IType type, IArguments arguments);
	
	void checkCall(MarkerList markers, ICodePosition position, IContext context, IArguments arguments);

	// Compilation
	
	String getDescriptor();
	
	String getSignature();
	
	String[] getExceptions();
	
	void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber) throws BytecodeException;
	
	void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;
	
	void writeArguments(MethodWriter writer, IArguments arguments) throws BytecodeException;
}

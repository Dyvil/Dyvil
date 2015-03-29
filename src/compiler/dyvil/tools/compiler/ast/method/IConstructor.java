package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IConstructor extends IASTNode, IMember, IBaseMethod, ITypeList, IContext, IClassCompilable
{
	public int getSignatureMatch(IArguments arguments);
	
	public void checkArguments(MarkerList markers, IArguments arguments);
	
	// Misc
	
	public void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	public default int typeCount()
	{
		return 0;
	}
	
	@Override
	public default void setType(int index, IType type)
	{
	}
	
	@Override
	public default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	@Override
	public default IType getType(int index)
	{
		return null;
	}
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void writeCall(MethodWriter writer, IArguments arguments, IType type);
	
	public void writeInvoke(MethodWriter writer, IArguments arguments);
}

package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ApplyMethodCall extends AbstractCall
{
	public ApplyMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return APPLY_METHOD_CALL;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		this.arguments.resolve(markers, context);
		
		IMethod method = ICall.resolveMethod(context, this.instance, Name.apply, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		Marker marker = markers.create(this.position, "resolve.method", "apply");
		marker.addInfo("Callee Type: " + this.instance.getType());
		if (!this.arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			this.arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}
		
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}

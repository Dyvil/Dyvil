package dyvil.tools.compiler.ast.method;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.parameter.IParameter;

public interface IExternalMethod extends ICallableMember
{
	public default IParameter getParameter_(int index)
	{
		return this.getParameter(index);
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef,TypePath typePath,String desc,boolean visible);
}

package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SimpleImport extends ASTObject implements IImport
{
	public IClass	theClass;
	
	private String	qualifiedImport;
	private String	packageName;
	private String	className;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, String qualifiedImport, String className)
	{
		this.position = position;
		this.setImport(qualifiedImport, className);
	}
	
	public void setImport(String qualifiedImport, String className)
	{
		this.qualifiedImport = qualifiedImport;
		this.className = className;
		
		int index = qualifiedImport.lastIndexOf('.');
		if (index != -1)
		{
			this.packageName = qualifiedImport.substring(0, index);
		}
	}
	
	public String getImport()
	{
		return this.qualifiedImport;
	}
	
	@Override
	public SimpleImport applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE)
		{
			Package pack = state.rootPackage.resolvePackage(this.packageName);
			if (pack == null)
			{
				state.addMarker(new SyntaxError(this.position, "Package could not be resolved", "Remove this import"));
				return this;
			}
			
			IClass iclass = pack.resolveClass(this.className);
			if (iclass == null)
			{
				state.addMarker(new SyntaxError(this.position, "Class could not be resolved", "Remove this import"));
			}
			
			this.theClass = iclass;
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.qualifiedImport).append(";");
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.className.equals(name))
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public IField resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		throw new UnsupportedOperationException();
	}
}

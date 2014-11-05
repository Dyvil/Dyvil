package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SemanticError;
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
		if (state == CompilerState.RESOLVE_TYPES)
		{
			Package pack = Package.rootPackage.resolvePackage(this.packageName);
			if (pack == null)
			{
				state.addMarker(new SemanticError(this.position, "'" + this.packageName + "' could not be resolved to a package", "Remove this import"));
				return this;
			}
			
			IClass iclass = pack.resolveClass(this.className);
			if (iclass == null)
			{
				state.addMarker(new SemanticError(this.position, "'" + this.className + "' could not be resolved to a class", "Remove this import"));
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
	public FieldMatch resolveField(String name, Type type)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(String name, Type returnType, Type... argumentTypes)
	{
		throw new UnsupportedOperationException();
	}
}

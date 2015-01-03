package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SimpleImport extends ASTNode implements IImport
{
	public IClass	theClass;
	
	private String	qualifiedImport;
	private String	packageName;
	private String	className;
	private String	alias;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, String qualifiedImport)
	{
		this.position = position;
		this.setImport(qualifiedImport);
	}
	
	public void setImport(String qualifiedImport)
	{
		this.qualifiedImport = qualifiedImport;
		
		int index = qualifiedImport.lastIndexOf('.');
		if (index != -1)
		{
			this.packageName = qualifiedImport.substring(0, index);
			this.className = qualifiedImport.substring(index + 1);
		}
	}
	
	public String getImport()
	{
		return this.qualifiedImport;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
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
		if (this.className.equals(name) || name.equals(this.alias))
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, IType... argumentTypes)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("import ").append(this.qualifiedImport);
	}
}

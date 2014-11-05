package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageImport extends ASTObject implements IImport
{
	public Package		pack;
	
	protected String	packageName;
	
	public PackageImport(ICodePosition position, String packageName)
	{
		this.position = position;
		this.packageName = packageName;
	}
	
	public void setPackage(String thePackage)
	{
		this.packageName = thePackage;
	}
	
	public String getPackage()
	{
		return this.packageName;
	}
	
	@Override
	public PackageImport applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			Package pack = Package.rootPackage.resolvePackage(this.packageName);
			if (pack == null)
			{
				state.addMarker(new SemanticError(this.position, "'" + this.packageName + "' could not be resolved to a package"));
			}
			
			this.pack = pack;
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.packageName).append(Formatting.Import.packageImportEnd).append(';');
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
		if (this.pack == null)
		{
			return null;
		}
		return this.pack.resolveClass(name);
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

package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ImportDeclaration implements IASTNode, IObjectCompilable, IImportContext
{
	protected ICodePosition position;
	protected IImport       theImport;
	protected boolean       isStatic;
	
	public ImportDeclaration(ICodePosition position)
	{
		this.position = position;
	}
	
	public ImportDeclaration(ICodePosition position, boolean isStatic)
	{
		this.position = position;
		this.isStatic = isStatic;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setImport(IImport iimport)
	{
		this.theImport = iimport;
	}
	
	public IImport getImport()
	{
		return this.theImport;
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.theImport == null)
		{
			markers.add(Markers.semantic(this.position, this.isStatic ? "using.invalid" : "import.invalid"));
			return;
		}
		
		this.theImport.resolveTypes(markers, Package.rootPackage, this.isStatic);
	}

	// Context
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.theImport.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.theImport.resolveClass(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.theImport.resolveField(name);
	}

	@Override
	public void getImplicitMatches(MethodMatchList list, IValue value, IType targetType)
	{
		this.theImport.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
		this.theImport.getMethodMatches(list, receiver, name, arguments);
	}

	// Compilation
	
	@Override
	public void write(DataOutput output) throws IOException
	{
		IImport.writeImport(this.theImport, output);
	}
	
	@Override
	public void read(DataInput input) throws IOException
	{
		this.theImport = IImport.readImport(input);
	}

	// Formatting
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isStatic)
		{
			buffer.append("using ");
		}
		else
		{
			buffer.append("import ");
		}
		
		if (this.theImport != null)
		{
			this.theImport.toString(prefix, buffer);
		}
	}
}

package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ImportDeclaration implements IASTNode, IObjectCompilable
{
	public static final int IMPORT = 0;
	public static final int USING  = 1;
	
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
	
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic)
	{
		if (this.theImport == null)
		{
			markers.add(I18n.createMarker(this.position, isStatic ? "using.invalid" : "import.invalid"));
			return;
		}
		
		this.theImport.resolveTypes(markers, Package.rootPackage, this.isStatic);
	}
	
	public Package resolvePackage(Name name)
	{
		return this.theImport.resolvePackage(name);
	}
	
	public IClass resolveClass(Name name)
	{
		return this.theImport.resolveClass(name);
	}
	
	public IDataMember resolveField(Name name)
	{
		return this.theImport.resolveField(name);
	}
	
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.theImport.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void write(DataOutput dos) throws IOException
	{
		dos.writeByte(this.theImport.importTag());
		this.theImport.write(dos);
	}
	
	@Override
	public void read(DataInput dis) throws IOException
	{
		this.theImport = IImport.fromTag(dis.readByte());
		this.theImport.read(dis);
	}
	
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

package dyvil.tools.compiler.ast.imports;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ImportDeclaration extends ASTNode
{
	public static final int	IMPORT	= 0;
	public static final int	USING	= 1;
	
	public IImport			theImport;
	public boolean			isStatic;
	
	public ImportDeclaration(ICodePosition position)
	{
		this.position = position;
	}
	
	public ImportDeclaration(ICodePosition position, boolean isStatic)
	{
		this.position = position;
		this.isStatic = isStatic;
	}
	
	public void setImport(IImport iimport)
	{
		this.theImport = iimport;
	}
	
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic)
	{
		if (this.theImport == null)
		{
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
	
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theImport.getMethodMatches(list, instance, name, arguments);
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
		this.theImport.toString(prefix, buffer);
	}
	
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeByte(this.theImport.importTag());
		this.theImport.write(dos);
	}
	
	public void read(DataInputStream dis) throws IOException
	{
		this.theImport = IImport.fromTag(dis.readByte());
		this.theImport.read(dis);
	}
}

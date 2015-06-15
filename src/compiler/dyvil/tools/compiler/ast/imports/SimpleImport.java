package dyvil.tools.compiler.ast.imports;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class SimpleImport extends ASTNode implements IImport
{
	public Name				name;
	public Name				alias;
	public IImport			child;
	
	private IClass			theClass;
	private Package			thePackage;
	
	private IField			field;
	private List<IMethod>	methods;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}
	
	@Override
	public int importTag()
	{
		return SIMPLE;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.child = iimport;
	}
	
	@Override
	public IImport getChild()
	{
		return this.child;
	}
	
	@Override
	public void setAlias(Name alias)
	{
		this.alias = alias;
	}
	
	public Name getAlias()
	{
		return this.alias;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean using)
	{
		if (using && this.child == null)
		{
			if (!(context instanceof IClass))
			{
				markers.add(this.position, "import.using.invalid");
				return;
			}
			
			IClassBody body = ((IClass) context).getBody();
			
			IField field = body.getField(this.name);
			if (field != null)
			{
				this.field = field;
				return;
			}
			
			this.methods = new ArrayList();
			int len = body.methodCount();
			for (int i = 0; i < len; i++)
			{
				IMethod m = body.getMethod(i);
				if (m.getName() == this.name)
				{
					this.methods.add(m);
				}
			}
			if (!this.methods.isEmpty())
			{
				return;
			}
			
			markers.add(this.position, "resolve.method_field", this.name.qualified);
			return;
		}
		
		Package pack = context.resolvePackage(this.name);
		if (pack != null)
		{
			this.thePackage = pack;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, pack, using);
			}
			return;
		}
		
		IClass iclass = context.resolveClass(this.name);
		if (iclass != null)
		{
			this.theClass = iclass;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, iclass, using);
			}
			return;
		}
		
		markers.add(this.position, "resolve.package", this.name.qualified);
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		if (this.child != null)
		{
			return this.child.resolvePackage(name);
		}
		if (name == this.name || name == this.alias)
		{
			return this.thePackage;
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		if (this.child != null)
		{
			return this.child.resolveClass(name);
		}
		if (name == this.name || name == this.alias)
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (this.child != null)
		{
			return this.child.resolveField(name);
		}
		if (name == this.name || name == this.alias)
		{
			return this.field;
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.child != null)
		{
			this.child.getMethodMatches(list, instance, name, arguments);
		}
		if (name != this.name && name != this.alias)
		{
			return;
		}
		
		if (this.methods == null)
		{
			return;
		}
		for (IMethod m : this.methods)
		{
			int match = m.getSignatureMatch(name, instance, arguments);
			if (match > 0)
			{
				list.add(new MethodMatch(m, match));
			}
		}
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(this.name.qualified);
		if (this.alias != null)
		{
			dos.writeUTF(this.alias.qualified);
		}
		else
		{
			dos.writeUTF("");
		}
		
		if (this.child != null)
		{
			dos.writeByte(this.child.importTag());
			this.child.write(dos);
		}
		else
		{
			dos.writeByte(0);
		}
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		this.name = Name.getQualified(dis.readUTF());
		
		String alias = dis.readUTF();
		if (!alias.isEmpty())
		{
			this.alias = Name.getQualified(alias);
		}
		
		byte childTag = dis.readByte();
		if (childTag != 0)
		{
			this.child = IImport.fromTag(childTag);
			this.child.read(dis);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (this.alias != null)
		{
			buffer.append(Formatting.Import.aliasSeperator);
			buffer.append(this.alias);
		}
		if (this.child != null)
		{
			buffer.append('.');
			this.child.toString(prefix, buffer);
		}
	}
}

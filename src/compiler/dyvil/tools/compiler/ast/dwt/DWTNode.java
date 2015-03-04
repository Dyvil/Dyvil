package dyvil.tools.compiler.ast.dwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.strings.StringUtils;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class DWTNode extends ASTNode implements IValue, INamed, IValueMap
{
	public static final int		NODE		= 256;
	public static final int		LIST		= 257;
	public static final int		REFERENCE	= 258;
	
	public DWTNode				parent;
	
	public String				name;
	public String				fullName;
	public IType				type;
	public List<DWTProperty>	properties	= new ArrayList();
	
	protected IClass			theClass;
	protected IMethod			getter;
	
	private int					varIndex;
	
	public DWTNode()
	{
	}
	
	public DWTNode(ICodePosition position)
	{
		this.position = position;
	}
	
	public DWTNode(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.fullName = name;
	}
	
	public void setParent(DWTNode parent)
	{
		this.fullName = parent.fullName + "$" + this.name;
		this.parent = parent;
	}
	
	@Override
	public int getValueType()
	{
		return NODE;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.fullName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.fullName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.fullName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.fullName.equals(name);
	}
	
	@Override
	public void addValue(String key, IValue value)
	{
		if (value.getValueType() == NODE)
		{
			((DWTNode) value).setParent(this);
		}
		this.properties.add(new DWTProperty(this, key, value));
	}
	
	@Override
	public IValue getValue(String key)
	{
		return null;
	}
	
	public void addFields(Map<String, IType> fields)
	{
		fields.put(this.fullName, this.type);
		
		for (DWTProperty property : this.properties)
		{
			fields.put(property.fullName, property.value.getType());
		}
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		String s = "J" + StringUtils.toTitleCase(this.name);
		this.theClass = DWTFile.javaxSwing.resolveClass(s);
		
		if (this.theClass == null)
		{
			markers.add(Markers.create(this.position, "dwt.component.type", this.name, s));
			return;
		}
		
		this.type = this.theClass.getType();
		
		for (DWTProperty property : this.properties)
		{
			property.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public DWTNode resolve(List<Marker> markers, IContext context)
	{
		for (DWTProperty property : this.properties)
		{
			String key = property.key;
			IValue value = property.value;
			int type = value.getValueType();
			if (type == LIST)
			{
				for (IValue v : (IValueList) value)
				{
					String s1 = Util.getAdder(key);
					MethodMatch m = this.theClass.resolveMethod(this, s1, new SingleArgument(value));
					
					if (m != null)
					{
						value.resolve(markers, m.theMethod);
						continue;
					}
					markers.add(Markers.create(v.getPosition(), "dwt.property.unknown", key, this.type.toString()));
				}
			}
			else if (type == NODE)
			{
				DWTNode node = (DWTNode) value;
				IClass iclass = node.theClass;
				node.resolve(markers, context);
				
				if (iclass == null)
				{
					continue;
				}
				
				MethodMatch getter = this.theClass.resolveMethod(this, Util.getGetter(key), EmptyArguments.INSTANCE);
				if (getter != null)
				{
					node.getter = getter.theMethod;
					continue;
				}
				IMethod constructor = iclass.getBody().getMethod("<init>");
				if (constructor == null)
				{
					markers.add(Markers.create(value.getPosition(), "dwt.component.constructor"));
				}
			}
			else
			{
				String s1 = Util.getSetter(key);
				MethodMatch m = this.theClass.resolveMethod(this, s1, new SingleArgument(value));
				
				if (m != null)
				{
					property.setter = m.theMethod;
					continue;
				}
				markers.add(Markers.create(value.getPosition(), "dwt.property.unknown", key, this.type.toString()));
			}
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public DWTNode foldConstants()
	{
		return null;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		writer.visitVarInsn(Opcodes.ALOAD, this.varIndex, this.type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	public void write(String owner, MethodWriter writer)
	{
		String internal = this.type.getInternalName();
		String extended = "L" + internal + ";";
		Label start = new Label();
		Label end = new Label();
		
		int index = this.varIndex = writer.addLocal(extended);
		writer.visitLabel(start, false);
		if (this.getter != null)
		{
			// Getter
			this.getter.writeCall(writer, this.parent, EmptyArguments.INSTANCE);
		}
		else
		{
			// Constructor
			writer.visitTypeInsn(Opcodes.NEW, internal);
			writer.visitInsn(Opcodes.DUP);
			writer.visitInsn(Opcodes.DUP);
			writer.visitMethodInsn(Opcodes.INVOKESPECIAL, internal, "<init>", "()V", 0, Type.VOID);
			writer.visitPutStatic(owner, this.fullName, extended);
		}
		
		writer.visitVarInsn(Opcodes.ASTORE, index);
		
		for (DWTProperty property : this.properties)
		{
			IMethod setter = property.setter;
			IValue value = property.value;
			if (setter != null)
			{
				String key = property.key;
				
				writer.visitVarInsn(Opcodes.ALOAD, index, this.type);
				value.writeExpression(writer);
				writer.visitInsn(Opcodes.DUP);
				writer.visitPutStatic(owner, property.fullName, value.getType().getExtendedName());
				setter.writeCall(writer, null, EmptyArguments.INSTANCE);
			}
			else if (value.getValueType() == NODE)
			{
				((DWTNode) value).write(owner, writer);
			}
		}
		
		writer.visitLabel(end, false);
		writer.visitLocalVariable(this.name, extended, null, start, end, index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append('\n');
		buffer.append(prefix).append('{');
		String prefix1 = prefix + '\t';
		for (DWTProperty property : this.properties)
		{
			buffer.append('\n').append(prefix1).append(property.key).append(Formatting.Field.keyValueSeperator);
			IValue value = property.value;
			if (value.isStatement())
			{
				buffer.append('\n').append(prefix1);
				value.toString(prefix1, buffer);
			}
			else
			{
				buffer.append(' ');
				value.toString(prefix1, buffer);
			}
		}
		buffer.append('\n').append(prefix).append('}');
	}
}

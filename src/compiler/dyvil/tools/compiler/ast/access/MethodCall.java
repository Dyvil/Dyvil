package dyvil.tools.compiler.ast.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.AccessResolver;
import dyvil.tools.compiler.transform.ConstantFolder;
import dyvil.tools.compiler.transform.Operators;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class MethodCall extends ASTNode implements IAccess, INamed
{
	public String				name;
	public String				qualifiedName;
	
	public List<IType>			generics;
	public Map<String, IType>	typeArguments;
	
	public IValue				instance;
	public List<IValue>			arguments;
	
	public boolean				dotless;
	public boolean				isSugarCall;
	
	public IMethod				method;
	public IType				type;
	
	public MethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public MethodCall(ICodePosition position, IValue instance, String name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
		this.arguments = new ArrayList(3);
	}
	
	@Override
	public int getValueType()
	{
		return METHOD_CALL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.method.isIntrinsic() || this.getType().isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return Type.NONE;
		}
		if (this.type == null)
		{
			if (this.method.hasTypeVariables())
			{
				if (this.typeArguments == null)
				{
					this.typeArguments = this.method.getTypeMap(this.instance, this.arguments, this.generics);
				}
				return this.type = this.method.getType(this.typeArguments);
			}
			return this.type = this.method.getType();
		}
		return this.type;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Type.NONE || type == Type.VOID)
		{
			return true;
		}
		if (this.method == null)
		{
			return false;
		}
		return Type.isSuperType(type, this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.method == null)
		{
			return 0;
		}
		
		IType type1 = this.method.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		else if (Type.isSuperType(type, type1))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
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
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	public void setSugar(boolean sugar)
	{
		this.isSugarCall = sugar;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return AccessResolver.resolve(markers, context, this);
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.method != null)
		{
			if (this.typeArguments == null && this.method.hasTypeVariables())
			{
				this.typeArguments = this.method.getTypeMap(this.instance, this.arguments, this.generics);
			}
			
			this.method.checkArguments(markers, this.instance, this.arguments, this.typeArguments);
			
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.method.deprecated", this.name));
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(Markers.create(this.position, "access.method.instance", this.name));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.method.sealed", this.name));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.method.invisible", this.name));
			}
		}

		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.arguments.size();
		if (len == 1)
		{
			IValue argument = this.arguments.get(0);
			if (argument.isConstant())
			{
				if (this.instance != null)
				{
					if (this.instance.isConstant())
					{
						IValue v1 = ConstantFolder.apply(this.instance, this.qualifiedName, argument);
						return v1 == null ? this : v1;
					}
					
					this.instance = this.instance.foldConstants();
					return this;
				}
				
				IValue v1 = ConstantFolder.apply(this.qualifiedName, argument);
				if (v1 != null)
				{
					return v1;
				}
			}
			
			if (this.instance != null)
			{
				this.instance = this.instance.foldConstants();
			}
			this.arguments.set(0, argument.foldConstants());
			return this;
		}
		
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		
		return this;
	}
	
	private IValue	replacement;
	
	@Override
	public boolean isResolved()
	{
		return this.method != null;
	}
	
	@Override
	public boolean resolve(IContext context, List<Marker> markers)
	{
		int len = this.arguments.size();
		if (len == 0 && this.instance != null)
		{
			IValue operator = Operators.get(this.instance, this.name);
			if (operator != null)
			{
				operator.setPosition(this.position);
				this.replacement = operator;
				// Return false to apply replacement in resolve2
				return false;
			}
		}
		else if (len == 1)
		{
			IValue argument = this.arguments.get(0);
			argument = argument.resolve(markers, context);
			
			IValue operator = this.instance == null ? Operators.get(this.name, argument) : Operators.get(this.instance, this.name, argument);
			if (operator != null)
			{
				operator.setPosition(this.position);
				this.replacement = operator;
				// Return false to apply replacement in resolve2
				return false;
			}
			
			this.arguments.set(0, argument);
		}
		else
		{
			for (int i = 0; i < len; i++)
			{
				IValue v1 = this.arguments.get(i);
				IValue v2 = v1.resolve(markers, context);
				if (v1 != v2)
				{
					this.arguments.set(i, v2);
				}
			}
		}
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, this.arguments);
		if (method != null)
		{
			this.method = method;
			return true;
		}
		
		if (len == 1 && this.instance != null && this.qualifiedName.endsWith("$eq"))
		{
			String s = this.qualifiedName.substring(0, this.qualifiedName.length() - 3);
			MethodMatch method1 = this.instance.getType().resolveMethod(null, s, this.arguments);
			if (method1 != null)
			{
				AssignMethodCall call = new AssignMethodCall(this.position);
				call.method = method1.theMethod;
				call.instance = this.instance;
				call.arguments = this.arguments;
				call.name = this.name.substring(0, this.name.length() - 1);
				call.qualifiedName = s;
				call.dotless = this.dotless;
				call.isSugarCall = this.isSugarCall;
				this.replacement = call;
			}
		}
		return false;
	}
	
	@Override
	public IValue resolve2(IContext context)
	{
		if (this.replacement != null)
		{
			return this.replacement;
		}
		
		if (this.isSugarCall)
		{
			if (this.arguments.isEmpty())
			{
				IField field = IAccess.resolveField(context, this.instance, this.qualifiedName);
				if (field != null)
				{
					FieldAccess access = new FieldAccess(this.position);
					access.field = field;
					access.instance = this.instance;
					access.name = this.name;
					access.qualifiedName = this.qualifiedName;
					access.dotless = this.dotless;
					return access;
				}
			}
		}
		// Resolve Apply Method
		else if (this.instance == null)
		{
			IValue instance;
			IType type = null;
			IMethod method = null;
			
			FieldMatch field = context.resolveField(this.qualifiedName);
			if (field == null)
			{
				// Find a type
				type = new Type(this.position, this.qualifiedName).resolve(null, context);
				if (!type.isResolved())
				{
					// No type found -> Not an apply method call
					return null;
				}
				else
				{
					// Find the apply method of the type
					MethodMatch match = type.resolveMethod(null, "apply", this.arguments);
					if (match == null)
					{
						// No apply method found -> Not an apply method call
						return null;
					}
					method = match.theMethod;
					instance = new ClassAccess(this.position, type);
				}
			}
			else
			{
				FieldAccess access = new FieldAccess(this.position);
				access.field = field.theField;
				access.name = this.name;
				access.qualifiedName = this.qualifiedName;
				access.dotless = this.dotless;
				
				// Find the apply method of the field type
				MethodMatch match = field.theField.getType().resolveMethod(access, "apply", this.arguments);
				if (match == null)
				{
					// No apply method found -> Not an apply method call
					return null;
				}
				method = match.theMethod;
				instance = access;
			}
			
			ApplyMethodCall call = new ApplyMethodCall(this.position);
			call.method = method;
			call.instance = instance;
			call.arguments = this.arguments;
			
			return call;
		}
		
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		List<IValue> list = new ArrayList(this.arguments);
		list.add(next);
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, list);
		if (method != null)
		{
			this.arguments = list;
			this.method = method;
			return this;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		Marker marker;
		if (this.isSugarCall && this.arguments.isEmpty())
		{
			marker = Markers.create(this.position, "resolve.method_field", this.name);
		}
		else
		{
			marker = Markers.create(this.position, "resolve.method", this.name);
		}
		
		marker.addInfo("Qualified Name: " + this.qualifiedName);
		if (this.instance != null)
		{
			IType vtype = this.instance.getType();
			marker.addInfo("Instance Type: " + (vtype == null ? "unknown" : vtype));
		}
		StringBuilder builder = new StringBuilder("Argument Types: [");
		Util.typesToString("", this.arguments, ", ", builder);
		marker.addInfo(builder.append(']').toString());
		return marker;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.method.writeCall(writer, this.instance, this.arguments);
		
		if (this.type != null)
		{
			IType methodType = this.method.getType();
			if (this.type != methodType && !Type.isSuperType(this.type, methodType))
			{
				writer.visitTypeInsn(Opcodes.CHECKCAST, this.type);
			}
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.method.writeCall(writer, this.instance, this.arguments);
		
		if (this.method.getType() != Type.VOID)
		{
			writer.visitInsn(Opcodes.POP);
		}
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		this.method.writeJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		this.method.writeInvJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			if (this.dotless && !Formatting.Method.useJavaFormat)
			{
				buffer.append(Formatting.Method.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.generics != null)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
		
		if (this.isSugarCall && !Formatting.Method.useJavaFormat)
		{
			if (!this.arguments.isEmpty())
			{
				buffer.append(Formatting.Method.sugarCallSeperator);
				this.arguments.get(0).toString(prefix, buffer);
			}
		}
		else
		{
			Util.parametersToString(prefix, this.arguments, buffer, true);
		}
	}
}

package dyvil.tools.compiler.ast.classes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.VariableThis;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;

public class CodeClass extends AbstractClass
{
	protected IDyvilHeader	unit;
	protected ICodePosition	position;
	
	public CodeClass()
	{
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit)
	{
		this.position = position;
		this.unit = unit;
		this.interfaces = new IType[1];
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit, int modifiers)
	{
		this.position = position;
		this.unit = unit;
		this.modifiers = modifiers;
		this.interfaces = new IType[1];
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
	
	@Override
	public void setHeader(IDyvilHeader unit)
	{
		this.unit = unit;
		
		if (this.name != null)
		{
			this.internalName = unit.getInternalName(this.name);
			this.fullName = unit.getFullName(this.name);
		}
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.unit;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
		if (this.unit != null)
		{
			this.internalName = this.unit.getInternalName(name);
			this.fullName = this.unit.getFullName(name);
		}
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.metadata == null)
		{
			this.metadata = IClass.getClassMetadata(this, this.modifiers);
		}
		
		if (this.genericCount > 0)
		{
			ClassGenericType type = new ClassGenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(markers, context);
				type.addType(new TypeVarType(var));
			}
			
			this.type = type;
		}
		else
		{
			this.type = new ClassType(this);
		}
		
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}
		
		int index = 1;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			IType type = param.getType();
			param.resolveTypes(markers, this);
			param.setIndex(index);
			if (type == Types.LONG || type == Types.DOUBLE)
			{
				index += 2;
			}
			else
			{
				index++;
			}
		}
		
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolveType(markers, this);
		}
		
		if (this.body != null)
		{
			this.body.resolveTypes(markers);
		}
		
		this.metadata.resolve(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolve(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolve(markers, this);
		}
		
		if (this.superType != null)
		{
			this.superType.resolve(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].resolve(markers, this);
		}
		
		if (this.body != null)
		{
			this.body.resolve(markers);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.metadata.checkTypes(markers, context);
		
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].checkTypes(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].checkTypes(markers, this);
		}
		
		if (this.superType != null)
		{
			this.superType.checkType(markers, this, TypePosition.SUPER_TYPE);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].checkType(markers, this, TypePosition.SUPER_TYPE);
		}
		
		if (this.body != null)
		{
			this.body.checkTypes(markers);
		}
		
		// Check Methods
		if (this.superType != null)
		{
			this.superType.getTheClass().checkMethods(markers, this, this.superType);
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			type.getTheClass().checkMethods(markers, this, type);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, this.getElementType());
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].check(markers, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			type.check(markers, context);
			
			IClass iclass = type.getTheClass();
			if (iclass == null)
			{
				continue;
			}
			
			int modifiers = iclass.getModifiers();
			if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != Modifiers.INTERFACE_CLASS)
			{
				markers.add(this.position, "class.implement.type", ModifierTypes.CLASS_TYPE.toString(modifiers), iclass.getName());
				continue;
			}
			if ((modifiers & Modifiers.SEALED) != 0 && iclass instanceof ExternalClass)
			{
				markers.add(this.position, "class.implement.sealed", iclass.getName());
			}
			if ((modifiers & Modifiers.DEPRECATED) != 0)
			{
				markers.add(this.position, "class.implement.deprecated", iclass.getName());
			}
		}
		
		if (this.superType != null)
		{
			this.superType.check(markers, context);
			
			IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				int modifiers = superClass.getModifiers();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != 0)
				{
					markers.add(this.position, "class.extend.type", ModifierTypes.CLASS_TYPE.toString(modifiers), superClass.getName());
				}
				else
				{
					if ((modifiers & Modifiers.FINAL) != 0)
					{
						markers.add(this.position, "class.extend.final", superClass.getName());
					}
					else if ((modifiers & Modifiers.SEALED) != 0 && superClass instanceof ExternalClass)
					{
						markers.add(this.position, "class.extend.sealed", superClass.getName());
					}
					if ((modifiers & Modifiers.DEPRECATED) != 0)
					{
						markers.add(this.position, "class.extend.deprecated", superClass.getName());
					}
				}
			}
		}
		
		if (this.body != null)
		{
			this.body.check(markers);
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].foldConstants();
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].foldConstants();
		}
		
		if (this.superType != null)
		{
			this.superType.foldConstants();
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].foldConstants();
		}
		
		if (this.body != null)
		{
			this.body.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(context, this);
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].cleanup(this, this);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].cleanup(this, this);
		}
		
		if (this.superType != null)
		{
			this.superType.cleanup(this, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].cleanup(this, this);
		}
		
		if (this.body != null)
		{
			this.body.cleanup();
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		// Header
		
		String signature = this.getSignature();
		String superClass = null;
		String[] interfaces = this.getInterfaceArray();
		
		if (this.superType != null)
		{
			superClass = this.superType.getInternalName();
		}
		
		int mods = this.modifiers & 0x7631;
		if ((mods & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
		{
			mods |= Opcodes.ACC_SUPER;
		}
		writer.visit(DyvilCompiler.classVersion, mods, this.internalName, signature, superClass, interfaces);
		
		// Source
		
		writer.visitSource(this.getHeader().getName() + ".dyvil", null);
		
		// Outer Class
		
		if (this.outerClass != null)
		{
			writer.visitOuterClass(this.outerClass.getInternalName(), null, null);
		}
		
		// Annotations
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0)
		{
			writer.visitAnnotation("Ldyvil/annotation/object;", true);
		}
		if ((this.modifiers & Modifiers.INTERNAL) != 0)
		{
			writer.visitAnnotation("Ldyvil/annotation/internal;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			writer.visitAnnotation("Ljava/lang/Deprecated;", true);
		}
		if ((this.modifiers & Modifiers.FUNCTIONAL) != 0)
		{
			writer.visitAnnotation("Ljava/lang/FunctionalInterface;", true);
		}
		
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(writer);
			}
		}
		
		// Inner Class Info
		
		if (this.outerClass != null)
		{
			this.writeInnerClassInfo(writer);
		}
		
		// Super Types
		
		if ((this.modifiers & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			this.metadata.write(writer, null);
			return;
		}
		
		if (this.superType != null)
		{
			IClass iclass = this.superType.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType type = this.interfaces[i];
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				iclass.writeInnerClassInfo(writer);
			}
		}
		
		// Type Parameter Variances
		
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].write(writer);
		}
		
		// Fields, Methods and Properties
		
		int fields = 0;
		int constructors = 0;
		int methods = 0;
		int properties = 0;
		if (this.body != null)
		{
			fields = this.body.fieldCount();
			methods = this.body.methodCount();
			constructors = this.body.constructorCount();
			properties = this.body.propertyCount();
			
			int classes = this.body.classCount();
			for (int i = 0; i < classes; i++)
			{
				this.body.getClass(i).writeInnerClassInfo(writer);
			}
		}
		
		ThisValue thisValue = new ThisValue(this.type, VariableThis.DEFAULT);
		StatementList instanceFields = new StatementList();
		
		IField[] staticFields = new IField[fields + 1];
		int staticFieldCount = 0;
		
		for (int i = 0; i < fields; i++)
		{
			IField f = this.body.getField(i);
			f.write(writer);
			
			if (f.hasModifier(Modifiers.LAZY))
			{
				continue;
			}
			
			if (f.hasModifier(Modifiers.STATIC))
			{
				staticFields[staticFieldCount++] = f;
			}
			else
			{
				FieldAssign assign = new FieldAssign(null, thisValue, f, f.getValue());
				instanceFields.addValue(assign);
			}
		}
		
		if (this.parameterCount > 0)
		{
			AnnotationVisitor av = writer.visitAnnotation("Ldyvil/annotation/ClassParameters;", false);
			AnnotationVisitor array = av.visitArray("names");
			
			for (int i = 0; i < this.parameterCount; i++)
			{
				IParameter param = this.parameters[i];
				param.write(writer);
				array.visit("", param.getName().qualified);
			}
			
			array.visitEnd();
		}
		
		for (int i = 0; i < constructors; i++)
		{
			this.body.getConstructor(i).write(writer, instanceFields);
		}
		
		for (int i = 0; i < properties; i++)
		{
			this.body.getProperty(i).write(writer);
		}
		
		for (int i = 0; i < methods; i++)
		{
			this.body.getMethod(i).write(writer);
		}
		
		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].write(writer);
		}
		
		this.metadata.write(writer, instanceFields);
		
		// Create the static <clinit> method
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.STATIC, "<clinit>", "()V", null, null));
		mw.begin();
		this.metadata.writeStaticInit(mw);
		for (int i = 0; i < staticFieldCount; i++)
		{
			staticFields[i].writeStaticInit(mw);
		}
		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeStaticInit(mw);
		}
		mw.end(Types.VOID);
	}
	
	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
		if (this.outerClass != null)
		{
			int mods = this.modifiers & 0x761F;
			if ((mods & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
			{
				mods |= Opcodes.ACC_STATIC;
			}
			else
			{
				mods &= ~Opcodes.ACC_STATIC;
			}
			writer.visitInnerClass(this.internalName, this.outerClass.getInternalName(), this.name.qualified, mods);
		}
	}
	
	private void writeTypes(DataOutput out) throws IOException
	{
		IType.writeType(this.superType, out);
		
		int itfs = this.interfaceCount;
		out.writeByte(itfs);
		for (int i = 0; i < itfs; i++)
		{
			IType.writeType(this.interfaces[i], out);
		}
	}
	
	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		this.writeTypes(out);
		
		int params = this.parameterCount;
		out.writeByte(params);
		for (int i = 0; i < params; i++)
		{
			IType.writeType(this.parameters[i].getType(), out);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeInt(this.modifiers);
		
		AnnotationList.write(this.annotations, out);
		
		out.writeUTF(this.name.unqualified);
		
		this.writeTypes(out);
		
		int params = this.parameterCount;
		out.writeByte(params);
		for (int i = 0; i < params; i++)
		{
			this.parameters[i].write(out);
		}
	}
	
	private void readTypes(DataInput in) throws IOException
	{
		this.superType = IType.readType(in);
		
		int itfs = in.readByte();
		this.interfaceCount = itfs;
		this.interfaces = new IType[itfs];
		for (int i = 0; i < itfs; i++)
		{
			this.interfaces[i] = IType.readType(in);
		}
	}
	
	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.readTypes(in);
		
		int params = in.readByte();
		if (this.parameterCount != 0)
		{
			this.parameterCount = params;
			for (int i = 0; i < params; i++)
			{
				this.parameters[i].setType(IType.readType(in));
			}
			return;
		}
		
		this.parameterCount = params;
		this.parameters = new IParameter[params];
		for (int i = 0; i < params; i++)
		{
			this.parameters[i] = new ClassParameter(Name.getQualified("par" + i), IType.readType(in));
		}
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.modifiers = in.readInt();
		
		this.annotations = AnnotationList.read(in);
		
		this.name = Name.get(in.readUTF());
		
		this.readTypes(in);
		
		int params = in.readByte();
		this.parameters = new IParameter[params];
		this.parameterCount = params;
		for (int i = 0; i < params; i++)
		{
			ClassParameter param = new ClassParameter();
			param.read(in);
			this.parameters[i] = param;
		}
	}
}

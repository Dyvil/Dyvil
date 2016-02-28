package dyvil.tools.compiler.ast.classes;

import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.typevar.TypeVarType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeClass extends AbstractClass
{
	protected IDyvilHeader  unit;
	protected ICodePosition position;

	protected IArguments superConstructorArguments = EmptyArguments.INSTANCE;
	
	public CodeClass()
	{
	}

	public CodeClass(ICodePosition position, Name name, ModifierSet modifiers, AnnotationList annotations)
	{
		this.position = position;
		this.name = name;
		this.modifiers = modifiers;
		this.annotations = annotations;

		this.interfaces = new IType[1];
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit)
	{
		this.position = position;
		this.unit = unit;
		this.modifiers = new ModifierList();

		this.interfaces = new IType[1];
	}
	
	public CodeClass(ICodePosition position, IDyvilHeader unit, ModifierSet modifiers)
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
	public IArguments getSuperConstructorArguments()
	{
		return this.superConstructorArguments;
	}

	@Override
	public void setSuperConstructorArguments(IArguments superConstructorArguments)
	{
		this.superConstructorArguments = superConstructorArguments;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.metadata == null)
		{
			this.metadata = IClass.getClassMetadata(this, this.modifiers.toFlags());
		}
		
		if (this.typeParameterCount > 0)
		{
			ClassGenericType type = new ClassGenericType(this);
			
			for (int i = 0; i < this.typeParameterCount; i++)
			{
				ITypeParameter var = this.typeParameters[i];
				var.resolveTypes(markers, context);
				type.addType(new TypeVarType(var));
			}
			
			this.thisType = type;
		}
		else
		{
			this.thisType = new ClassType(this);
		}
		
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(markers, this);
		}
		
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(markers, this);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolveType(markers, this);
		}
		
		this.metadata.resolveTypesHeader(markers, context);
		
		if (this.body != null)
		{
			this.body.resolveTypes(markers);
		}
		
		this.metadata.resolveTypesBody(markers, context);

		this.metadata.resolveTypesGenerate(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}
		
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, this);
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
		
		this.metadata.resolve(markers, this);
		
		if (this.body != null)
		{
			this.body.resolve(markers);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, this);
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

		this.metadata.checkTypes(markers, context);

		if (this.body != null)
		{
			this.body.checkTypes(markers);
		}
		
		this.checkSuperMethods(markers, this, this.getType(), new IdentityHashSet<>());
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		ModifierUtil.checkModifiers(markers, this, this.modifiers, Modifiers.CLASS_MODIFIERS);

		if (this.annotations != null)
		{
			this.annotations.check(markers, context, this.getElementType());
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, this);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].check(markers, this);
		}

		if (this.superType != null)
		{
			this.superType.check(markers, context);

			IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				int modifiers = superClass.getModifiers().toFlags();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != 0)
				{
					markers.add(Markers.semantic(this.position, "class.extend.type",
					                             ModifierUtil.classTypeToString(modifiers), superClass.getName()));
				}
				else if ((modifiers & Modifiers.FINAL) != 0)
				{
					markers.add(Markers.semantic(this.position, "class.extend.final", superClass.getName()));
				}
			}
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

			int modifiers = iclass.getModifiers().toFlags();
			if ((modifiers & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
			{
				markers.add(Markers.semantic(this.position, "class.implement.type",
				                             ModifierUtil.classTypeToString(modifiers), iclass.getName()));
			}
		}

		this.metadata.check(markers, context);

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

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].foldConstants();
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

		this.metadata.foldConstants();

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
		
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(this, this);
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

		this.metadata.cleanup(this, this);
		
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
		
		int modifiers = this.modifiers.toFlags();
		if ((modifiers & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
		{
			modifiers |= Opcodes.ACC_SUPER;
		}
		writer.visit(ClassFormat.CLASS_VERSION, modifiers & 0x7631, this.internalName, signature, superClass,
		             interfaces);
		
		// Source
		
		writer.visitSource(this.getHeader().getName() + ".dyvil", null);
		
		// Outer Class
		
		if (this.enclosingClass != null)
		{
			writer.visitOuterClass(this.enclosingClass.getInternalName(), null, null);
		}
		
		// Annotations
		
		this.writeAnnotations(writer, modifiers);
		
		// Inner Class Info
		
		if (this.enclosingClass != null)
		{
			this.writeInnerClassInfo(writer);
		}
		
		// Super Types
		
		if ((modifiers & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			this.metadata.write(writer);
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

		// Class Body

		this.metadata.write(writer);

		if (this.parameterCount > 0)
		{
			this.writeClassParameters(writer);
		}

		if (this.body != null)
		{
			final int methods = this.body.methodCount();
			final int constructors = this.body.constructorCount();
			final int fields = this.body.fieldCount();
			final int properties = this.body.propertyCount();
			
			int classes = this.body.classCount();
			for (int i = 0; i < classes; i++)
			{
				this.body.getClass(i).writeInnerClassInfo(writer);
			}

			for (int i = 0; i < fields; i++)
			{
				this.body.getField(i).write(writer);
			}

			for (int i = 0; i < constructors; i++)
			{
				this.body.getConstructor(i).write(writer);
			}

			for (int i = 0; i < properties; i++)
			{
				this.body.getProperty(i).write(writer);
			}

			for (int i = 0; i < methods; i++)
			{
				this.body.getMethod(i).write(writer);
			}
		}

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].write(writer);
		}
		
		// Create the static <clinit> method

		MethodWriter mw = new MethodWriterImpl(writer,
		                                       writer.visitMethod(Modifiers.STATIC, "<clinit>", "()V", null, null));
		mw.begin();
		this.writeStaticInit(mw);
		mw.end(Types.VOID);
	}

	private void writeClassParameters(ClassWriter writer) throws BytecodeException
	{
		AnnotationVisitor av = writer.visitAnnotation("Ldyvil/annotation/_internal/ClassParameters;", false);
		assert av != null;
		AnnotationVisitor array = av.visitArray("names");

		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			param.write(writer);
			array.visit("", param.getName().qualified);
		}

		array.visitEnd();
	}

	@Override
	public void writeInit(MethodWriter writer) throws BytecodeException
	{
		this.metadata.writeInit(writer);

		if (this.body != null)
		{
			for (int i = 0, count = this.body.fieldCount(); i < count; i++)
			{
				this.body.getField(i).writeInit(writer);
			}
			for (int i = 0, count = this.body.propertyCount(); i < count; i++)
			{
				this.body.getProperty(i).writeInit(writer);
			}
			for (int i = 0, count = this.body.initializerCount(); i < count; i++)
			{
				this.body.getInitializer(i).writeInit(writer);
			}
		}

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeInit(writer);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		this.metadata.writeStaticInit(writer);

		if (this.body != null)
		{
			for (int i = 0, count = this.body.fieldCount(); i < count; i++)
			{
				this.body.getField(i).writeStaticInit(writer);
			}
			for (int i = 0, count = this.body.propertyCount(); i < count; i++)
			{
				this.body.getProperty(i).writeStaticInit(writer);
			}
			for (int i = 0, count = this.body.initializerCount(); i < count; i++)
			{
				this.body.getInitializer(i).writeStaticInit(writer);
			}
		}

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeStaticInit(writer);
		}
	}

	private void writeAnnotations(ClassWriter writer, int modifiers)
	{
		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
		if ((modifiers & Modifiers.FUNCTIONAL) != 0)
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
		
		// Type Variable Annotations
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].write(writer);
		}
		
		// Super Type Variable Annotations
		if (this.superType != null)
		{
			this.superType.writeAnnotations(writer, TypeReference.newSuperTypeReference(-1), "");
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].writeAnnotations(writer, TypeReference.newSuperTypeReference(i), "");
		}
	}
	
	@Override
	public void writeInnerClassInfo(ClassWriter writer)
	{
		if (this.enclosingClass != null)
		{
			int modifiers = this.modifiers.toFlags() & 0x761F;
			if ((modifiers & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
			{
				modifiers |= Opcodes.ACC_STATIC;
			}
			else
			{
				modifiers &= ~Opcodes.ACC_STATIC;
			}
			writer.visitInnerClass(this.internalName, this.enclosingClass.getInternalName(), this.name.qualified,
			                       modifiers);
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
		ModifierSet.write(this.modifiers, out);
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
		this.modifiers = ModifierSet.read(in);
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

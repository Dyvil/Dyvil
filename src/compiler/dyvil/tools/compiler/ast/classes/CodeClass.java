package dyvil.tools.compiler.ast.classes;

import dyvil.collection.Set;
import dyvil.collection.mutable.ArraySet;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.ASMConstants;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.classes.metadata.TraitMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
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
	protected IArguments superConstructorArguments = ArgumentList.EMPTY;

	// Metadata
	protected IHeaderUnit   unit;
	protected ICodePosition position;

	protected boolean traitInit;

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

	public CodeClass(IHeaderUnit unit, Name name)
	{
		this.unit = unit;
		this.name = name;
		this.modifiers = new ModifierList();

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
	public IHeaderUnit getHeader()
	{
		return this.unit;
	}

	@Override
	public void setHeader(IHeaderUnit unit)
	{
		this.unit = unit;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public IArguments getSuperConstructorArguments()
	{
		return this.superConstructorArguments;
	}

	@Override
	public void setSuperConstructorArguments(IArguments arguments)
	{
		this.superConstructorArguments = arguments;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.metadata == null)
		{
			this.metadata = IClass.getClassMetadata(this, this.modifiers.toFlags());
		}

		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}
		this.modifiers.resolveTypes(this, markers);

		this.metadata.resolveTypesPre(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(markers, context);
		}

		this.parameters.resolveTypes(markers, context);

		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(markers, context);
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolveType(markers, context);
		}

		this.metadata.resolveTypesHeader(markers, context);

		// This has to happen here because the metadata might add 'abstract' modifiers in resolveTypesHeader
		this.checkFunctional(markers);

		if (this.body != null)
		{
			this.body.resolveTypes(markers, context);
		}

		this.metadata.resolveTypesBody(markers, context);

		this.metadata.resolveTypesGenerate(markers, context);

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, context);
		}

		this.parameters.resolve(markers, context);

		if (this.superType != null)
		{
			this.superType.resolve(markers, context);
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].resolve(markers, context);
		}

		this.metadata.resolve(markers, context);

		if (this.body != null)
		{
			this.body.resolve(markers, context);
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}

		this.metadata.checkTypes(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, context);
		}

		this.checkSuperMethods(markers, this, this.getThisType(), new IdentityHashSet<>());

		this.parameters.checkTypes(markers, context);

		if (this.superType != null)
		{
			this.superType.checkType(markers, context, TypePosition.SUPER_TYPE);
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].checkType(markers, context, TypePosition.SUPER_TYPE);
		}

		if (this.body != null)
		{
			this.body.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.annotations != null)
		{
			this.annotations.check(markers, context, this.getElementType());
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, context);
		}

		this.parameters.check(markers, context);

		if (this.superType != null)
		{
			this.superType.check(markers, context);

			final IClass superClass = this.superType.getTheClass();
			if (superClass != null)
			{
				final int modifiers = superClass.getModifiers().toFlags();
				if ((modifiers & Modifiers.CLASS_TYPE_MODIFIERS) != 0)
				{
					markers.add(Markers.semanticError(this.position, "class.extend.type",
					                                  ModifierUtil.classTypeToString(modifiers), superClass.getName()));
				}
				else if ((modifiers & Modifiers.FINAL) != 0)
				{
					markers.add(Markers.semanticError(this.position, "class.extend.final", superClass.getName()));
				}
			}
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			final IType type = this.interfaces[i];
			type.check(markers, context);

			if (!type.isResolved())
			{
				continue;
			}

			final IClass theClass = type.getTheClass();
			if (theClass == null)
			{
				continue;
			}

			if (!theClass.isInterface())
			{
				final String classType = ModifierUtil.classTypeToString(theClass.getModifiers().toFlags());
				markers
					.add(Markers.semanticError(this.position, "class.implement.type", classType, theClass.getName()));
			}
		}

		this.metadata.check(markers, context);

		if (this.body != null)
		{
			this.body.check(markers, context);
		}

		context.pop();
	}

	public void checkFunctional(MarkerList markers)
	{
		final boolean hasAnnotation = this.hasModifier(Modifiers.FUNCTIONAL);
		if (hasAnnotation && !this.isInterface())
		{
			// FunctionalInterface annotation on class or object
			markers.add(Markers.semanticError(this.position, "interface.functional.class"));
			return;
		}

		if (this.body == null)
		{
			if (hasAnnotation)
			{
				// No abstract method
				markers.add(Markers.semanticError(this.position, "interface.functional.not_found", this.name));
			}
			return;
		}

		// Partial copy in ExternalClass.getFunctionalMethod
		IMethod functionalMethod = this.body.getFunctionalMethod();
		if (functionalMethod != null)
		{
			return;
		}

		for (int i = 0, count = this.body.methodCount(); i < count; i++)
		{
			final IMethod method = this.body.getMethod(i);
			if (!method.isAbstract() || method.isObjectMethod())
			{
				continue;
			}

			if (functionalMethod == null)
			{
				functionalMethod = method;
				continue;
			}

			if (!hasAnnotation)
			{
				return;
			}

			// Multiple abstract methods
			markers.add(Markers.semanticError(this.position, "interface.functional.multiple", this.name));
			return;
		}

		if (functionalMethod != null)
		{
			this.body.setFunctionalMethod(functionalMethod);
			return;
		}

		if (hasAnnotation)
		{
			// No abstract method
			markers.add(Markers.semanticError(this.position, "interface.functional.not_found", this.name));
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

		this.parameters.foldConstants();

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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(compilableList, this);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(compilableList, this);
		}

		this.parameters.cleanup(compilableList, this);

		if (this.superType != null)
		{
			this.superType.cleanup(compilableList, this);
		}

		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].cleanup(compilableList, this);
		}

		this.metadata.cleanup(compilableList, this);

		if (this.body != null)
		{
			this.body.cleanup(compilableList, this);
		}
	}

	@Override
	public String getFullName()
	{
		if (this.fullName != null)
		{
			return this.fullName;
		}
		if (this.enclosingClass != null)
		{
			return this.enclosingClass.getFullName() + '.' + this.name;
		}
		return this.fullName = this.unit.getFullName(this.name);
	}

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}
		if (this.enclosingClass != null)
		{
			return this.enclosingClass.getInternalName() + '$' + this.name;
		}
		return this.internalName = this.unit.getInternalName(this.name);
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

		final long flags = ModifierUtil.getFlags(this);
		int modifiers = ModifierUtil.getJavaModifiers(flags);

		if ((modifiers & Modifiers.INTERFACE_CLASS) != Modifiers.INTERFACE_CLASS)
		{
			modifiers |= ASMConstants.ACC_SUPER;
		}
		writer.visit(ClassFormat.CLASS_VERSION, modifiers, this.getInternalName(), signature, superClass,
		             interfaces);

		// Source

		writer.visitSource(this.getHeader().getName() + ".dyvil", null);

		// Outer Class

		if (this.enclosingClass != null)
		{
			this.writeInnerClassInfo(writer);
		}

		// Annotations

		this.writeAnnotations(writer, flags);

		// Super Types

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

		// Compute Trait Classes
		final Set<IClass> traitClasses;
		if ((modifiers & Modifiers.INTERFACE_CLASS) == 0)
		{
			traitClasses = new ArraySet<>();
			this.traitInit = !fillTraitClasses(this, traitClasses, true) && !traitClasses.isEmpty();
		}
		else
		{
			traitClasses = null;
		}

		// Class Body

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].write(writer);
		}
		this.metadata.write(writer);

		this.writeClassParameters(writer);

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

		// Create the static <clinit> method
		MethodWriter initWriter = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.STATIC, "<clinit>", "()V",
		                                                                          null, null));
		initWriter.visitCode();
		this.writeStaticInit(initWriter);
		initWriter.visitEnd(Types.VOID);

		if (traitClasses == null || traitClasses.isEmpty())
		{
			return;
		}

		// Create the virtual <traitinit> method

		initWriter = new MethodWriterImpl(writer, writer
			                                          .visitMethod(Modifiers.PROTECTED, TraitMetadata.INIT_NAME, "()V",
			                                                       null, null));
		initWriter.visitCode();
		initWriter.setLocalType(0, this.getInternalName());

		for (IClass traitClass : traitClasses)
		{
			final String internal = traitClass.getInternalName();

			// Load 'this'
			initWriter.visitVarInsn(Opcodes.ALOAD, 0);

			// Invoke the static <traitinit> method of the trait class
			initWriter.visitMethodInsn(Opcodes.INVOKESTATIC, internal, TraitMetadata.INIT_NAME, "(L" + internal + ";)V",
			                           true);
		}

		initWriter.visitInsn(Opcodes.RETURN);
		initWriter.visitEnd();
	}

	/**
	 * Fills the list of traits and returns a status. If {@code top} is true, the returned value represents whether or
	 * not the super type of the given class has traits
	 *
	 * @param currentClass
	 * 	the current class to process
	 * @param traitClasses
	 * 	the list of trait classes
	 * @param top
	 * 	{@code true} if this is the top call
	 */
	private static boolean fillTraitClasses(IClass currentClass, Set<IClass> traitClasses, boolean top)
	{
		boolean traits = false;
		for (int i = 0, count = currentClass.interfaceCount(); i < count; i++)
		{
			final IType interfaceType = currentClass.getInterface(i);
			final IClass interfaceClass = interfaceType.getTheClass();
			if (interfaceClass == null)
			{
				continue;
			}

			if (interfaceClass.hasModifier(Modifiers.TRAIT_CLASS))
			{
				traitClasses.add(interfaceClass);
				traits = true;
			}

			fillTraitClasses(interfaceClass, traitClasses, false);
		}

		final IType superType = currentClass.getSuperType();
		final IClass superClass;
		if (superType == null || (superClass = superType.getTheClass()) == null)
		{
			return traits && !top;
		}

		return top ?
			       fillTraitClasses(superClass, traitClasses, false) :
			       traits || fillTraitClasses(superClass, traitClasses, false);
	}

	private void writeClassParameters(ClassWriter writer) throws BytecodeException
	{
		final int parameterCount = this.parameters.size();
		if (parameterCount == 0)
		{
			return;
		}

		final AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.CLASS_PARAMETERS, false);
		final AnnotationVisitor arrayVisitor = annotationVisitor.visitArray("names");

		for (int i = 0; i < parameterCount; i++)
		{
			final IParameter parameter = this.parameters.get(i);
			parameter.write(writer);
			arrayVisitor.visit("", parameter.getInternalName());
		}

		arrayVisitor.visitEnd();
		annotationVisitor.visitEnd();
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (this.traitInit)
		{
			writer.visitVarInsn(Opcodes.ALOAD, 0); // Load 'this'
			writer
				.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.getInternalName(), TraitMetadata.INIT_NAME, "()V", false);
			// Invoke the virtual <traitinit> method of this class
		}

		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeClassInit(writer);
		}

		this.metadata.writeClassInit(writer);

		if (this.body == null)
		{
			return;
		}

		for (int i = 0, count = this.body.fieldCount(); i < count; i++)
		{
			this.body.getField(i).writeClassInit(writer);
		}
		for (int i = 0, count = this.body.propertyCount(); i < count; i++)
		{
			this.body.getProperty(i).writeClassInit(writer);
		}
		for (int i = 0, count = this.body.initializerCount(); i < count; i++)
		{
			this.body.getInitializer(i).writeClassInit(writer);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.compilableCount; i++)
		{
			this.compilables[i].writeStaticInit(writer);
		}

		this.metadata.writeStaticInit(writer);

		if (this.body == null)
		{
			return;
		}

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

	private void writeAnnotations(ClassWriter writer, long flags)
	{
		ModifierUtil.writeModifiers(writer, this, flags);

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}
		if (this.hasModifier(Modifiers.FUNCTIONAL))
		{
			writer.visitAnnotation("Ljava/lang/FunctionalInterface;", true).visitEnd();
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
			IType.writeAnnotations(this.superType, writer, TypeReference.newSuperTypeReference(-1), "");
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			IType.writeAnnotations(this.interfaces[i], writer, TypeReference.newSuperTypeReference(i), "");
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

		this.parameters.writeSignature(out);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		ModifierSet.write(this.modifiers, out);
		AnnotationList.write(this.annotations, out);

		out.writeUTF(this.name.unqualified);

		this.writeTypes(out);

		this.parameters.write(out);
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

		this.parameters.readSignature(in);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.modifiers = ModifierSet.read(in);
		this.annotations = AnnotationList.read(in);

		this.name = Name.read(in);

		this.readTypes(in);

		this.parameters = ParameterList.read(in);
	}
}

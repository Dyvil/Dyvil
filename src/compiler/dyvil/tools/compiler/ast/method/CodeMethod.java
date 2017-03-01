package dyvil.tools.compiler.ast.method;

import dyvil.annotation.Reified;
import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.collection.mutable.HashSet;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvil.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.typevar.TypeVarType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

public class CodeMethod extends AbstractMethod
{
	protected IValue value;

	// Metadata
	protected IType        thisType;
	protected Set<IMethod> overrideMethods;

	public CodeMethod(IClass iclass)
	{
		super(iclass);
	}

	public CodeMethod(IClass iclass, Name name)
	{
		super(iclass, name);
	}

	public CodeMethod(IClass iclass, Name name, IType type)
	{
		super(iclass, name, type);
	}

	public CodeMethod(IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(iclass, name, type, modifiers);
	}

	public CodeMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public IType getReceiverType()
	{
		if (this.receiverType != null)
		{
			return this.receiverType;
		}

		final IType thisType = this.getThisType();
		if (thisType == this.enclosingClass.getThisType())
		{
			// If the this type was inherited from the enclosing class and not explicit, we can use the enclosing class'
			// thisType to avoid a type copying operation
			return this.receiverType = this.enclosingClass.getReceiverType();
		}
		return this.receiverType = thisType.asParameterType();
	}

	@Override
	public IType getThisType()
	{
		return this.thisType;
	}

	@Override
	public boolean setThisType(IType type)
	{
		this.thisType = type;
		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolveTypes(markers, context);

		if (this.thisType != null)
		{
			this.thisType = this.thisType.resolveType(markers, context);

			// Check the self type for compatibility
			final IType thisType = this.thisType;
			final IClass thisClass = thisType.getTheClass();
			if (thisClass != null && thisClass != this.enclosingClass)
			{
				final Marker marker = Markers.semanticError(thisType.getPosition(), "method.this_type.incompatible",
				                                            this.getName());
				marker.addInfo(Markers.getSemantic("method.this_type", thisType));
				marker.addInfo(Markers.getSemantic("method.enclosing_class", this.enclosingClass.getFullName()));
				markers.add(marker);
			}
		}
		else
		{
			this.thisType = this.enclosingClass.getThisType();
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolveTypes(markers, context);
		}

		this.parameters.resolveTypes(markers, context);
		if (this.parameters.isLastVariadic())
		{
			this.modifiers.addIntModifier(Modifiers.VARARGS);
		}

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.modifiers.addIntModifier(Modifiers.ABSTRACT);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolve(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].resolve(markers, context);
		}

		if (this.thisType != null)
		{
			this.thisType.resolve(markers, context);
		}

		this.parameters.resolve(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, context);
		}

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			boolean inferType = false;
			if (this.type == Types.UNKNOWN || this.type == null)
			{
				inferType = true;
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN && this.value.isResolved())
				{
					markers.add(Markers.semantic(this.position, "method.type.infer", this.name.unqualified));
				}
			}

			final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("method.type.incompatible",
			                                                                             "method.type", "value.type",
			                                                                             this.name);
			this.value = TypeChecker.convertValue(this.value, this.type, null, markers, context, markerSupplier);

			if (inferType)
			{
				this.type = this.value.getType();
			}
		}
		else if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		if (this.thisType != null)
		{
			this.thisType.checkType(markers, context, TypePosition.PARAMETER_TYPE);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].checkTypes(markers, context);
		}

		this.parameters.checkTypes(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, context, TypePosition.RETURN_TYPE);
		}

		if (this.value != null)
		{
			this.value.resolveStatement(this, markers);
			this.value.checkTypes(markers, context);
		}

		// Check for duplicate methods
		this.checkDuplicates(markers);

		this.checkOverrideMethods(markers);

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.check(markers, context);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].check(markers, this);
		}

		if (this.receiverType != null)
		{
			this.receiverType.check(markers, context);
		}

		this.parameters.check(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, this);

			if (!Types.isSuperType(Types.THROWABLE, exceptionType))
			{
				Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
			}
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}

		ModifierUtil.checkMethodModifiers(markers, this);

		context.pop();
	}

	private void checkDuplicates(MarkerList markers)
	{
		final Collection<IMethod> candidates = this.enclosingClass.getMethods(this.name);
		if (candidates.isEmpty())
		{
			return;
		}

		final String descriptor = this.getDescriptor();
		final String signature = this.getSignature();
		final int parameterCount = this.parameters.size();

		boolean thisMangled = !this.name.qualified.equals(this.getInternalName());

		for (IMethod method : candidates)
		{
			thisMangled = this.checkDuplicate(markers, descriptor, signature, parameterCount, thisMangled, method);
		}
	}

	private boolean checkDuplicate(MarkerList markers, String descriptor, String signature, int parameterCount,
		                              boolean thisMangled, IMethod method)
	{
		if (method == this // common cases
			    || method.getParameterList().size() != parameterCount // optimization
			    || !method.getDescriptor().equals(descriptor))
		{
			return thisMangled;
		}

		final String otherMangledName = method.getInternalName();
		if (!this.internalName.equals(otherMangledName))
		{
			return thisMangled;
		}

		// Name mangling required

		if (!thisMangled)
		{
			// ensure this method gets name-mangled
			this.internalName = createMangledName(this);
		}

		if (this.internalName.equals(otherMangledName))
		{
			markers.add(Markers.semanticError(this.position, "method.duplicate", this.name, signature));
			return true;
		}

		final Marker marker = Markers.semantic(this.position, "method.name_mangled", this.name);
		marker.addInfo(Markers.getSemantic("method.name_mangled.1", this.name));
		marker.addInfo(Markers.getSemantic("method.name_mangled.2", this.name));
		marker.addInfo(Markers.getSemantic("method.name_mangled.bytecode_name", this.internalName));
		markers.add(marker);
		return true;
	}

	private static String createMangledName(IMethod method)
	{
		// append the qualified name plus the name separator
		final StringBuilder builder = new StringBuilder(method.getName().qualified).append('_');

		final IParameterList params = method.getParameterList();
		for (int i = 0, count = params.size(); i < count; i++)
		{
			// append all param names followed by an underscore
			builder.append(params.get(i).getInternalName()).append('_');
		}

		// strip the trailing _
		return builder.deleteCharAt(builder.length() - 1).toString();
	}

	@Override
	public void addOverride(IMethod method)
	{
		if (!this.enclosingClass.isSubClassOf(method.getEnclosingClass().getClassType()))
		{
			return;
		}

		if (this.overrideMethods == null)
		{
			this.overrideMethods = new IdentityHashSet<>();
		}
		this.overrideMethods.add(method);
	}

	@Override
	public IntrinsicData getIntrinsicData()
	{
		final IAnnotation annotation = this.getAnnotation(Types.INTRINSIC_CLASS);
		if (annotation == null)
		{
			return null;
		}

		try
		{
			// FIXME dirty hack. This can probably be solved by using a recursive resolution scheme instead of a phase-based one
			annotation.resolve(null, Types.LANG_HEADER);
			return this.intrinsicData = Intrinsics.readAnnotation(this, annotation);
		}
		catch (Exception ignored)
		{
			return super.getIntrinsicData();
		}
	}

	@Override
	protected boolean checkOverride0(IMethod candidate)
	{
		return this.overrideMethods != null && this.overrideMethods.contains(candidate);
	}

	private void checkOverrideMethods(MarkerList markers)
	{
		if (this.checkNoOverride(markers))
		{
			return;
		}

		final ITypeContext typeContext = this.enclosingClass.getThisType();

		this.checkParameterLabels(markers, typeContext);

		if (this.checkNoOverride(markers))
		{
			return;
		}

		if (!this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(Markers.semantic(this.position, "method.overrides", this.name));
		}

		final boolean thisTypeResolved = this.type.isResolved();

		for (IMethod overrideMethod : this.overrideMethods)
		{
			ModifierUtil.checkOverride(this, overrideMethod, markers);

			// Type Compatibility Check

			if (!thisTypeResolved)
			{
				continue;
			}

			final IType superReturnType = overrideMethod.getType().getConcreteType(typeContext);
			if (superReturnType != this.type && superReturnType.isResolved() // avoid extra error
				    && !Types.isSuperType(superReturnType.asParameterType(), this.type))
			{
				final Marker marker = Markers
					                      .semanticError(this.position, "method.override.type.incompatible", this.name);
				marker.addInfo(Markers.getSemantic("method.type", this.type));
				marker.addInfo(Markers.getSemantic("method.override.type", superReturnType));

				addOverrideInfo(typeContext, overrideMethod, marker);
				markers.add(marker);
			}
		}
	}

	private void checkParameterLabels(MarkerList markers, ITypeContext typeContext)
	{
		for (Iterator<IMethod> iterator = this.overrideMethods.iterator(); iterator.hasNext(); )
		{
			final IMethod overrideMethod = iterator.next();
			final IClass enclosingClass = overrideMethod.getEnclosingClass();
			boolean errors = true;

			for (IMethod method : this.overrideMethods)
			{
				if (method != overrideMethod && method.getEnclosingClass() == enclosingClass)
				{
					// If this method overrides two methods from the same class, we do not produce any parameter label errors
					errors = false;
				}
			}

			final IParameterList params = overrideMethod.getParameterList();
			for (int i = 0, count = params.size(); i < count; i++)
			{
				final IParameter thisParam = this.parameters.get(i);
				final Name thisName = thisParam.getName();
				final Name otherName = params.get(i).getName();

				if (thisName == otherName || thisName == null || otherName == null)
				{
					// Parameter labels match
					continue;
				}

				if (errors)
				{
					final Marker marker = Markers.semantic(thisParam.getPosition(), "method.override.parameter_label",
					                                       i + 1, thisName, otherName);
					addOverrideInfo(typeContext, overrideMethod, marker);
					markers.add(marker);
				}

				// This method does not properly override the candidate
				iterator.remove();
			}
		}
	}

	private boolean checkNoOverride(MarkerList markers)
	{
		if (this.overrideMethods != null && !this.overrideMethods.isEmpty())
		{
			return false;
		}

		if (this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(Markers.semanticError(this.position, "method.override.notfound", this.name));
		}
		return true;
	}

	private static void addOverrideInfo(ITypeContext typeContext, IMethod overrideMethod, Marker marker)
	{
		marker.addInfo(Markers.getSemantic("method.override", Util.methodSignatureToString(overrideMethod, typeContext),
		                                   overrideMethod.getEnclosingClass().getFullName()));
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].foldConstants();
		}

		if (this.receiverType != null)
		{
			this.receiverType.foldConstants();
		}

		this.parameters.foldConstants();

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].foldConstants();
		}

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.annotations != null)
		{
			IAnnotation intrinsic = this.annotations.getAnnotation(Types.INTRINSIC_CLASS);
			if (intrinsic != null)
			{
				this.intrinsicData = Intrinsics.readAnnotation(this, intrinsic);
			}
		}

		if (this.receiverType != null)
		{
			this.receiverType.cleanup(compilableList, classCompilableList);
		}

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].cleanup(compilableList, classCompilableList);
		}

		this.parameters.cleanup(compilableList, classCompilableList);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(compilableList, classCompilableList);
		}

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final boolean interfaceClass = this.enclosingClass.isInterface();

		final long flags = ModifierUtil.getFlags(this);
		final String ownerClassName = this.enclosingClass.getInternalName();
		final String mangledName = this.getInternalName();
		final String descriptor = this.getDescriptor();
		final String signature = this.needsSignature() ? this.getSignature() : null;
		final String[] exceptionTypes = this.getInternalExceptions();

		MethodWriter methodWriter = new MethodWriterImpl(writer, writer
			                                                         .visitMethod(ModifierUtil.getJavaModifiers(flags),
			                                                                      mangledName, descriptor, signature,
			                                                                      exceptionTypes));

		if (!this.hasModifier(Modifiers.STATIC))
		{
			methodWriter.setThisType(ownerClassName);
		}

		this.writeAnnotations(methodWriter, flags);

		this.parameters.writeInit(methodWriter);

		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].writeParameter(methodWriter);
		}

		final Label start = new Label();
		final Label end = new Label();

		if (this.value != null)
		{
			methodWriter.visitCode();
			methodWriter.visitLabel(start);
			this.value.writeExpression(methodWriter, this.type);
			methodWriter.visitLabel(end);
			methodWriter.visitEnd(this.type);
		}
		else if (this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT))
		{
			// no value, but no abstract flag

			methodWriter.visitCode();
			methodWriter.visitTypeInsn(Opcodes.NEW, "java/lang/AbstractMethodError");
			methodWriter.visitInsn(Opcodes.DUP);
			methodWriter.visitLdcInsn(ownerClassName.replace('/', '.') + '.' + mangledName + descriptor);
			methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AbstractMethodError", "<init>",
			                             "(Ljava/lang/String;)V", false);
			methodWriter.visitInsn(Opcodes.ATHROW);
			methodWriter.visitEnd(this.type);
		}

		this.parameters.writeLocals(methodWriter, start, end);

		if (this.hasModifier(Modifiers.STATIC))
		{
			return;
		}

		methodWriter.visitLocalVariable("this", 'L' + ownerClassName + ';', null, start, end, 0);

		if (this.overrideMethods == null)
		{
			return;
		}

		final int lineNumber = this.getLineNumber();
		final int opcode = interfaceClass ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL;

		/*
		 * Contains entries in the format 'mangledName(paramTypes)returnType'
		 * Used to ensure unique bridge methods
		 */
		final Set<String> descriptors = new HashSet<>(1 + this.overrideMethods.size());
		descriptors.add(mangledName + descriptor);

		for (IMethod overrideMethod : this.overrideMethods)
		{
			final String overrideDescriptor = overrideMethod.getDescriptor();
			final String overrideMangledName = overrideMethod.getInternalName();
			final String overrideEntry = overrideMangledName + overrideDescriptor;

			// Check if a bridge method for the descriptor has not yet been
			// generated
			if (descriptors.contains(overrideEntry))
			{
				continue;
			}
			descriptors.add(overrideEntry);

			// Generate a bridge method
			methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
				Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE, overrideMangledName, overrideDescriptor,
				null, exceptionTypes));

			methodWriter.visitCode();
			methodWriter.setThisType(ownerClassName);

			methodWriter.visitVarInsn(Opcodes.ALOAD, 0);

			final IParameterList overrideParameterList = overrideMethod.getParameterList();

			// Generate Parameters and load arguments
			for (int p = 0, count = this.parameters.size(); p < count; p++)
			{
				final IParameter overrideParameter = overrideParameterList.get(p);
				final IType parameterType = this.parameters.get(p).getCovariantType();
				final IType overrideParameterType = overrideParameter.getCovariantType();

				overrideParameter.writeInit(methodWriter);
				methodWriter.visitVarInsn(overrideParameterType.getLoadOpcode(), overrideParameter.getLocalIndex());
				overrideParameterType.writeCast(methodWriter, parameterType, lineNumber);
			}
			// Generate Type Parameters and load reified type arguments
			for (int i = 0, count = this.typeParameterCount; i < count; i++)
			{
				final ITypeParameter thisParameter = this.typeParameters[i];
				final Reified.Type reifiedType = thisParameter.getReifiedKind();
				if (reifiedType == null)
				{
					continue;
				}

				final ITypeParameter overrideParameter = overrideMethod.getTypeParameter(i);
				this.writeReifyArgument(methodWriter, thisParameter, reifiedType, overrideParameter);

				// Extra type parameters from the overridden method are ignored
			}

			IType overrideReturnType = overrideMethod.getType();

			methodWriter.visitLineNumber(lineNumber);
			methodWriter.visitMethodInsn(opcode, ownerClassName, mangledName, descriptor, interfaceClass);
			this.type.writeCast(methodWriter, overrideReturnType, lineNumber);
			methodWriter.visitInsn(overrideReturnType.getReturnOpcode());
			methodWriter.visitEnd();
		}
	}

	private void writeReifyArgument(MethodWriter writer, ITypeParameter thisParameter, Reified.Type reifiedType,
		                               ITypeParameter overrideParameter)
	{
		overrideParameter.writeParameter(writer);
		if (overrideParameter.getReifiedKind() == null)
		{
			this.writeDefaultReifyArgument(writer, thisParameter, reifiedType);
			return;
		}

		// Delegate to the TypeVarType implementation
		final TypeVarType typeVarType = new TypeVarType(overrideParameter);

		if (reifiedType == Reified.Type.TYPE)
		{
			typeVarType.writeTypeExpression(writer);
			return;
		}
		typeVarType.writeClassExpression(writer, reifiedType == Reified.Type.OBJECT_CLASS);
	}

	private void writeDefaultReifyArgument(MethodWriter writer, ITypeParameter thisParameter, Reified.Type reifiedType)
	{
		if (reifiedType == Reified.Type.TYPE)
		{
			thisParameter.getUpperBound().writeTypeExpression(writer);
			return;
		}
		thisParameter.getUpperBound().writeClassExpression(writer, reifiedType == Reified.Type.OBJECT_CLASS);
	}

	private boolean needsSignature()
	{
		return this.typeParameterCount != 0 || this.type.needsSignature() || this.parameters.needsSignature();
	}

	protected void writeAnnotations(MethodWriter writer, long flags)
	{
		if (this.annotations != null)
		{
			this.annotations.write(writer);
		}

		// Write DyvilName annotation if it differs from the mangled name
		final String qualifiedName = this.name.qualified;
		if (!this.getInternalName().equals(qualifiedName))
		{
			final AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.DYVIL_NAME, false);
			annotationVisitor.visit("value", qualifiedName);
			annotationVisitor.visitEnd();
		}

		// Write receiver type signature
		final IType receiverType = this.receiverType;
		if (receiverType != null && receiverType != this.enclosingClass.getThisType() && receiverType.needsSignature())
		{
			final String signature = receiverType.getSignature();
			final AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.RECEIVER_TYPE, false);
			annotationVisitor.visit("value", signature);
			annotationVisitor.visitEnd();
		}

		ModifierUtil.writeModifiers(writer, this, flags);

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		// Type Variable Annotations
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].write(writer);
		}

		IType.writeAnnotations(this.type, writer, TypeReference.newTypeReference(TypeReference.METHOD_RETURN), "");
		for (int i = 0; i < this.exceptionCount; i++)
		{
			IType.writeAnnotations(this.exceptions[i], writer, TypeReference.newExceptionReference(i), "");
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		this.parameters.writeSignature(out);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.writeAnnotations(out);

		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		this.parameters.write(out);
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.type = IType.readType(in);

		this.parameters.readSignature(in);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);

		this.name = Name.read(in);
		this.type = IType.readType(in);
		this.parameters = ParameterList.read(in);
	}
}

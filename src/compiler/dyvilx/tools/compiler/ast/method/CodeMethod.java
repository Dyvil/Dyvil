package dyvilx.tools.compiler.ast.method;

import dyvil.annotation.Reified;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.intrinsic.IntrinsicData;
import dyvilx.tools.compiler.ast.method.intrinsic.Intrinsics;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.typevar.TypeVarType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CodeMethod extends AbstractMethod
{
	// =============== Fields ===============

	protected IValue value;

	// =============== Constructors ===============

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

	public CodeMethod(IClass iclass, Name name, IType type, AttributeList attributes)
	{
		super(iclass, name, type, attributes);
	}

	public CodeMethod(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

	// - - - - - - - - Value - - - - - - - -

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

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.thisType != null)
		{
			// Resolve the explicit receiver type, but do not expose type parameters of this method
			this.thisType = this.thisType.resolveType(markers, context);
		}
		else
		{
			this.thisType = this.enclosingClass.getThisType();
		}

		context = context.push(this);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolveTypes(markers, context);
		}

		// Return type has to be resolved after type parameters
		super.resolveTypes(markers, context);

		this.parameters.resolveTypes(markers, context);
		if (this.parameters.isLastVariadic())
		{
			this.attributes.addFlag(Modifiers.ACC_VARARGS);
		}

		if (this.exceptions != null)
		{
			this.exceptions.resolveTypes(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolve(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolve(markers, context);
		}

		if (this.thisType != null)
		{
			this.thisType.resolve(markers, context);
		}

		this.parameters.resolve(markers, context);

		if (this.exceptions != null)
		{
			this.exceptions.resolve(markers, context);
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
					markers.add(Markers.semanticError(this.position, "method.type.infer", this.name.unqualified));
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
			markers.add(Markers.semanticError(this.position, "method.type.abstract", this.name.unqualified));
			this.type = Types.ANY;
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.checkTypes(markers, context);
		}

		if (this.thisType != null)
		{
			this.thisType.checkType(markers, context, TypePosition.PARAMETER_TYPE);

			// Check the explicit this type for compatibility
			final IType thisType = this.thisType;
			final IClass thisClass = thisType.getTheClass();
			if (thisClass != null && thisClass != this.enclosingClass && !this.hasModifier(Modifiers.EXTENSION))
			{
				final Marker marker = Markers.semanticError(thisType.getPosition(), "method.this_type.incompatible",
				                                            this.getName());
				marker.addInfo(Markers.getSemantic("method.this_type", thisType));
				marker.addInfo(Markers.getSemantic("method.enclosing_class", this.enclosingClass.getFullName()));
				markers.add(marker);
			}
		}

		this.parameters.checkTypes(markers, context);

		if (this.exceptions != null)
		{
			this.exceptions.checkTypes(markers, context, TypePosition.RETURN_TYPE);
		}

		if (this.value != null)
		{
			this.value.resolveStatement(this, markers);
			this.value.checkTypes(markers, context);
		}
		else if (this.enclosingClass.hasModifier(Modifiers.ABSTRACT))
		{
			this.attributes.addFlag(Modifiers.ABSTRACT);
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

		if (this.typeParameters != null)
		{
			this.typeParameters.check(markers, context);
		}

		if (this.thisType != null)
		{
			this.thisType.check(markers, context);
		}

		if (this.hasModifier(Modifiers.EXTENSION) //
		    && (this.thisType == null || this.thisType == this.enclosingClass.getThisType()))
		{
			markers.add(Markers.semanticError(this.position, "method.extension.this_type.invalid", this.name));
		}

		this.parameters.check(markers, context);

		if (this.exceptions != null)
		{
			for (int i = 0; i < this.exceptions.size(); i++)
			{
				final IType exceptionType = this.exceptions.get(i);
				exceptionType.check(markers, this);

				if (!Types.isSuperType(Types.THROWABLE, exceptionType))
				{
					Marker marker = Markers.semanticError(exceptionType.getPosition(), "method.exception.type");
					marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
					markers.add(marker);
				}
			}
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}

		ModifierChecks.checkMethodModifiers(markers, this);

		context.pop();
	}

	// --------------- Duplicates ---------------

	private void checkDuplicates(MarkerList markers)
	{
		for (IMethod method : this.enclosingClass.allMethods())
		{
			if (method == this // don't match with itself
			    || this.parameters.size() != method.getParameters().size() // optimization
			    || !this.getInternalName().equals(method.getInternalName()) // same bytecode name
			    || !this.getDescriptor().equals(method.getDescriptor())) // same descriptor
			{
				continue;
			}

			markers.add(Markers
				            .semanticError(this.position, "method.duplicate.descriptor", this.name, this.internalName,
				                           this.getDescriptor()));
			return;
		}
	}

	// --------------- Overrides ---------------

	private void checkOverrideMethods(MarkerList markers)
	{
		if (this.checkNoOverride(markers))
		{
			return;
		}

		final ITypeContext typeContext = this.enclosingClass.getThisType();

		this.filterOverrides(markers, typeContext);

		if (this.checkNoOverride(markers))
		{
			return;
		}

		if (!this.isOverride() && !this.attributes.hasFlag(Modifiers.GENERATED))
		{
			markers.add(Markers.semantic(this.position, "method.overrides", this.name));
		}

		final boolean thisTypeResolved = this.type.isResolved();

		for (IMethod overrideMethod : this.overrideMethods)
		{
			ModifierChecks.checkOverride(this, overrideMethod, markers);

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

	private void filterOverrides(MarkerList markers, ITypeContext typeContext)
	{
		for (Iterator<IMethod> iterator = this.overrideMethods.iterator(); iterator.hasNext(); )
		{
			final IMethod overrideMethod = iterator.next();
			if (this.filterOverride(overrideMethod, markers, typeContext))
			{
				iterator.remove();
			}
		}
	}

	private boolean filterOverride(IMethod candidate, MarkerList markers, ITypeContext typeContext)
	{
		final String candidateInternalName = candidate.getInternalName();
		final boolean sameName = this.name == candidate.getName();
		final boolean sameInternalName = this.getInternalName().equals(candidateInternalName);

		if (sameName && !sameInternalName) // same name but different internal name
		{
			if (this.name.qualified.equals(this.internalName))
			// no AutoMangled or BytecodeName annotation, otherwise the user probably knows what they are doing and
			// doesn't need a warning
			{
				final Marker marker = Markers.semantic(this.position, "method.override.mangled_mismatch", this.name,
				                                       candidateInternalName);
				marker.addInfo(Markers.getSemantic("method.override.mangled_mismatch.info", candidateInternalName));
				markers.add(marker);
			}
			return true;
		}
		if (!sameName && sameInternalName)
		{
			final Marker marker = Markers.semanticError(this.position, "method.override.mangled_clash", this.name,
			                                            candidate.getName(), candidateInternalName);
			marker.addInfo(Markers.getSemantic("method.override.mangled_clash.info"));
			return true; // hard error so it doesn't matter if we remove or not - bytecode will never be generated
		}

		// sameName && sameInternalName should be true

		final IClass enclosingClass = candidate.getEnclosingClass();
		boolean errors = true;

		for (IMethod method : this.overrideMethods)
		{
			if (method != candidate && method.getEnclosingClass() == enclosingClass)
			{
				// If this method overrides two methods from the same class, we do not produce any parameter label errors
				errors = false;
			}
		}

		final ParameterList params = candidate.getParameters();
		for (int i = 0, count = params.size(); i < count; i++)
		{
			final IParameter thisParam = this.parameters.get(i);
			final Name thisName = thisParam.getLabel();
			final Name otherName = params.get(i).getLabel();

			if (thisName == otherName || thisName == null || otherName == null)
			{
				// Parameter labels match
				continue;
			}

			if (errors)
			{
				final Marker marker = Markers
					                      .semantic(thisParam.getPosition(), "method.override.parameter_label", i + 1,
					                                thisName, otherName);
				addOverrideInfo(typeContext, candidate, marker);
				markers.add(marker);
			}

			// This method does not properly override the candidate
			return true;
		}

		return false;
	}

	private boolean checkNoOverride(MarkerList markers)
	{
		if (this.overrideMethods != null && !this.overrideMethods.isEmpty())
		{
			return false;
		}

		if (this.isOverride())
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

	// ---------------  ---------------

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.typeParameters != null)
		{
			this.typeParameters.foldConstants();
		}

		if (this.thisType != null)
		{
			this.thisType.foldConstants();
		}

		this.parameters.foldConstants();

		if (this.exceptions != null)
		{
			this.exceptions.foldConstants();
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

		final Annotation intrinsic = this.attributes.getAnnotation(Types.INTRINSIC_CLASS);
		if (intrinsic != null)
		{
			this.intrinsicData = Intrinsics.readAnnotation(this, intrinsic);
		}

		if (this.typeParameters != null)
		{
			this.typeParameters.cleanup(compilableList, classCompilableList);
		}

		if (this.thisType != null)
		{
			this.thisType.cleanup(compilableList, classCompilableList);
		}

		this.parameters.cleanup(compilableList, classCompilableList);

		if (this.exceptions != null)
		{
			this.exceptions.cleanup(compilableList, classCompilableList);
		}

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
	}

	// --------------- Intrinsics ---------------

	@Override
	public IntrinsicData getIntrinsicData()
	{
		final Annotation annotation = this.getAnnotation(Types.INTRINSIC_CLASS);
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

	// --------------- Compilation ---------------

	private boolean needsSignature()
	{
		return this.isTypeParametric() || this.type.needsSignature() || this.parameters.needsSignature();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final boolean interfaceClass = this.enclosingClass.isInterface();
		final boolean staticAbstract = this.hasModifier(Modifiers.STATIC | Modifiers.ABSTRACT);

		int javaFlags = this.getJavaFlags();
		long dyvilFlags = this.getDyvilFlags();

		final String ownerClassName = this.enclosingClass.getInternalName();
		final String mangledName = this.getInternalName();
		final String descriptor = this.getDescriptor();
		final String signature = this.needsSignature() ? this.getSignature() : null;
		final String[] exceptionTypes = this.getInternalExceptions();

		MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(javaFlags, mangledName, descriptor,
		                                                                            signature, exceptionTypes));

		if (!this.isStatic())
		{
			methodWriter.setLocalType(0, ownerClassName);

			if (this.hasModifier(Modifiers.EXTENSION))
			{
				final IType thisType = this.getThisType();
				methodWriter.visitParameter(0, "this", thisType, 0);

				IType.writeAnnotations(thisType, writer, TypeReference.newFormalParameterReference(0), "");
			}
		}

		this.writeAnnotations(methodWriter, dyvilFlags);

		this.writeParameters(methodWriter);

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
		else if (staticAbstract)
		{
			// no value, but no abstract flag

			methodWriter.visitCode();
			methodWriter.visitTypeInsn(Opcodes.NEW, "java/lang/AbstractMethodError");
			methodWriter.visitInsn(Opcodes.DUP);
			methodWriter.visitLdcInsn(ClassFormat.internalToPackage(ownerClassName) + '.' + mangledName + descriptor);
			methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/AbstractMethodError", "<init>",
			                             "(Ljava/lang/String;)V", false);
			methodWriter.visitInsn(Opcodes.ATHROW);
			methodWriter.visitEnd(this.type);
		}

		if (!this.isStatic())
		{
			methodWriter.visitLocalVariable("this", 'L' + ownerClassName + ';', null, start, end, 0);
		}
		this.parameters.writeLocals(methodWriter, start, end);

		if (this.hasModifier(Modifiers.EXTENSION) || this.overrideMethods == null)
		{
			return;
		}

		final int lineNumber = this.lineNumber();
		final int opcode = this.getInvokeOpcode();
		final int bridgeModifiers =
			Modifiers.PUBLIC | Modifiers.SYNTHETIC | Modifiers.BRIDGE | (this.isStatic() ? Modifiers.STATIC : 0);

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
			methodWriter = new MethodWriterImpl(writer, writer.visitMethod(bridgeModifiers, overrideMangledName,
			                                                               overrideDescriptor, null, exceptionTypes));

			methodWriter.visitCode();

			if (!this.isStatic())
			{
				methodWriter.setLocalType(0, ownerClassName);
				methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
			}

			// Generate Parameters and load arguments
			this.writeBridgeParameters(methodWriter, overrideMethod);
			// Generate Type Parameters and load reified type arguments
			this.writeBridgeTypeParameters(methodWriter, overrideMethod);

			final IType overrideReturnType = overrideMethod.getType();

			methodWriter.visitLineNumber(lineNumber);
			methodWriter.visitMethodInsn(opcode, ownerClassName, mangledName, descriptor, interfaceClass);
			this.type.writeCast(methodWriter, overrideReturnType, lineNumber);
			methodWriter.visitInsn(overrideReturnType.getReturnOpcode());
			methodWriter.visitEnd();
		}
	}

	private void writeBridgeParameters(MethodWriter methodWriter, IMethod overrideMethod)
	{
		final int lineNumber = this.lineNumber();
		final ParameterList overrideParameterList = overrideMethod.getParameters();
		for (int p = 0, count = overrideParameterList.size(); p < count; p++)
		{
			final IParameter overrideParameter = overrideParameterList.get(p);
			final IType parameterType = this.parameters.get(p).getCovariantType();
			final IType overrideParameterType = overrideParameter.getCovariantType();

			overrideParameter.writeParameter(methodWriter);
			methodWriter.visitVarInsn(overrideParameterType.getLoadOpcode(), overrideParameter.getLocalIndex());
			overrideParameterType.writeCast(methodWriter, parameterType, lineNumber);
		}
	}

	private void writeBridgeTypeParameters(MethodWriter methodWriter, IMethod overrideMethod)
	{
		if (this.typeParameters != null)
		{
			final TypeParameterList overrideTypeParams = overrideMethod.getTypeParameters();

			for (int i = 0, count = this.typeParameters.size(); i < count; i++)
			{
				final ITypeParameter thisParameter = this.typeParameters.get(i);
				final Reified.Type reifiedType = thisParameter.getReifiedKind();
				if (reifiedType == null)
				{
					continue;
				}

				final ITypeParameter overrideParameter = overrideTypeParams.get(i);
				this.writeReifyArgument(methodWriter, thisParameter, reifiedType, overrideParameter);

				// Extra type parameters from the overridden method are ignored
			}
		}
	}

	protected void writeParameters(MethodWriter methodWriter)
	{
		this.parameters.write(methodWriter);

		if (this.typeParameters != null)
		{
			this.typeParameters.writeParameters(methodWriter);
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
		typeVarType.checkType(MarkerList.BLACKHOLE, this, TypePosition.REIFY_FLAG);

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

	protected void writeAnnotations(MethodWriter writer, long dyvilFlags)
	{
		this.attributes.write(writer);

		// Write DyvilName annotation if it differs from the mangled name
		if (!this.getInternalName().equals(this.name.qualified))
		{
			AnnotationUtil.writeDyvilName(writer, this.name.qualified);
		}

		// Write receiver type signature
		final IType thisType = this.getThisType();
		if (thisType != null && thisType != this.enclosingClass.getThisType())
		{
			final AnnotationVisitor annotationVisitor = writer.visitAnnotation(AnnotationUtil.RECEIVER_TYPE, false);
			annotationVisitor.visit("value", thisType.getDescriptor(IType.NAME_FULL));
			annotationVisitor.visitEnd();
		}

		ModifierUtil.writeDyvilModifiers(writer, dyvilFlags);

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			writer.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		// Type Variable Annotations
		if (this.typeParameters != null)
		{
			this.typeParameters.write(writer);
		}

		IType.writeAnnotations(this.type, writer, TypeReference.newTypeReference(TypeReference.METHOD_RETURN), "");
		if (this.exceptions != null)
		{
			for (int i = 0; i < this.exceptions.size(); i++)
			{
				IType.writeAnnotations(this.exceptions.get(i), writer, TypeReference.newExceptionReference(i), "");
			}
		}
	}

	// --------------- Serialization ---------------

	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);

		this.name = Name.read(in);
		this.type = IType.readType(in);
		this.parameters = ParameterList.read(in);
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
	public void writeSignature(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		this.parameters.writeSignature(out);
	}
}

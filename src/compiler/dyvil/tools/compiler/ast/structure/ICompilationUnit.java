package dyvil.tools.compiler.ast.structure;

import java.io.File;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.lexer.CodeFile;

public interface ICompilationUnit extends IASTNode
{
	public CodeFile getInputFile();
	
	public File getOutputFile();
	
	public void tokenize();
	
	public void parse();
	
	public void resolveTypes();
	
	public void resolve();
	
	public void checkTypes();
	
	public void check();
	
	public void foldConstants();
	
	public void compile();
}

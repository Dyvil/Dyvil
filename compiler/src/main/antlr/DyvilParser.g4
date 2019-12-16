parser grammar DyvilParser;

options {
	tokenVocab = DyvilLexer;
}

compilationUnit: EOF;

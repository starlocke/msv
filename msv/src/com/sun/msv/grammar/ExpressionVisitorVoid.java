package com.sun.tranquilo.grammar;

/**
 * ExpressionVisitor that returns void.
 */
public interface ExpressionVisitorVoid
{
	void onAttribute( AttributeExp exp );
	void onChoice( ChoiceExp exp );
	void onElement( ElementExp exp );
	void onOneOrMore( OneOrMoreExp exp );
	void onMixed( MixedExp exp );
	void onRef( ReferenceExp exp );
	void onEpsilon();
	void onNullSet();
	void onAnyString();
	void onSequence( SequenceExp exp );
	void onTypedString( TypedStringExp exp );
}
/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.grammar;

/**
 * &lt;concur&gt; pattern of TREX.
 * 
 * No of the other language needs this expression.
 * So application can deny processing this expression if neceesary.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class ConcurExp extends BinaryExp {
	
	ConcurExp( Expression left, Expression right ) {
		super(left,right,HASHCODE_CONCUR);
	}

	public Object visit( ExpressionVisitor visitor ) {
		return visitor.onConcur(this);
	}

	public Expression visit( ExpressionVisitorExpression visitor ) {
		return visitor.onConcur(this);
	}
	
	public boolean visit( ExpressionVisitorBoolean visitor ) {
		return visitor.onConcur(this);
	}

	public void visit( ExpressionVisitorVoid visitor ) {
		visitor.onConcur(this);
	}

	protected boolean calcEpsilonReducibility() {
		return exp1.isEpsilonReducible() && exp2.isEpsilonReducible();
	}
}

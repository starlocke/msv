/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.grammar;

/**
 * &lt;mixed&gt; of RELAX.
 * 
 * For TREX, this operator is not an essential one. You can use
 * <xmp>
 *   <interleave>
 *     <anyString />
 *     ...
 *   </interleave>
 * </xmp>
 * 
 * However, by introducing "mixed" as a primitive, 
 * RELAX module can be expressed without using interleave.
 * 
 * And TREX validation will also (slightly?) become faster.
 */
public class MixedExp extends UnaryExp
{
	MixedExp( Expression exp )	{ super( exp,HASHCODE_MIXED ); }
	
	public Object visit( ExpressionVisitor visitor )				{ return visitor.onMixed(this);	}
	public Expression visit( ExpressionVisitorExpression visitor )	{ return visitor.onMixed(this); }
	public boolean visit( ExpressionVisitorBoolean visitor )		{ return visitor.onMixed(this); }
	public void visit( ExpressionVisitorVoid visitor )				{ visitor.onMixed(this); }

	protected boolean calcEpsilonReducibility()
	{
		return exp.isEpsilonReducible();
	}
}

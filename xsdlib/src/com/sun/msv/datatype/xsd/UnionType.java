/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.datatype.xsd;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;
import com.sun.msv.datatype.SerializationContext;

/**
 * union type.
 * 
 * @author Kohsuke KAWAGUCHI
 */
final public class UnionType extends ConcreteType {
	/**
	 * derives a new datatype from atomic datatypes by union
	 */
	public UnionType( String newTypeName, XSDatatype[] memberTypes ) throws DatatypeException {
		super(newTypeName);

		if(memberTypes.length==0)
			throw new DatatypeException(localize(ERR_EMPTY_UNION));
		
		XSDatatypeImpl[] m = new XSDatatypeImpl[memberTypes.length];
		System.arraycopy( memberTypes, 0, m, 0, memberTypes.length );
		
		for( int i=0; i<m.length; i++ )
			if( m[i].isFinal(DERIVATION_BY_UNION) )
				throw new DatatypeException(localize(
					ERR_INVALID_MEMBER_TYPE, m[i].displayName() ));
		
		this.memberTypes = m;
	}
	
	/** member types */
	final public XSDatatypeImpl[] memberTypes;

	// union type is not an atom type.
	public final boolean isAtomType() { return false; }
	
	public final int isFacetApplicable( String facetName ) {
		if( facetName.equals(FACET_PATTERN)
		||	facetName.equals(FACET_ENUMERATION) )
			return APPLICABLE;
		else
			return NOT_ALLOWED;
	}
	
	protected final boolean checkFormat( String content, ValidationContext context ) {
		for( int i=0; i<memberTypes.length; i++ )
			if( memberTypes[i].checkFormat(content,context) )	return true;
		
		return false;
	}
	
	public Object convertToValue( String content, ValidationContext context ) {
		Object o;
		for( int i=0; i<memberTypes.length; i++ ) {
			o = memberTypes[i].convertToValue(content,context);
			if(o!=null)		return o;
		}
		
		return null;
	}
	public Class getJavaObjectType() {
		// TODO: find the common base type, if it's possible.
		return Object.class;
	}
	
	public String convertToLexicalValue( Object o, SerializationContext context ) {
		for( int i=0; i<memberTypes.length; i++ ) {
			try {
				return memberTypes[i].convertToLexicalValue(o,context);
			} catch( Exception e ) {
				;	// ignore
			}
		}
		
		throw new IllegalArgumentException();
	}
	
	protected void diagnoseValue(String content, ValidationContext context) throws DatatypeException {
		// what is the appropriate implementation for union?
		if( checkFormat(content,context) )		return;
		else	throw new DatatypeException();
	}

}

/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.generator;

import com.sun.msv.datatype.*;
import java.util.Random;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import com.sun.xml.util.XmlChars;

/**
 * default implementation of DataTypeGenerator.
 * 
 * You may want to override this class to implement custom generator for
 * unimplemented datatype or datatype local to your schema.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class DataTypeGeneratorImpl implements DataTypeGenerator {
	private final Random random;
	
	public DataTypeGeneratorImpl() { this(new Random()); }
	public DataTypeGeneratorImpl( Random random ) {
		this.random = random;
	}
	
	/**
	 * if this flag is set to true, then non-ASCII characters will not be used.
	 */
	public boolean asciiOnly = false;
	
	/**
	 * map from DataType to Set that holds generated values for types.
	 * This map is used when we fail to generate an appropriate value for a type.
	 */
	protected Map generatedValues = new java.util.HashMap();
	
	/**
	 * set that contains tokens that are found in example files.
	 * This set is used as the last resort to generate a value for a type.
	 */
	protected Set tokens;

	public String generate( DataType dt, ContextProvider context ) {
		String s=null; int i;

		// obtain previously generated values.
		Set vs = (Set)generatedValues.get(dt);
		if(vs==null) {
			generatedValues.put(dt, vs=new java.util.HashSet() );
			
			// copy values from examples.
			Iterator itr = tokens.iterator();
			while(itr.hasNext()) {
				String token = (String)itr.next();
				try {// we have to be able to verify this without depending on the context.
					if(dt.verify(token,null))
						vs.add(token);
				}catch(Exception e){}
			}
		}

		if(vs.size()<32 || random.nextBoolean() ) {
			// we need more diversity. generate more.
			// we have to continue producing values, especially for
			// ID values.
			
			for( i=0; i<100; i++ ) {
				s = _generate(dt,context);
				if( s!=null && dt.verify(s,context) ) {
					// memorize generated values so that we can use them later.
					vs.add(s);
					break;	// this value is OK.
				}
			}
			if(i==100) {
				if( vs.size()==0 )
					// we retried 10 times but failed to generate a value.
					// and no example is available.
					// So this situation is an absolute failure.
					fail(dt);
				else
					s = (String)vs.toArray()[random.nextInt(vs.size())];
			}
		} else {
			// we have enough diversity. use it.
			s = (String)vs.toArray()[random.nextInt(vs.size())];
		}
		
		
		return s;
	}
		
	/**
	 * actual generation.
	 * this method can return an invalid value.
	 */
	protected String _generate( DataType dt, ContextProvider context ) {
		if( dt instanceof AnyURIType ) {
			// anyURI
			String r;
			do {
				r = generateString();	// any string should work
			}while(!dt.verify(r,context));
			return r;
		}
		
		if( dt instanceof NonNegativeIntegerType ) {
			long r;
			do { r=random.nextLong(); }while(r<0);
			return Long.toString(r);
		}
		
		if( dt instanceof PositiveIntegerType ) {
			long r;
			do { r=random.nextLong(); }while(r<=0);
			return Long.toString(r);
		}
		
		if( dt.getClass()==ShortType.class )	return Long.toString( (short)random.nextInt() );
		if( dt.getClass()==IntType.class )		return Long.toString( random.nextInt() );
		if( dt.getClass()==LongType.class )		return Long.toString( random.nextLong() );
		if( dt instanceof IntegerType )			return Long.toString( random.nextLong() );
		if( dt.getClass()==StringType.class )	return generateString();
		if( dt.getClass()==TokenType.class )	return generateString();
		if( dt.getClass()==NormalizedStringType.class )	return generateString();
		if( dt.getClass()==NmtokenType.class )	return generateNMTOKEN();
		if( dt.getClass()==NcnameType.class )	return generateNCName();
		if( dt.getClass()==NumberType.class )	return generateDecimal();
		if( dt.getClass()==BooleanType.class )	return generateBoolean();
		// TODO: implement this method better.
		if( dt.getClass()==QnameType.class )	return generateNCName();
		
		if( dt instanceof FinalComponent )	// ignore final component
			return generate( ((FinalComponent)dt).baseType, context );
		
		if( dt instanceof com.sun.msv.grammar.relax.EmptyStringType )
			return "";
		
		if( dt instanceof com.sun.msv.grammar.trex.TypedString )
			return ((com.sun.msv.grammar.trex.TypedString)dt).value;
		
		
		
		// getting desparate...
		
		if( dt instanceof DataTypeImpl ) {
			// if it contains EnumerationFacet, we can try that.
			DataTypeImpl dti = (DataTypeImpl)dt;
			EnumerationFacet e = (EnumerationFacet)dti.getFacetObject( dti.FACET_ENUMERATION );
			if(e!=null) {
				Object[] items = e.values.toArray();
				for( int i=0; i<10; i++ ) {
					try {
						return dt.convertToLexicalValue(items[random.nextInt(items.length)],context);
					} catch( Exception x ) { ; }
				}
			}
			
			DataType baseType = dti.getConcreteType();
			
			if( baseType instanceof ListType )
				return generateList(dti,context);
			if( baseType instanceof UnionType )
				return generateUnion((UnionType)baseType,context);
			
			if( baseType!=dti )
				return generate(baseType,context);
		}
		
		// use previously generated value if such a thing exists.
		Set vs = (Set)generatedValues.get(dt);
		if(vs!=null && vs.size()!=0 )
			return (String)vs.toArray()[random.nextInt(vs.size())];
		
		
		return null;
	}

	protected void fail( DataType dt ) {
		throw new GenerationException("unable to generate value for this datatype: " + dt.displayName() );
	}

	protected String generateNMTOKEN() {
		// string
		int len = random.nextInt(15)+1;
		String r = "";
		for( int i=0; i<len; i++ ) {
			char ch;
			do {
				if( asciiOnly )
					ch = (char)random.nextInt(128);
				else
					ch = (char)random.nextInt(Character.MAX_VALUE);
			}while( !XmlChars.isNameChar(ch) );
			r += ch;
		}
		return r;
	}
	
	protected String generateUnion(UnionType ut, ContextProvider context ) {
		return generate( ut.memberTypes[random.nextInt(ut.memberTypes.length)], context );
	}
		
	protected String generateList(DataTypeImpl dti, ContextProvider context) {
		ListType base = (ListType)dti.getConcreteType();
		LengthFacet lf = (LengthFacet)dti.getFacetObject(dti.FACET_LENGTH);
		int n;	// compute # of items into this value.
		
		if(lf!=null) {
			n = lf.length;
		} else {
			MaxLengthFacet xlf = (MaxLengthFacet)dti.getFacetObject(dti.FACET_MAXLENGTH);
			int max = (xlf!=null)?xlf.maxLength:16;
			MinLengthFacet nlf = (MinLengthFacet)dti.getFacetObject(dti.FACET_MINLENGTH);
			int min = (nlf!=null)?nlf.minLength:0;
			
			n = random.nextInt(max-min)+min;
		}
		
		String s="";
		for( int i=0; i<n; i++ )
			s += " " + generate(base.itemType,context) + " ";
		return s;
	}
	
	protected String generateNCName() {
		String r;
		do {
			r = generateNMTOKEN();
		}while( !XmlNames.isNCName(r) );
		return r;
	}
	
	protected String generateDecimal() {
		return random.nextLong()+"."+random.nextInt(1000);
	}
		
	protected String generateBoolean() {
		switch(random.nextInt(4)) {
		case 0:		return "true";
		case 1:		return "false";
		case 2:		return "0";
		case 3:		return "1";
		default:	throw new Error();
		}
	}
		
	protected String generateString() {
		// string
		int len = random.nextInt(16);
		String r = "";
		for( int i=0; i<len; i++ ) {
			char ch;
			do {
				if( asciiOnly )
					ch = (char)random.nextInt(128);
				else
					ch = (char)random.nextInt(Character.MAX_VALUE);
			}while( !XmlChars.isChar(ch) || Character.isISOControl(ch) );
			r += ch;
		}
		return r;
	}
}

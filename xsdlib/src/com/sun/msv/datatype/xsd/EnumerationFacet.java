package com.sun.tranquilo.datatype;

import java.util.Set;
import java.util.Vector;

/**
 * "enumeration" facets validator
 */
public class EnumerationFacet extends DataTypeWithValueConstraintFacet
{
	protected EnumerationFacet( String typeName, DataTypeImpl baseType, Facets facets )
		throws BadTypeException
	{
		super(typeName,baseType,FACET_ENUMERATION,facets);
		
		// converts all lexical values into the value of value space.
		Vector lexValues = (Vector)facets.getVector(FACET_ENUMERATION);
		int len = lexValues.size();
		
		for( int i=0; i<len; i++ )
		{
			// loosened enumeration value will be detected in this process.
			
			final String val = (String)lexValues.elementAt(i);
			Object o = baseType.convertToValueObject(val);
			if(o==null)
			{
				throw new BadTypeException(
					BadTypeException.ERR_INVALID_VALUE_FOR_THIS_TYPE,
					val, baseType.getName() );
			}
			
			values.add(o);
		}
		
		facets.consume(FACET_ENUMERATION);
	}
	
	/** set of valid values */
	private final Set values = new java.util.HashSet();

	public Object convertToValue( String literal )
	{
		Object o = baseType.convertToValue(literal);
		if(o==null || !values.contains(o))		return null;
		return o;
	}
	
	protected DataTypeErrorDiagnosis diagnoseByFacet(String content)
	{
		if( convertToValue(content)!=null )	return null;
			
		return new DataTypeErrorDiagnosis(this, content, -1,
			DataTypeErrorDiagnosis.ERR_ENUMERATION );
	}

}

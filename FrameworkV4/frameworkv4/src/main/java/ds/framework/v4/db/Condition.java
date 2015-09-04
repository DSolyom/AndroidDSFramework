/*
	Copyright 2011 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package ds.framework.v4.db;

import java.util.ArrayList;


public class Condition {
	
	public static final int EQUALS = 0;
	public static final int LOWER = 1;
	public static final int HIGHER = 2;
	public static final int EQUALS_OR_LOWER = 3;
	public static final int EQUALS_OR_HIGHER = 4;
	
	public static final int IN = 9;
	public static final int BETWEEN = 10;

    public static final int IS = 11;
	
	String mField;
	int mRelation;
	String mStringRelation;
	String mStrValue;
	Long mLongValue;
	Float mFloatValue;
	int[] mIntArrayValue;
	Object[] mArrayValue;

    /**
     *
     * @param field
     * @param value
     */
	public Condition(String field, String value) {
		this(field, EQUALS, value);
	}

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, String relation, String value) {
		mField = field;
		mStringRelation = relation;
		mStrValue = value;
	}

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, int relation, String value) {
		mField = field;
		mRelation = relation;
		mStringRelation = null;
		mStrValue = value;
	}

    /**
     *
     * @param field
     * @param value
     */
	public Condition(String field, int value) {
		this(field, EQUALS, value);
	}

    /**
     *
     * @param field
     * @param value
     */
    public Condition(String field, long value) {
        this(field, EQUALS, value);
    }

    /**
     *
     * @param field
     * @param value
     */
    public Condition(String field, boolean value) {
        this(field, value ? HIGHER : EQUALS, 0);
    }

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, int relation, int value) {
		this(field, relation, (long) value);
	}

	/**
	 *
	 * @param field
	 * @param relation
	 * @param value
	 */
	public Condition(String field, int relation, long value) {
		mField = field;
		mRelation = relation;
		mStringRelation = null;
		mLongValue = value;
	}

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, String relation, int value) {
        this(field, relation, (long) value);
    }

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
    public Condition(String field, String relation, long value) {
		mField = field;
		mStringRelation = relation;
		mLongValue = value;
	}

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, int relation, float value) {
		mField = field;
		mRelation = relation;
		mStringRelation = null;
		mFloatValue = value;
	}

    /**
     *
     * @param field
     * @param relation
     * @param value
     */
	public Condition(String field, String relation, float value) {
		mField = field;
		mStringRelation = relation;
		mFloatValue = value;
	}

    /**
     *
     * @param field
     * @param values
     */
	public Condition(String field, int[] values) {
		mField = field;
		mRelation = IN;
		mStringRelation = null;
		mIntArrayValue = values;
	}

    /**
     *
     * @param field
     * @param values
     */
	public Condition(String field, ArrayList<Integer> values) {
		final int[] intValues = new int[values.size()];
		
		int at = 0;
		for(Integer value : values) {
			intValues[at++] = value;
		}
		
		mField = field;
		mRelation = IN;
		mStringRelation = null;
		mIntArrayValue =intValues;
	}

    /**
     *
     * @param field
     * @param values
     */
	public Condition(String field, Object[] values) {
		mField = field;
		mRelation = IN;
		mStringRelation = null;
		mArrayValue = values;
	}

    /**
     *
     * @return
     */
	@Override
	public String toString() {
		String ret = DbQuery.quoteName(mField);
		if (mStringRelation != null) {
			ret += " " + mStringRelation + " ";
		} else switch(mRelation) {
			case IN:
				ret += " IN ";
				break;
				
			case LOWER:
				ret += " < ";
				break;
			
			case HIGHER:
				ret += " > ";
				break;

			case EQUALS_OR_LOWER:
				ret += " <= ";
				break;
			
			case EQUALS_OR_HIGHER:
				ret += " >= ";
				break;

            case IS:
                ret += " IS ";
                break;

			case EQUALS:
			default:
				ret += " = ";
				break;
		}
		if (mStrValue != null) {
			ret += DbQuery.quoteNameOrValue(mStrValue);
		} else if (mLongValue != null) {
			ret += mLongValue;
		} else if (mFloatValue != null) {
			ret += mFloatValue;
		} else if (mIntArrayValue != null) {
			ret += DbQuery.quoteValue(mIntArrayValue);
		} else if (mArrayValue != null) {
			ret += DbQuery.quoteValue(mArrayValue);
		} else if (mStrValue == null) {
			ret += "NULL";
		}
		
		return ret;
	}
}

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
import java.util.Arrays;

public class ConditionTree {
	
	public static final int RELATION_UNKNOWN = 0;
	public static final int AND = 1;
	public static final int OR = 2;
	public static final int XOR = 3;
	
	int mRelation = RELATION_UNKNOWN;
	public final ArrayList<ConditionTree> mChildren = new ArrayList<ConditionTree>();
	public Condition mCondition;
	
	public ConditionTree() {
		;
	}
	
	public ConditionTree(int relation) {
		mRelation = relation;		
	}
	
	public ConditionTree(Condition condition) {
		mCondition = condition;
	}
	
	public ConditionTree(int relation, Condition... conditions) {
		mRelation = relation;
		for(Condition condition : conditions) {
			mChildren.add(new ConditionTree(condition));
		}
	}
	
	public ConditionTree(int relation, ConditionTree... conditions) {
		mRelation = relation;
		mChildren.addAll(Arrays.asList(conditions));
	}

	/**
	 * adds a condition to the tree
	 * 
	 * @param relation
	 * @param condition
	 * @return the new root of the tree
	 */
	public ConditionTree addCondition(int relation, Condition child) {
		ConditionTree root;
		if (mRelation != relation) {
			root = new ConditionTree(relation);
			if (mRelation != RELATION_UNKNOWN) {
				root.mChildren.add(this);
			}
		} else {
			root = this;
		}
			
		root.mChildren.add(new ConditionTree(child));
		
		return root;
	}
	
	public ConditionTree addCondition(int relation, ConditionTree other) {
		if (other.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return other;
		}
		
		if (relation == other.mRelation && relation == mRelation) {
			mChildren.addAll(other.mChildren);
			return this;
		}
		
		ConditionTree root = new ConditionTree(relation);

		root.mChildren.add(this);
		root.mChildren.add(other);
		
		return root;
	}
	
	public ConditionTree addCondition(int relation, ConditionTree... others) {
		ConditionTree ret = this;
		
		for(ConditionTree other : others) {
			ret = ret.addCondition(relation, other);
		}
		return ret;
	}
	
	public ConditionTree merge(ConditionTree other) {
		return addCondition(AND, other);
	}
	
	public boolean isEmpty() {
		return mChildren.size() == 0 && mCondition == null;
	}
	
	@Override
	public String toString() {
		if (mCondition != null) {
			return mCondition.toString();
		}
		String ret = "";
		
		for(ConditionTree tree: mChildren) {
			if (ret.length() > 0) {
				switch(mRelation) {
					case OR:
						ret += " OR ";
						break;
						
					case XOR:
						ret += " XOR ";
						break;
				
					case AND:
					default:
						ret += " AND ";
						break;
				}
			}
			ret += "(" + tree.toString() + ")";
		}
		
		return ret;
	}
}

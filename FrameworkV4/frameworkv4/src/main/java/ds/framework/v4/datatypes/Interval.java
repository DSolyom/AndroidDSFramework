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

package ds.framework.v4.datatypes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class Interval implements Serializable {

	private static final long serialVersionUID = 1L;

	public int start;
	public int end;
	
	public Interval() {
		;
	}
	
	public Interval(int s, int e) {
		if (s > e) {
			s += e;
			e = s - e;
			s = s - e;
		}
		start = s;
		end = e;
	}
	
	/**
	 * substract other interval
	 * 
	 * @param other
	 * @return - if result is two intervals than return one of the parts, null otherwise
	 */
	public Interval substract(Interval other) {
		if (other.end + 1 < start || other.start > end - 1) {
			return null;
		}
		
		if (other.start > start && other.end < end) {
			end = other.start;
			return new Interval(other.end, end);
		}
		if (other.start > start) {
			end = other.start;
		} else {
			start = other.end;
		}
		
		return null;
	}
	
	/**
	 * merge intervals
	 * 
	 * @param other
	 * @return - false if they are not overlapping or neighbors
	 */
	public boolean merge(Interval other) {
		if (other.end < start || other.start > end) {
			return false;
		}
		
		mergeStart(other.start);
		mergeEnd(other.end);
		
		return true;
	}
	
	/**
	 * merge intervals - all overlapping makes one new
	 * 
	 * @param intervals - !make sure to copy intervals first if needed as these will be changed here
	 * @return
	 */
	public static Interval[] mergeAllPossible(Interval[] intervals) {
		sort(intervals);
		
		int sI = intervals.length;
		int mergeWith = 0;
		for(int i = 1; i < sI; ++i) {
			if (!intervals[mergeWith].merge(intervals[i])) {
				intervals[++mergeWith] = intervals[i];
			}
		}
		
		Interval[] ret = new Interval[mergeWith + 1];
		System.arraycopy(intervals, 0, ret, 0, mergeWith + 1);
		
		return ret;
	}
	
	/**
	 * merge intervals
	 * 
	 * @param intervals - !make sure to copy intervals first if needed as these will be changed here
	 * @return - null if they are not all overlapping or neighbors
	 */
	public static Interval merge(Interval[] intervals) {
		sort(intervals);
		
		int sI = intervals.length;
		for(int i = 1; i < sI; ++i) {
			if (!intervals[0].merge(intervals[i])) {
				return null;
			}
		}

		return intervals[0];
	}
	
	public void mergeStart(int otherStart) {
		if (otherStart < start) {
			start = otherStart;
		}	
	}
	
	public void mergeEnd(int otherEnd) {
		if (otherEnd > end) {
			end = otherEnd;
		}	
	}
	
	public boolean isEmpty() {
		return !(start < end);
	}
	
	public boolean contains(Interval other) {
		return (other.start >= start && other.end <= end);
	}
	
	public static void sort(Interval[] intervals) {
		Arrays.sort(intervals, new Comparator<Interval>() {
			
			@Override
			public int compare(Interval left, Interval right) {
				if (left.start == right.start) {
					return (left.end > right.end ? 1 : -1);
				}
				return (left.start > right.start ? 1 : -1);
			}
		});
	}

	public int length() {
		return end - start;
	}
}

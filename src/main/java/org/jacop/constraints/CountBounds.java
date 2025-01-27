/*
 * CountBounds.java
 * This file is part of JaCoP.
 * <p>
 * JaCoP is a Java Constraint Programming solver.
 * <p>
 * Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * Notwithstanding any other provision of this License, the copyright
 * owners of this work supplement the terms of this License with terms
 * prohibiting misrepresentation of the origin of this work and requiring
 * that modified versions of this work be marked in reasonable ways as
 * different from the original version. This supplement of the license
 * terms is in accordance with Section 7 of GNU Affero General Public
 * License version 3.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jacop.constraints;

import org.jacop.api.SatisfiedPresent;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.core.TimeStamp;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CountBounds constraint implements the counting over number of occurrences of
 * a given value in a list of variables. The number of occurrences is
 * specified by lower bound and upper bound, lb and ub.
 *
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.7
 */

public class CountBounds extends Constraint implements SatisfiedPresent {

    final static AtomicInteger idNumber = new AtomicInteger(0);

    /**
     * It specifies a lower and upper bounds of occurences of the specified value in a list.
     */
    final int lb, ub;

    /**
     * The list of variables which are checked and counted if equal to specified value.
     */
    final public IntVar list[];

    /**
     * The value to which is any variable is equal to makes the constraint count it.
     */
    final public int value;

    /*
     * Defines first position of the variable that are not considered;
     * either equal to value or missing the value in their domain.
     */
    private TimeStamp<Integer> position;

    /*
     * Defines number of variables equal to the value.
     */
    private TimeStamp<Integer> equal;

    /**
     * It constructs a CountBounds constraint.
     *
     * @param value   value which is counted
     * @param list    variables which equality to val is counted.
     * @param lb      minimal number of occurences of value at list.
     * @param ub      maximal number of occurences of value at list.
     */
    public CountBounds(IntVar[] list, int value, int lb, int ub) {

        checkInputForNullness("x", list);

        this.queueIndex = 1;
        this.numberId = idNumber.incrementAndGet();

        this.list = Arrays.copyOf(list, list.length);
        this.lb = lb;
	this.ub = ub;
        this.value = value;

        setScope(list);

    }

    /**
     * It constructs a CountBounds constraint.
     *
     * @param value   value which is counted
     * @param list    variables which equality to val is counted.
     * @param lb      minimal number of occurences of value at list.
     * @param ub      maximal number of occurences of value at list.
     */
    public CountBounds(List<? extends IntVar> list, int value, int lb, int ub) {
        this(list.toArray(new IntVar[list.size()]), value, lb, ub);
    }

    // registers the constraint in the constraint store and
    // initialize stateful variables
    @Override public void impose(Store store) {

        super.impose(store);

        position = new TimeStamp<>(store, 0);
        equal = new TimeStamp<>(store, 0);
    }

    @Override public int getDefaultConsistencyPruningEvent() {
        return IntDomain.ANY;
    }

    @Override public void consistency(final Store store) {

        int numberEq = equal.value();
	int numberMayBe = 0;
	int start = position.value();
        for (int i = start; i < list.length; i++) {
	    IntVar v = list[i];
            if (v.domain.contains(value))
                if (v.singleton()) {
                    numberEq++;
		    swap(start, i);
		    start++;
		}
                else
                    numberMayBe++;
	    else { // does not have the value in its domain
                swap(start, i);
		start++;
	    }	    
        }

	if (numberEq > ub || numberMayBe + numberEq < lb) {
	    throw store.failException;
	}
	else if (numberEq >= lb && numberMayBe + numberEq <= ub) {
            removeConstraint();
	}	
        else if (numberMayBe + numberEq == lb) {
            for (int i = start; i < list.length; i++) {
		IntVar v = list[i];
		v.domain.in(store.level, v, value, value);
            }

            removeConstraint();
        } else if (numberEq == ub) {
            for (int i = start; i < list.length; i++) {
		IntVar v = list[i];
		v.domain.inComplement(store.level, v, value);
	    }
	    
            removeConstraint();
        }

	equal.update(numberEq);
	position.update(start);	
    }

    private void swap(int i, int j) {
        if (i != j) {
            IntVar tmp = list[i];
            list[i] = list[j];
            list[j] = tmp;
        }
    }

    @Override public boolean satisfied() {

        if (!grounded())
            return false;

        int countAll = 0;

        for (IntVar v : list)
            if (v.singleton(value))
                countAll++;
        return (countAll >= lb && countAll <= ub);
    }

    @Override public String toString() {

        StringBuilder result = new StringBuilder(id());

        result.append(" : CountBounds(").append("[");

        for (int i = 0; i < list.length; i++) {
            result.append(list[i]);
            if (i < list.length - 1)
                result.append(", ");
        }

        result.append("], ").append(value).append(", ").append(lb).append(", ").append(ub).append(" )");

        return result.toString();

    }

}

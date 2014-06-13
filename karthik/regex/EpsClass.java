/*
 * Copyright (C) 2014 karthik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package karthik.regex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author karthik
 */
class EpsClass implements Matchable {

    private static EpsClass EpsClassRef = null;

    private EpsClass() {
    }

    static EpsClass getEpsClass() {
        if (EpsClassRef == null)
            EpsClassRef = new EpsClass();

        return EpsClassRef;
    }

    public boolean isQuantifier() {
        return false;
    }

    public boolean isEpsilon() {
        return true;
    }

    public boolean isBoundaryOrLookaround() {
        return false;
    }

    public boolean isBackReference() {
        return false;
    }

    public boolean matches(final CharSequence s, int pos) {
        return false;
    }

    public List<Integer> getGroupID() {
        return new ArrayList<Integer>(); // returns empty list
    }

    public int getFlags() {
        return 0;
    }

    public String toString() {
        return "EPSILON";
    }

}

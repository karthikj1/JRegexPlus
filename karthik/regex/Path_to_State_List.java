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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author karthik
 */
class Path_to_State_List {

    Map<Integer, Path_to_State> states;

    Path_to_State_List() {
        states = new HashMap<>();
    }

    boolean containsKey(Integer key) {
        return states.containsKey(key);
    }

    Path_to_State get(Integer key) {
        return states.get(key);
    }

    Path_to_State put(Integer key, Path_to_State stateObj) {
        return states.put(key, stateObj);
    }

    boolean isEmpty() {
        return states.isEmpty();
    }

    Set<Integer> keySet() {
        return states.keySet();
    }
}

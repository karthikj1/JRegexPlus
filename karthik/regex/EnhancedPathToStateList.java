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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author karthik
 */
class EnhancedPathToStateList {

    Map<Integer, List<PathToState>> states;

    EnhancedPathToStateList() {
        states = new HashMap<>();
    }

    boolean containsKey(Integer key) {
        return states.containsKey(key);
    }

    List<PathToState> get(Integer key) {
        // returns list of state objects corresponding to given key
        // or returns an empty list to avoid returning null

        if (states.containsKey(key))
            return states.get(key);
        else
            return new ArrayList<>();
    }

    PathToState put(Integer key, PathToState stateObj) {
        if (states.containsKey(key))
            states.get(key).add(stateObj);
        else {
            List<PathToState> newList = new ArrayList<>();
            newList.add(stateObj);
            states.put(key, newList);
        }
        return stateObj;
    }

    void putUnique(Integer key, PathToState stateObj) // adds key only if stateObj is not already in the map for the given key
    {
        if (states.containsKey(key)) {
            boolean isUnique = true;
            List<PathToState> values = states.get(key);
            for (PathToState value_obj : values)
                if (value_obj.equals(stateObj)) {
                    isUnique = false;
                    break;
                }

            if (isUnique)
                values.add(stateObj);
        } else {
            List<PathToState> newList = new ArrayList<>();
            newList.add(stateObj);
            states.put(key, newList);
        }
    }

    boolean isEmpty() {
        return states.isEmpty();
    }

    Set<Integer> keySet() {
        return states.keySet();
    }
}

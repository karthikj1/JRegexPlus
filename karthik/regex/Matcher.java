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

/**
 *
 * @author karthik
 */
public class Matcher {

    private final NFASimulator transitions;
    private List<Integer[][]> results;
    private CharSequence search_string;
    private int startPos;
    private int region_end;
    private Map<String, Integer> group_names = new HashMap<>();

    Matcher(TransitionTable m) {

        if (m.contains_backref())
            transitions = new EnhancedNFASimulator(m);
        else
            transitions = new NFASimulator(m);

        results = new ArrayList<>();
        startPos = 0;
    }

    Matcher(TransitionTable m, Map<String, Integer> names) {
        this(m);
        group_names = names;
    }

    public Matcher reset() {
        startPos = 0;
        return this;
    }

    boolean matchFromStart(final CharSequence s) throws MatcherException {
        // used to find lookarounds
        return find(s, true);
    }

    public boolean find(final CharSequence s) throws MatcherException {
        return find(s, false);
    }

    private boolean find(final CharSequence s, boolean match_from_start) throws MatcherException {
        PathToState result;

        results = new ArrayList<>();
        search_string = s;
        region_end = s.length();

        if (match_from_start)
            startPos = 0;

        do {
            result = transitions.findOneMatch(search_string, startPos,
                region_end - 1);
            if (result == null)
                startPos++;
            else {
                results.add(result.get_matches_from_state());
                startPos = startPos + result.resultStringLength();
                // prevent engine from endless loop if there is a zero-length find
                if (result.resultStringLength() == 0)
                    startPos++;
                break;
            }
        } while ((startPos < region_end) && (!match_from_start));

        return (results.size() > 0);
    }

    public boolean matchEntireString(final String s) throws MatcherException {

        if (!find(s, true))  // no find found
            return false;

        if (group(0, 0).equals(s)) // does the find equal the entire search string
            return true;
        else
            results.clear();
        return false;
    }

    public int start(int matchIndex, int groupIndex) throws MatcherException {
        check_size(matchIndex, groupIndex);
        int start = results.get(matchIndex)[groupIndex][0];
        return start;
    }

    public int end(int matchIndex, int groupIndex) throws MatcherException {
        check_size(matchIndex, groupIndex);
        int end = results.get(matchIndex)[groupIndex][1];
        return end;
    }

    public String group(int matchIndex, int groupIndex) throws MatcherException {
        // finds the groupIndex group in the match_string numbered matchIndex
        check_size(matchIndex, groupIndex);
        Integer start = results.get(matchIndex)[groupIndex][0];
        Integer end = results.get(matchIndex)[groupIndex][1];
        if (start == -1)
            return "";

        return search_string.toString().substring(start, end + 1);
    }

    public String group(int... groupIndex) throws MatcherException {
        int index = 0;
        if (groupIndex.length > 0) {
            index = groupIndex[0];
            check_size(0, groupIndex[0]);
        } else
            check_size(0, 0);
        return group(0, index);
    }

    public String group(CharSequence group_name) throws MatcherException {

        Integer group_index = group_names.get(group_name.toString());
        if (group_index == null)
            throw new MatcherException(
                "Group " + group_name + " does not exist!");

        return group(group_index);
    }

    public int groupCount(int... matchIndex) throws MatcherException {
        int index = 0;
        if (matchIndex.length > 0) {
            index = matchIndex[0];
            check_size(index, 0);
        } else
            check_size(0, 0);
        return results.get(index).length - 1; // -1 because group 0 is the entire find               
    }

    public int matchCount() {
        return results.size();
    }

    private boolean check_size(int matchIndex, int groupIndex) throws MatcherException {
        if (results.size() > matchIndex)
            if (results.get(matchIndex).length > groupIndex)
                return true;
            else
                throw new MatcherException(
                    "Match " + matchIndex + "contains less than "
                    + groupIndex + "groups");
        else
            throw new MatcherException(
                "There are less than " + matchIndex + "matches");

    }

    public CharSequence get_regex_string() {
        return transitions.getRegexString();
    }

    public String toString() {
        return transitions.toString();
    }
}

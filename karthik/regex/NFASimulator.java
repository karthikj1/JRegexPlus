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

import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */
class NFASimulator {

    protected Integer finish;
    protected TransitionTable original_start_table;
    protected TransitionTable transMatrix;
    protected CharSequence regex_string = "";
    protected Integer string_index; // global pointer that keeps track of our position in the search string

    protected String search_string;
    protected Integer region_start, region_end;

    protected Stack<Integer> boundary_stack = new Stack<>();
    protected Stack<PathToState> boundary_stack_objects = new Stack<>();
    protected ECloseCache eclose_cache;

    NFASimulator(TransitionTable start_nfa) {
        original_start_table = start_nfa;
    }

    void setRegexString(CharSequence regex) {
        regex_string = regex;
    }

    CharSequence getRegexString() {
        return regex_string;
    }

    private NFAStateObject init_simulator() {
        transMatrix = original_start_table;
        PathToStateList eclosed_start_states = new PathToStateList();
        eclose_cache = ECloseCache.create_eclose_cache(transMatrix);

        Integer start = original_start_table.getStart();
        eclosed_start_states.put(start, new PathToState());
        eclosed_start_states = eclose(eclosed_start_states);
        return new NFAStateObject(original_start_table, eclosed_start_states);

    }

    PathToState findOneMatch(CharSequence s, Integer start, Integer end) throws MatcherException {

        PathToState longest_success = null;
        NFAStateObject current_nfa;
        PathToStateList states;

        current_nfa = init_simulator();
        transMatrix = current_nfa.trans_table;
        states = current_nfa.states;
        finish = transMatrix.getFinish();

        search_string = s.toString();
        string_index = region_start = start;
        region_end = end;

        while ((string_index <= end) && (!states.isEmpty())) {

            // first check boundary if there are any boundary tokens
            states = boundary_close(states);
            states = process_quantifiers(states);
            longest_success = get_longest_success(states, longest_success);
            // now actually read the current character
            states = move(states);
            states = process_quantifiers(states);
            longest_success = get_longest_success(states, longest_success);

            string_index++;
        }

        // one final boundary_close in case there is a $ in the regex to match the end of the string 
        // original nfa is evaluated again for edge cases where the match starts on the last boundary 
        // before end of string. eg. (?<=foo) on an input string foo
        // capture successes if any
        states = boundary_close(states);
        longest_success = get_longest_success(states, longest_success);

        if (longest_success == null) {
            states = boundary_close(current_nfa.states);
            longest_success = get_longest_success(states, longest_success);
        }
        return longest_success;
    }

    private PathToState get_longest_success(PathToStateList states,
        PathToState longest_success) {
        /* checks if states contains any finish states and return the longer of
         * the finish state or the current longest success.
         * returns original longest_success if there is no finish state in the Map states
         */
        PathToState finish_state = states.get(finish);
        if (finish_state == null)
            return longest_success;

        // capture successes if any
        if (longest_success == null)
            return finish_state;

//        if (finish_state.resultStringLength() > longest_success.resultStringLength())
//            longest_success = finish_state;
        longest_success = longest_success.compare_finish_states(finish_state);

        return longest_success;
    }

    private PathToStateList move(final PathToStateList source_states) throws MatcherException {
        /* assumes source_states has already been e-closed
         moves to new state based on one character at index string_index from the string to match_string
         and returns a new set of states along with their related state objects
         */
        PathToStateList move_states = new PathToStateList();
        PathToState target_state_obj;
        Matchable match_token;

        for (Integer current_state : source_states.keySet())
            for (Integer target_state : transMatrix.get_all_transitions(
                current_state)) {
                match_token = transMatrix.getTransition(current_state,
                    target_state);

                if (!move_states.containsKey(target_state))
                    if ((!match_token.isBoundaryOrLookaround()) && match_token.
                        matches(search_string, string_index)) {
                        target_state_obj = new PathToState(source_states.get(
                            current_state));
                        target_state_obj.append(string_index, match_token.
                            getGroupID());
                        move_states.put(target_state, target_state_obj);
                    } // if transitions matches current character // if transitions != null
            } // for target_state // for current_state

        return move_states;
    }

    private PathToStateList boundary_close(
        final PathToStateList source_states)
        throws MatcherException {
        /* assumes source_states has already been e-closed
         moves to new state based on the boundary at index string_index from the string search_string
         and returns a new set of states along with their related state objects
         */
        PathToStateList move_states = new PathToStateList();
        PathToState current_state_obj;

        Matchable match_token;
        boolean found_boundary_token;
        Integer current_state;

        for (Integer stateID : source_states.keySet()) {
            /* take each initial_states in the provided initial states and
             push it and it's associated initial_states object on a stack
             */
            boundary_stack.push(stateID);
            boundary_stack_objects.push(source_states.get(stateID));
        }

        while (!boundary_stack.isEmpty()) {
            found_boundary_token = false;
            current_state = boundary_stack.pop();
            current_state_obj = boundary_stack_objects.pop();
            // cycle through every possible transition from current_state, looking for boundary transitions
            for (Integer target_state : transMatrix.get_all_transitions(
                current_state)) {
                match_token = transMatrix.getTransition(current_state,
                    target_state);

                if (match_token.isBoundaryOrLookaround()) {
                    found_boundary_token = true;
                    /* found a boundary token
                     * so make the transitions it produces
                     */
                    if ((!move_states.containsKey(target_state)) && (match_token.
                        matches(search_string, string_index))) {

                        // add transition produced by boundary token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(current_state_obj);

                    } // if match_token matches current character

                } // match token is boundary or lookaround 

            } // for target_state
            /* there were no transitions involving boundary tokens for this state
             * So put it back in the list of active states
             */
            if ((!found_boundary_token) && (!move_states.containsKey(
                current_state)))
                move_states.put(current_state, current_state_obj);

        } // while stack is not empty

        return move_states;
    }

    private PathToStateList process_quantifiers(
        final PathToStateList source_states)
        throws MatcherException {
        /* 
         moves to new state based on the boundary at index string_index from the string search_string
         and returns a new set of states along with their related state objects
         */
        PathToStateList move_states = new PathToStateList();
        PathToState current_state_obj;

        Matchable match_token;
        boolean retain_state, has_transitions;
        Integer current_state;

        for (Integer stateID : source_states.keySet()) {
            /* take each initial_states in the provided initial states and
             push it and it's associated initial_states object on a stack
             */
            boundary_stack.push(stateID);
            boundary_stack_objects.push(source_states.get(stateID));
        }

        while (!boundary_stack.isEmpty()) {
            retain_state = false;
            has_transitions = false;
            current_state = boundary_stack.pop();
            current_state_obj = boundary_stack_objects.pop();
            // cycle through every possible transition from current_state, looking for quantifier tokens
            for (Integer target_state : transMatrix.get_all_transitions(
                current_state)) {
                match_token = transMatrix.getTransition(current_state,
                    target_state);
                has_transitions = true;

                /* found a quantifier token, so process it 
                 * and make the transitions it produces
                 */
                if (!move_states.containsKey(target_state)) {

                    if (match_token.isQuantifier()) {
                        PathToState target_state_obj = new PathToState(
                            current_state_obj);
                        target_state_obj.processQuantifier(string_index,
                            (QuantifierToken) match_token);
                        // add transition produced by quantifier token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(target_state_obj);
                        continue;
                    }
                    if (match_token.isEpsilon()) {
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(current_state_obj);
                        continue;
                    }
                    // if we got here, match_token is not quantifier or epsilon
                    retain_state = true;
                } else if (!match_token.isQuantifier() && !match_token.
                    isEpsilon())
                    // if we got here, this token is for a normal transition or for a boundary transition
                    // which may match the *next* character or boundary, so we keep this state active       
                    retain_state = true;

            } // for target_state
            /* if there were transitions involving non-quantifier tokens for this state
             * or there were no transitions at all (i.e. the finish state)
             * So put it back in the list of active states
             */
            retain_state = retain_state | (!has_transitions);
            if ((retain_state) && (!move_states.containsKey(current_state)))
                move_states.put(current_state, current_state_obj);

        } // while stack is not empty

        return move_states;
    }

    private PathToStateList eclose(final PathToStateList current_states) {

        PathToStateList eclose_map = new PathToStateList();
        PathToState target_state_obj;
        Integer[] eps_transitions;

        for (Integer stateID : current_states.keySet()) {
            /* take each state in the provided initial states and
             put in the map that will be returned
             */
            eps_transitions = eclose_cache.get_eps_transitions(stateID);

            target_state_obj = current_states.get(stateID);
            eclose_map.put(stateID, target_state_obj);

            for (Integer target_state : eps_transitions)
                eclose_map.put(target_state, target_state_obj);
        }
        return eclose_map;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(original_start_table.toString());
        sb.append("Num States = ").append(original_start_table.getNumStates()).
            append("\r\n");
        sb.append("Start = ").append(original_start_table.getStart()).append(
            " finish = ");
        sb.append(original_start_table.getFinish());
        return sb.toString();
    } // toString()
}

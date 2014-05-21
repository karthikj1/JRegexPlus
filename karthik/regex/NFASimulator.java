/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    protected Stack<Path_to_State> boundary_stack_objects = new Stack<>();
    protected EClose_Cache eclose_cache;    
    
    NFASimulator(TransitionTable start_nfa){
        original_start_table = start_nfa;
    }
    
    void setRegexString(CharSequence regex){
        regex_string = regex;
    }

    CharSequence getRegexString(){
        return regex_string;
    }                
    
    private NFA_StateObject init_simulator(){
        transMatrix = original_start_table;
        Path_to_State_List eclosed_start_states = new Path_to_State_List(); 
        eclose_cache = EClose_Cache.create_eclose_cache(transMatrix);
        
        Integer start = original_start_table.getStart();
        eclosed_start_states.put(start, new Path_to_State());
        eclosed_start_states = eclose(eclosed_start_states);        
        return new NFA_StateObject(original_start_table, eclosed_start_states);
        
    }
   
    Path_to_State findOneMatch(CharSequence s, Integer start, Integer end) throws MatcherException
        {

        Path_to_State longest_success = null;
        NFA_StateObject current_nfa;
        Path_to_State_List states;

        current_nfa = init_simulator();
        transMatrix = current_nfa.trans_table;
        states = current_nfa.states;
        finish = transMatrix.getFinish();

        search_string = s.toString();
        string_index = region_start = start;
        region_end = end;

        while ((string_index < end) && (!states.isEmpty()))
            {

            // first check boundary if there are any boundary tokens
            states = boundary_close(states);
            longest_success = get_longest_success(states, longest_success);
            states = process_quantifiers(states);            
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
        
        if(longest_success == null){
            states = boundary_close(current_nfa.states);
            longest_success = get_longest_success(states, longest_success);
        }
        return longest_success;
        }

    private Path_to_State get_longest_success(Path_to_State_List states, Path_to_State longest_success)
        {
        /* checks if states contains any finish states and return the longer of
         * the finish state or the current longest success.
         * returns original longest_success if there is no finish state in the Map states
        */
        Path_to_State finish_state = states.get(finish);
        // capture successes if any
        if(finish_state != null) {            
            if(longest_success == null)
                longest_success = finish_state;
            else if(finish_state.resultStringLength() > longest_success.resultStringLength())
                longest_success = finish_state;
        }
        return longest_success;
        }
    

   private Path_to_State_List move(final Path_to_State_List source_states) throws MatcherException{
        /* assumes source_states has already been e-closed
           moves to new state based on one character at index string_index from the string to match_string
           and returns a new set of states along with their related state objects
        */
        Path_to_State_List move_states = new Path_to_State_List();
        Path_to_State target_state_obj;
        Matchable match_token;                                
        
        for (Integer current_state : source_states.keySet()) {
            for (Integer target_state : transMatrix.getKeySet(current_state)) {
                match_token = transMatrix.getTransition(current_state, target_state); 
                
                if (!move_states.containsKey(target_state)) {                      
                    if ((!match_token.isBoundaryOrLookaround()) && match_token.matches(search_string, string_index)) {
                        target_state_obj = new Path_to_State(source_states.get(current_state));
                        target_state_obj.append(string_index, match_token.getGroupID());
                        move_states.put(target_state, target_state_obj);
                    } // if transitions matches current character
                } // if transitions != null
            } // for target_state
        } // for current_state
        
        return move_states;
    }

    private Path_to_State_List boundary_close(final Path_to_State_List source_states) 
            throws MatcherException {
        /* assumes source_states has already been e-closed
           moves to new state based on the boundary at index string_index from the string search_string
           and returns a new set of states along with their related state objects
        */
        Path_to_State_List move_states = new Path_to_State_List();
        Path_to_State current_state_obj;
        
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
            for (Integer target_state : transMatrix.getKeySet(current_state)) {
                match_token = transMatrix.getTransition(current_state, target_state);

                if (match_token.isBoundaryOrLookaround()){
                    found_boundary_token = true;
                    /* found a boundary token
                     * so make the transitions it produces
                     */
                    if ((!move_states.containsKey(target_state)) && (match_token.matches(search_string, string_index))) {

                        // add transition produced by boundary token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(current_state_obj);

                    } // if match_token matches current character

                } // match token is boundary or lookaround 
                 
            } // for target_state
            /* there were no transitions involving boundary tokens for this state
             * So put it back in the list of active states
             */
            if ((!found_boundary_token) &&(!move_states.containsKey(current_state))) 
                move_states.put(current_state, current_state_obj);
            
        } // while stack is not empty

        return move_states;
    }    

    private Path_to_State_List process_quantifiers(final Path_to_State_List source_states) 
            throws MatcherException {
        /* 
           moves to new state based on the boundary at index string_index from the string search_string
           and returns a new set of states along with their related state objects
        */
        Path_to_State_List move_states = new Path_to_State_List();
        Path_to_State current_state_obj;
        
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
            for (Integer target_state : transMatrix.getKeySet(current_state)) {
                match_token = transMatrix.getTransition(current_state, target_state);
                has_transitions = true;
                
                    /* found a quantifier token, so process it 
                     * and make the transitions it produces
                     */
                if (!move_states.containsKey(target_state)) {
                        
                    if(match_token.isQuantifier()){
                        Path_to_State target_state_obj = new Path_to_State(current_state_obj);
                        target_state_obj.processQuantifier(string_index, (QuantifierToken) match_token);
                        // add transition produced by quantifier token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(target_state_obj);
                        continue;
                        }
                 if(match_token.isEpsilon()){
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(current_state_obj);
                        continue;
                     }       
                // if we got here, match_token is not quantifier or epsilon
                retain_state = true;
                }
                else
                    if(!match_token.isQuantifier() && !match_token.isEpsilon())
                        retain_state = true;
                    
            } // for target_state
            /* if there were transitions involving non-quantifier tokens for this state
             * or there were no transitions at all (i.e. the finish state)
             * So put it back in the list of active states
             */
            retain_state = retain_state | (!has_transitions);
            if ((retain_state) &&(!move_states.containsKey(current_state))) 
                move_states.put(current_state, current_state_obj);
            
        } // while stack is not empty

        return move_states;
    }    

    private Path_to_State_List eclose(final Path_to_State_List current_states) {        

        Path_to_State_List eclose_map = new Path_to_State_List();
        Path_to_State target_state_obj;
        Integer[] eps_transitions;
        
        for (Integer stateID : current_states.keySet()) {
            /* take each state in the provided initial states and
            put in the map that will be returned
            */
            eps_transitions = eclose_cache.get_eps_transitions(stateID);
            
            target_state_obj = current_states.get(stateID);
            eclose_map.put(stateID, target_state_obj);
            
            for(Integer target_state: eps_transitions)
               eclose_map.put(target_state, target_state_obj);            
        }
        return eclose_map;
    }
 
 
    
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(original_start_table.toString());
        sb.append("Num States = ").append(original_start_table.getNumStates()).append("\r\n");
        sb.append("Start = ").append(original_start_table.getStart()).append(" finish = ");
        sb.append(original_start_table.getFinish());
        return sb.toString();
    } // toString()
}

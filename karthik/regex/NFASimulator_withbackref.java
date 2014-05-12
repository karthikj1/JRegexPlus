/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.ArrayList;
import java.util.List;
import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */
class NFASimulator_withbackref {
    private int finish;
    private TransitionTable original_start_table;    
    private TransitionTable transMatrix;
    private CharSequence regex_string = "";
    
    private String search_string;       
    private Stack<NFA_StateObject> nfa_stack;
    
    NFASimulator_withbackref(TransitionTable start_nfa){
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
        
        Integer start = original_start_table.getStart();
        eclosed_start_states.put(start, new Path_to_State());
        eclosed_start_states = eclose(transMatrix, eclosed_start_states);        
        return new NFA_StateObject(original_start_table, eclosed_start_states);
        
    }
   
    Path_to_State findOneMatch(CharSequence s, int start, int end) throws MatcherException
    {
        
        Path_to_State longest_success = null;       
        NFA_StateObject current_nfa; 
        NFA_StateObject original_nfa;         
        Stack<NFA_StateObject> temp_stack = new Stack<>();
        nfa_stack = new Stack<>();
        
        original_nfa = init_simulator();
        Path_to_State_List states = new Path_to_State_List();
        nfa_stack.push(original_nfa);
        
        search_string = s.toString();
        int ctr = start;
        
        while (ctr < end)
            {                      
          while(!nfa_stack.isEmpty())
              {
                current_nfa = nfa_stack.pop();
                transMatrix = current_nfa.trans_table;
                states = current_nfa.states;
                finish = transMatrix.getFinish();        
                
                  // first check boundary if there are any boundary tokens
                  states = boundary_close(states, ctr);
                   longest_success = get_longest_success(states, longest_success); 
                // now actually read the current character
                states = move(states, ctr);
                longest_success = get_longest_success(states, longest_success);
                current_nfa.states = states;
                if(!states.isEmpty())
                    temp_stack.push(current_nfa);
                }
            ctr++;      
            while(!temp_stack.isEmpty())
                nfa_stack.push(temp_stack.pop());
            }

        // one final boundary_close in case there is a $ in the regex to match the end of the string 
        // original nfa is evaluated again for edge cases where the match starts on the last boundary 
        // before end of string. eg. (?<=foo) on an input string foo
        nfa_stack.push(init_simulator());
        
        while(!nfa_stack.isEmpty())
              {
                current_nfa = nfa_stack.pop();
                transMatrix = current_nfa.trans_table;
                states = current_nfa.states;
                finish = transMatrix.getFinish();        
          
            // capture successes if any
              states = boundary_close(states, ctr);  
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
        
        List<Path_to_State> finish_states = states.get(finish);
        
        // capture successes if any
            for (Path_to_State finish_state : finish_states)
                {
                    if (longest_success == null)
                        longest_success = finish_state;
                    else if (finish_state.resultStringLength() > longest_success.resultStringLength())
                        longest_success = finish_state;
                }
        return longest_success;
        }
    

   private Path_to_State_List move(final Path_to_State_List source_states, int pos) throws MatcherException{
        /* assumes source_states has already been e-closed
           moves to new state based on one character at index pos from the string to match_string
           and returns a new set of states along with their related state objects
        */
        Path_to_State_List move_states = new Path_to_State_List();
        Path_to_State target_state_obj;
        Matchable match_token;             
        
        if (source_states == null) {
            return null;
        }
        
        for (Integer current_state : source_states.keySet()) {
            for (Integer target_state : transMatrix.getKeySet(current_state)) {
                match_token = transMatrix.getTransition(current_state, target_state); 
                
                    // just comment out this IF block and its contents if backrefs blow everything up
                    if(match_token.isBackReference()){                        
                        for (Path_to_State current_state_obj : source_states.get(current_state))                            
                            process_backreference(current_state, target_state, 
                                    current_state_obj, match_token);                        
                    }  
                    if ((!match_token.isBoundaryOrLookaround()) && match_token.matches(search_string, pos)) {
                        
                        for (Path_to_State current_state_obj : source_states.get(current_state))
                            {
                            target_state_obj = new Path_to_State(current_state_obj);
                            target_state_obj.append(pos, match_token.getGroupID());
                            move_states.put(target_state, target_state_obj);
                            }
                    } // if transitions matches current character                
            } // for target_state
        } // for current_state
        
        return eclose(transMatrix, move_states);
    }

    private void process_backreference(Integer current_state, Integer target_state,
            final Path_to_State current_state_object, final Matchable match_token) throws MatcherException
        {

        String match_string;
        TransitionTable backref_string_trans_table = new TransitionTable(EpsClass.getEpsClass());
        BackReferenceRegexToken backref_token = (BackReferenceRegexToken) match_token;
        Path_to_State target_state_obj;
                
        Integer[] backref_indices = current_state_object.get_match_for_group(backref_token.getBackRefID());
            if (backref_indices[0] != -1)
                {
                match_string = search_string.substring(backref_indices[0], backref_indices[1] + 1);
                backref_string_trans_table = Pattern.get_trans_table(match_string, backref_token.getGroupID());
                }

            backref_string_trans_table = transMatrix.get_new_table_with_expanded_backref(backref_string_trans_table, current_state, target_state);
            target_state_obj = new Path_to_State(current_state_object);

            Path_to_State_List backref_states = new Path_to_State_List();
            backref_states.put(current_state, target_state_obj);
            backref_states = eclose(backref_string_trans_table, backref_states);

            NFA_StateObject new_NFA_and_state = new NFA_StateObject(backref_string_trans_table, backref_states);
            nfa_stack.push(new_NFA_and_state);
       }

    private Path_to_State_List boundary_close(final Path_to_State_List source_states, int pos) 
            throws MatcherException {
        /* assumes source_states has already been e-closed
           moves to new state based on the boundary at index pos from the string search_string
           and returns a new set of states along with their related state objects
        */
        Stack<Integer> boundary_stack = new Stack<>();
        Stack<Path_to_State> boundary_stack_objects = new Stack<>();
        Path_to_State_List move_states = new Path_to_State_List();
        Path_to_State current_state_obj;
        
        Matchable match_token;
        boolean found_boundary_token;
        Integer current_state;
        
        if (source_states == null) {
            return null;
        }
        
        for (Integer stateID : source_states.keySet()) {
            /* take each initial_states in the provided initial states and
             push it and it's associated initial_states object on a stack
             */
            for(Path_to_State source_state_obj:source_states.get(stateID)){
                boundary_stack.push(stateID);
                boundary_stack_objects.push(source_state_obj);
            }
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
                    if (match_token.matches(search_string, pos)) {

                        // add transition produced by boundary token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(new Path_to_State(current_state_obj));

                    } // if match_token matches current character

                } // if match_token != null

            } // for target_state
            /* there were no transitions involving boundary tokens for this state
             * So put it back in the list of active states
             */
            if ((!found_boundary_token)) 
                move_states.putUnique(current_state, current_state_obj);
            
        } // while stack is not empty

        return eclose(transMatrix, move_states);
    }    

    private Path_to_State_List eclose(TransitionTable transition_table, 
            final Path_to_State_List initial_states) {
        Stack<Integer> eclose_stack = new Stack<>();

        Path_to_State_List eclose_map = new Path_to_State_List();
        int current_state;
        Path_to_State target_state_obj;
        List<Path_to_State> current_stateobj_list;
        
        if (initial_states == null) {
            return null;
        }
        for (Integer stateID : initial_states.keySet()) {
            /* take each initial_states in the provided initial states and
            push it on a stack
            */
            eclose_stack.push(stateID);
            for(Path_to_State initial_state_obj : initial_states.get(stateID))
                eclose_map.put(stateID, initial_state_obj);
        }
        
        while (!eclose_stack.isEmpty()) {
            // pop the state ID and the state object associated with it
            current_state = eclose_stack.pop();
            
            for (int target_state: transition_table.getKeySet(current_state))                 
                    if (transition_table.getTransition(current_state, target_state).isEpsilon()) {
                        /* If we got here, target_state is reachable from current state via e-transition
                           if target_state has already been reached by another path
                           add it to the eclose_map anyway to preserve path information
                        */
                        eclose_stack.push(target_state);
                        /* make a clone of the state object so we don't end up with 
                          multiple references to the same object */
                         
                        // iterate over a newly created list below to prevent ConcurrentModificationException
                        // when we put the target_state back in eclose_map
                        current_stateobj_list = new ArrayList<>(eclose_map.get(current_state));
                        for(Path_to_State current_state_obj : current_stateobj_list){
                            target_state_obj = new Path_to_State(current_state_obj);
                        /* push the newly reached target state on the stack
                        so we can look for e-transitions from that state in the 
                        next iteration of the while loop
                        */
                            eclose_map.putUnique(target_state, target_state_obj);
                        /*
                         add the target state and it's associated state object
                         to the map that will be returned
                         */
                        }
                    }                           
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

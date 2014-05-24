/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.List;
import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */
class Enhanced_NFASimulator extends NFASimulator{ 
    
    // handled backreferences as well.
    // backref algorithm is a bit slower since it has to keep track of more paths
    // so this is only used if the regex actually contains a backreference
    // otherwise NFA_Simulator is used which runs a bit quicker
         
    private Stack<EnhancedNFA_StateObject> nfa_stack;
    
    Enhanced_NFASimulator(TransitionTable start_nfa){
        super(start_nfa);
    }
    
    private EnhancedNFA_StateObject init_simulator(){
        transMatrix = original_start_table;
        Enhanced_Path_to_State_List eclosed_start_states = new Enhanced_Path_to_State_List();  
        eclose_cache = EClose_Cache.create_eclose_cache(transMatrix);
        
        Integer start = original_start_table.getStart();
        eclosed_start_states.put(start, new Path_to_State());
        eclosed_start_states = eclose(eclosed_start_states, eclose_cache);        
        return new EnhancedNFA_StateObject(original_start_table, eclosed_start_states, eclose_cache);
        
    }
   
    Path_to_State findOneMatch(CharSequence s, Integer start, Integer end) throws MatcherException
    {
        
        Path_to_State longest_success = null;       
        EnhancedNFA_StateObject original_nfa;         
        Stack<EnhancedNFA_StateObject> temp_stack = new Stack<>();
        nfa_stack = new Stack<>();
        
        original_nfa = init_simulator();
        nfa_stack.push(original_nfa);
        
        search_string = s.toString();
        string_index = region_start = start;
        region_end = end;
        
        while (string_index <= end)
            {
            while (!nfa_stack.isEmpty())
                {
                longest_success = simulate_one_character(longest_success, temp_stack);
                }
            string_index++;
            while (!temp_stack.isEmpty())
                nfa_stack.push(temp_stack.pop());
            }

        // one final check in case there is a $ in the regex to match the end of the string 
        // original nfa is evaluated again for edge cases where the match starts on the last boundary 
        // before end of string. eg. (?<=foo) on an input string foo
        
        nfa_stack.push(init_simulator());
        
        while(!nfa_stack.isEmpty())
              {
                longest_success = simulate_one_character(longest_success, temp_stack);
              }       
        return longest_success;   
    }

    private Path_to_State simulate_one_character(Path_to_State longest_success, Stack<EnhancedNFA_StateObject> temp_stack)
            throws MatcherException
        {
        EnhancedNFA_StateObject current_nfa;
        Enhanced_Path_to_State_List states;
        
        current_nfa = nfa_stack.pop();
        transMatrix = current_nfa.trans_table;
        states = current_nfa.states;
        eclose_cache = current_nfa.eclose_cache;
        finish = transMatrix.getFinish();
        
        // first check boundary if there are any boundary tokens
        states = boundary_close(states);
        states = process_quantifiers(states, transMatrix);
        longest_success = get_longest_success(states, longest_success);
        
    // now actually read the current character
        states = move(states);
        states = process_quantifiers(states, transMatrix);
        longest_success = get_longest_success(states, longest_success);
    
        current_nfa.states = states;
        if (!states.isEmpty())
            temp_stack.push(current_nfa);
        
        return longest_success;
        }

    private Path_to_State get_longest_success(Enhanced_Path_to_State_List states, Path_to_State longest_success)
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
    

   private Enhanced_Path_to_State_List move(final Enhanced_Path_to_State_List source_states) throws MatcherException{
        /* assumes source_states has already been e-closed
           moves to new state based on one character at index string_index from the string to match_string
           and returns a new set of states along with their related state objects
        */
        Enhanced_Path_to_State_List move_states = new Enhanced_Path_to_State_List();
        Path_to_State target_state_obj;
        Matchable match_token;                     
        
        for (Integer current_state : source_states.keySet()) {
            for (Integer target_state : transMatrix.getKeySet(current_state)) {
                match_token = transMatrix.getTransition(current_state, target_state); 
                
                    // just comment out this IF block and its contents if backrefs blow everything up
                    if(match_token.isBackReference()){
                        BackRefRegexToken backref_token = (BackRefRegexToken) match_token;                            
                        if (backref_token.isBackref_start()){
                            for (Path_to_State current_state_obj : source_states.get(current_state))                                  
                                process_backreference_start(current_state, target_state, current_state_obj, backref_token);
                        }
                        else{
                            EndBackRefRegexToken end_backref_token = (EndBackRefRegexToken) match_token;
                            if(end_backref_token.get_match_pos() != string_index)
                                continue;
                            
                            TransitionTable new_table = transMatrix.get_table_with_backref_expansion_removed(end_backref_token);
                              // e-close backref_states with the correct eclose cache and then create new NFA object to push on stack 
                            EClose_Cache backref_table_eclose_cache = EClose_Cache.create_eclose_cache(new_table);        
                            int start_index = end_backref_token.getStartRow(); // assumes this is 0 for now
                            int end_index = end_backref_token.getEndRow();
                
                            int backref_num_states = end_index + 1 - start_index;

                            for (Path_to_State current_state_obj : source_states.get(current_state)){      
                                Integer[] match_path_end = current_state_obj.get_match_for_group(0);
                                if (match_path_end[1] != string_index - 1)
                                    continue;
                                
                                process_backreference_end(target_state - backref_num_states, backref_table_eclose_cache,
                                        new_table, current_state_obj);
                            }
                        }
                    }
                    /* if this method gets called after the end of the string, it is only relevant 
                       to find an endbackref token. So we can skip the processing for other types of tokens
                    */
                    if(string_index > search_string.length() - 1)
                        continue;
                    
                    if ((!match_token.isBoundaryOrLookaround()) && match_token.matches(search_string, string_index)) {
                        
                        for (Path_to_State current_state_obj : source_states.get(current_state))
                            {
                            target_state_obj = new Path_to_State(current_state_obj);
                            target_state_obj.append(string_index, match_token.getGroupID());
                            move_states.put(target_state, target_state_obj);
                            }
                    } // if transitions matches current character                
            } // for target_state
        } // for current_state
        
        return move_states;
    }
   
   private void process_backreference_end(final Integer target, EClose_Cache backref_table_eclose_cache,
           TransitionTable new_table, Path_to_State current_state_obj) throws MatcherException
        {
            
        Path_to_State target_state_obj = new Path_to_State(current_state_obj);
        Enhanced_Path_to_State_List backref_states = new Enhanced_Path_to_State_List();
                  
        backref_states.put(target, target_state_obj);
        // just the above line so that current_state is correct

        backref_states = process_quantifiers(backref_states, new_table);

        EnhancedNFA_StateObject new_NFA_and_state = new EnhancedNFA_StateObject(new_table, 
                backref_states, backref_table_eclose_cache);
        nfa_stack.push(new_NFA_and_state);        
        }

    private void process_backreference_start(final Integer current_state, final Integer target_state,
            final Path_to_State current_state_object, BackRefRegexToken backref_token) throws MatcherException
        {
         
        Path_to_State target_state_obj;

        Integer[] backref_indices = current_state_object.get_match_for_group(backref_token.getBackRefID());

        if (backref_indices[0] == -1)  // backref group was empty
            return;

        // simple check below to make sure there are enough characters left in the text to match the backreference
        // if not, we can avoid some work
        if ((backref_indices[1] - backref_indices[0]) > (region_end - string_index))
            return;
                
        int endPeek = string_index + backref_indices[1] - backref_indices[0];
        String match_string = search_string.substring(backref_indices[0], backref_indices[1] + 1);
        String peekahead = search_string.substring(string_index, endPeek + 1);
        
        // peek to see if back ref string is in the search string just ahead
        if(!peekahead.equals(match_string))
            return;
        
        TransitionTable backref_string_trans_table = TransitionTable.get_expanded_backref_table(match_string, backref_token.getGroupID(),
                transMatrix, target_state);
        
        target_state_obj = new Path_to_State(current_state_object);
        Enhanced_Path_to_State_List backref_states = new Enhanced_Path_to_State_List();

        // not the best solution in line below - need a better way for TransitionTable to signal where the states have been moved around           
        backref_states.put(0, target_state_obj);
        // just the above line so that current_state is correct after the table is expanded

        // e-close backref_states with the correct eclose cache and then create new NFA object to push on stack 
        EClose_Cache backref_table_eclose_cache = EClose_Cache.create_eclose_cache(backref_string_trans_table);        
        backref_states = eclose(backref_states, backref_table_eclose_cache);

        EnhancedNFA_StateObject new_NFA_and_state = new EnhancedNFA_StateObject(backref_string_trans_table, 
                backref_states, backref_table_eclose_cache);
        nfa_stack.push(new_NFA_and_state);        

        }

    private Enhanced_Path_to_State_List boundary_close(final Enhanced_Path_to_State_List source_states) 
            throws MatcherException {
        /* assumes source_states has already been e-closed
           moves to new state based on the boundary at index string_index from the string search_string
           and returns a new set of states along with their related state objects
        */
        Enhanced_Path_to_State_List move_states = new Enhanced_Path_to_State_List();
        Path_to_State current_state_obj;
        
        Matchable match_token;
        boolean found_boundary_token;
        Integer current_state;
        
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
                    if (match_token.matches(search_string, string_index)) {

                        // add transition produced by boundary token
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(current_state_obj);

                    } // if match_token matches current character

                } // if match_token != null

            } // for target_state
            /* there were no transitions involving boundary tokens for this state
             * So put it back in the list of active states
             */
            if ((!found_boundary_token)) 
                move_states.putUnique(current_state, current_state_obj);
            
        } // while stack is not empty

        return move_states;
    }    

       private Enhanced_Path_to_State_List process_quantifiers(final Enhanced_Path_to_State_List source_states
               , TransitionTable trans_table) 
            throws MatcherException {
        /* 
           moves to new state based on the quantifier tokens, if any
           and returns a new set of states along with their related state objects
        */
        Enhanced_Path_to_State_List move_states = new Enhanced_Path_to_State_List();
        Path_to_State current_state_obj;
        
        Matchable match_token;
        boolean retain_state, has_transitions;
        Integer current_state;
        
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
            retain_state = false;
            has_transitions = false;
            current_state = boundary_stack.pop();
            current_state_obj = boundary_stack_objects.pop();
            // cycle through every possible transition from current_state, looking for quantifier tokens
            for (Integer target_state : trans_table.getKeySet(current_state)) {
                match_token = trans_table.getTransition(current_state, target_state);
                has_transitions = true;
                
                if (match_token.isQuantifier()){                    
                    /* found a quantifier token so process it
                     * so make the transitions it produces
                     */
                        Path_to_State target_state_obj = new Path_to_State(current_state_obj);
                        target_state_obj.processQuantifier(string_index, (QuantifierToken) match_token);
                       
                        // add transition produced by quantifier token
                        
                        boundary_stack.push(target_state);
                        boundary_stack_objects.push(target_state_obj);
                        continue;

                } // if match_token is a quantifier
                
                if (match_token.isEpsilon())
                    {
                    boundary_stack.push(target_state);
                    boundary_stack_objects.push(current_state_obj);
                    continue;
                    }        
                // if we got here, match_token is not quantifier or epsilon
                retain_state = true;
            } // for target_state
            /* if there were transitions involving non-quantifier tokens for this state
             * or there were no transitions at all (i.e. the finish state)
             * put it back in the list of active states
             */
            retain_state = retain_state | (!has_transitions);
            if (retain_state) 
                move_states.putUnique(current_state, current_state_obj);
            
        } // while stack is not empty

        return move_states;
    }    

       
    private Enhanced_Path_to_State_List eclose(final Enhanced_Path_to_State_List current_states, EClose_Cache cache) {        

        Enhanced_Path_to_State_List eclose_map = new Enhanced_Path_to_State_List();        
        List<Path_to_State> current_stateobj_list;
        Integer[] eps_transitions;
        
        for (Integer stateID : current_states.keySet()) {
            /* take each current_states in the provided initial states and
            push it on a stack
            */
            eps_transitions = cache.get_eps_transitions(stateID);
            
            for(Path_to_State initial_state_obj : current_states.get(stateID))
                eclose_map.put(stateID, initial_state_obj);
            
             current_stateobj_list = current_states.get(stateID);
           
             for(Integer target_state: eps_transitions)
                 for(Path_to_State target_state_obj:current_stateobj_list)
                   eclose_map.putUnique(target_state, target_state_obj);            
             
        }
      return eclose_map;
    }
 }

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.HashMap;
import java.util.Map;
import karthik.regex.dataStructures.Stack;

/**
 *
 * @author karthik
 */
class EClose_Cache
    {
    private Map<Integer, Integer[]> eclose_cache;
    
    private EClose_Cache(Map<Integer, Integer[]> cache){
        eclose_cache = cache;
    }
    
    Integer[] get_eps_transitions(Integer source_state)
        {
        Integer[] return_array = eclose_cache.get(source_state);

        return (return_array == null) ? new Integer[0] : return_array;
        }

    static EClose_Cache create_eclose_cache(TransitionTable transition_table)
        {
        // e-closes every state in the transition matrix 

        Integer[] eclose_array_for_one_state;
        Map<Integer, Integer[]> return_map = new HashMap<>();

        Integer numStates = transition_table.getNumStates();
        for (Integer source_state = 0; source_state < numStates; source_state++)
            {
            eclose_array_for_one_state = calc_eclose_states(transition_table, source_state);
            return_map.put(source_state, eclose_array_for_one_state);
            }

        return new EClose_Cache(return_map);
        }

    private static Integer[] calc_eclose_states(TransitionTable transition_table,
            Integer current_state)
        {

        Map<Integer, Integer> eclose_map = new HashMap<>();
        Stack<Integer> tempStack = new Stack<>();

        tempStack.push(current_state);
        eclose_map.put(current_state, current_state);

        while (!tempStack.isEmpty())
            {
            // pop the state ID and the state object associated with it
            current_state = tempStack.pop();

            for (Integer target_state : transition_table.get_all_transitions(current_state))
                if ((transition_table.getTransition(current_state, target_state).isEpsilon())
                        && (!eclose_map.containsKey(target_state)))
                    {
                    /* If we got here, target_state is reachable from current state via e-transition
                     if target_state has already been reached, no need to add it to the eclose_map again
                     */

                    tempStack.push(target_state);
                    /* push the newly reached target state on the stack
                     so we can look for e-transitions from that state in the 
                     next iteration of the while loop
                        
                     */
                    eclose_map.put(target_state, target_state);
                    }
            }
        return eclose_map.keySet().toArray(new Integer[eclose_map.size()]);
        }

    }

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author karthik
 */
class Path_to_State_List
    {
    Map<Integer, Path_to_State> states;

    Path_to_State_List()
        {
            states = new HashMap<>();
        }
    
    boolean containsKey(Integer key){
        return states.containsKey(key);
    } 
    
    Path_to_State get(Integer key){
        return states.get(key);
    }
    
    Path_to_State put(Integer key, Path_to_State stateObj){
        return states.put(key, stateObj);
    }
    
    boolean isEmpty(){
        return states.isEmpty();
    }
    
    Set<Integer> keySet(){
        return states.keySet();
    }
    }

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
class Enhanced_Path_to_State_List
    {
    Map<Integer, List<Path_to_State>> states;

    Enhanced_Path_to_State_List()
        {
            states = new HashMap<>();
        }
    
    boolean containsKey(Integer key){
        return states.containsKey(key);
    } 
    
    List<Path_to_State> get(Integer key){
        // returns list of state objects corresponding to given key
        // or returns an empty list to avoid returning null
        
        if(states.containsKey(key))
            return states.get(key);
        else
            return new ArrayList<>();
    }
    
    Path_to_State put(Integer key, Path_to_State stateObj)
        {
        if (states.containsKey(key))
            states.get(key).add(stateObj);
        else
            {
            List<Path_to_State> newList = new ArrayList<>();
            newList.add(stateObj);
            states.put(key, newList);            
            }
        return stateObj;
        }
    
      void putUnique(Integer key, Path_to_State stateObj)
        // adds key only if stateObj is not already in the map for the given key
        {
        if (states.containsKey(key)){
            boolean isUnique = true;
            List<Path_to_State> values = states.get(key);
            for(Path_to_State value_obj: values)
                if(value_obj.equals(stateObj)){
                    isUnique = false;
                    break;
                }
            
            if(isUnique)
                values.add(stateObj);
        }
        else
            {
            List<Path_to_State> newList = new ArrayList<>();
            newList.add(stateObj);
            states.put(key, newList);
            }
        }
      
    boolean isEmpty(){
        return states.isEmpty();
    }
    
    Set<Integer> keySet(){
        return states.keySet();
    }
    }

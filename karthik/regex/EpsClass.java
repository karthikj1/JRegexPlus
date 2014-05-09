/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author karthik
 */
class EpsClass implements Matchable
    {

    public boolean isEpsilon()
        {
        return true;
        }

    public boolean isBoundaryOrLookaround()
        {
        return false;
        }

    public boolean isBackReference()
        {
        return false;
        }

    public boolean matches(final CharSequence s, int pos)
        {
        return false;
        }

    public List<Integer> getGroupID()
        {
        return new ArrayList<Integer>(); // returns empty list
        }

    public String toString()
        {
        return "EPSILON";
        }

    }

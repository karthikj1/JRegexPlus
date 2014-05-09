/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

/**
 *
 * @author karthik
 */
public class MatcherException extends Exception{
        public MatcherException()
    {
        super();
    }
    
    public MatcherException(String msg)
    {
        super(msg);
    }

    public MatcherException(String msg, Throwable t)
    {
        super(msg, t);
    }    

}

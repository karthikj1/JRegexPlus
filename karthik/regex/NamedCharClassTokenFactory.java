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

/**
 *
 * @author karthik
 */
class NamedCharClassTokenFactory
// creates character class regex token for named POSIX classes
    {
    
    static CharClassRegexToken getCharClassToken(final String class_name, final boolean isNegated, int flags) 
        throws TokenizerException{
       StringBuffer sb = new StringBuffer("");
       
        if(class_name.equals("Lower"))
            return new CharClassRegexToken(getLowerCase().toString(), isNegated, flags);
        
        if(class_name.equals("Upper"))
            return new CharClassRegexToken(getUpperCase().toString(), isNegated, flags);
     
        if(class_name.equals("Digit"))
            return new CharClassRegexToken(getDigit().toString(), isNegated, flags);
        
        if(class_name.equals("XDigit"))
            return new CharClassRegexToken(getHexDigits().toString(), isNegated, flags);

        if(class_name.equals("Punct"))
            return new CharClassRegexToken(getPunct().toString(), isNegated, flags);
        
        if(class_name.equals("Alpha")){
            return new CharClassRegexToken(getAlpha().toString(), isNegated, flags);
        }
        
        if(class_name.equals("Cntrl")){
            return new CharClassRegexToken(getCntrlChars().toString(), isNegated, flags);
        }

        if(class_name.equals("Alnum")){
            sb.append(getAlpha());
            sb.append(getDigit());
            return new CharClassRegexToken(sb.toString(), isNegated, flags);
        }
        
        if(class_name.equals("Graph")){
            sb.append(getAlpha());
            sb.append(getPunct());
            sb.append(getDigit());
            return new CharClassRegexToken(sb.toString(), isNegated, flags);
        }        
        
        if(class_name.equals("Print")){
            sb.append(getAlpha());
            sb.append(getPunct());
            sb.append(getDigit());
            sb.append(' ');
            return new CharClassRegexToken(sb.toString(), isNegated, flags);
        }
        
        if(class_name.equals("Blank")){
            sb.append(" \\t");           
            return new CharClassRegexToken(sb.toString(), isNegated, flags);
        }
        
        if(class_name.equals("Space")){
            sb.append(" \\t\\n\\x0B\\f\\r");           
            return new CharClassRegexToken(sb.toString(), isNegated, flags);
        }
        
        throw new TokenizerException("Unknown POSIX class name " + class_name
            + " after \\p");
        
    }
    
    private static CharSequence getLowerCase(){
        StringBuffer sb = new StringBuffer("");
        for(int c = 'a'; c <= 'z'; c++){
            sb.append((char) c);
         }
        return sb;
    }
    
    private static CharSequence getUpperCase(){
    
    StringBuffer sb = new StringBuffer("");
        for(int c = 'A'; c <= 'Z'; c++){
            sb.append((char) c);
        }
        return sb;
    }
   
      private static CharSequence getHexDigits(){
        StringBuffer sb = new StringBuffer("");
        for(int c = 'a'; c <= 'f'; c++){
            sb.append((char) c);
            sb.append(Character.toUpperCase((char) c));
         }
        sb.append(getDigit());
        return sb;
    }
     
    private static CharSequence getCntrlChars(){
    
    StringBuffer sb = new StringBuffer("");
        for(int c = 0x00; c <= 0x1f; c++){
            sb.append((char) c);
        }
        sb.append("\\x7f");
        return sb;
    }
    
    private static CharSequence getAlpha()
        {
        StringBuffer sb = new StringBuffer(getLowerCase());
        sb.append(getUpperCase());
        return sb;
        }
    
    private static CharSequence getDigit(){
    
    StringBuffer sb = new StringBuffer("");
        for(int c = '0'; c <= '9'; c++){
            sb.append((char) c);
        }
        return sb;
    }
    
    private static CharSequence getPunct(){
        return "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    }
    
    }


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package markup;

/**
 *
 * @author User
 */
public abstract class Element {
    
    public abstract void toMarkdown(StringBuilder sb);
    
    public abstract void toHtml(StringBuilder sb);
    
}

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
public class Text extends Element {

    String s;

    public Text(String s)
    {
        this.s = s;
    }

    @Override
    public void toMarkdown(StringBuilder sb)
    {
        sb.append(s);
    }

    @Override
    public void toHtml(StringBuilder sb)
    {
        sb.append(s);
    }
    
}

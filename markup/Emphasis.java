/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package markup;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author User
 */
public class Emphasis extends MarkElement {

    public Emphasis(List<Element> list)
    {
        this.list = list;
    }

    @Override
    public void toMarkdown(StringBuilder sb)
    {
        sb.append("*");

        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            Element el = (Element)iter.next();
            el.toMarkdown(sb);
        }

        sb.append("*");
    }

    @Override
    public void toHtml(StringBuilder sb)
    {
        sb.append("<em>");

        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            Element el = (Element)iter.next();
            el.toHtml(sb);
        }

        sb.append("</em>");
    }
    
}

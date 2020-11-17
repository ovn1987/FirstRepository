/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package markup;

import java.util.List;
import java.util.Iterator;


/**
 *
 * @author User
 */
public class Paragraph extends MarkElement {

    public Paragraph(List list1)
    {
        list = list1;
    }

    @Override
    public void toMarkdown(StringBuilder sb)
    {
        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            Element el = (Element)iter.next();
            el.toMarkdown(sb);
        }
    }

    @Override
    public void toHtml(StringBuilder sb)
    {
        Iterator iter = list.iterator();
        while(iter.hasNext())
        {
            Element el = (Element)iter.next();
            el.toHtml(sb);
        }
    }
    
}

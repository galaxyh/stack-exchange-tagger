
package org.h2t2.setagger.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlTagHandler {

    private static final Pattern htmlTagPattern = Pattern.compile("</?[a-z][a-z0-9]*[^<>]*>");

    /**
     * @param content
     *            The content with HTML tags.
     * @return A String that removes all HTML tags from content.
     *            The removed HTML tags are replaced with a white space.
     * @author TL
     */
    protected static String removeHtmlTags(String content) {
        StringBuilder result = new StringBuilder();
        String temp;
        int start = 0, end;
        Matcher matcher = htmlTagPattern.matcher(content);
        while (matcher.find()) {
            end = matcher.start();
            temp = content.substring(start, end);
            if (temp.length() > 0) {
                result.append(temp).append(" ");
            }
            start = matcher.end();
        }
        temp = content.substring(start);
        if (temp.length() > 0) {
            result.append(temp).append(" ");
        }
        return result.toString();
    }
}

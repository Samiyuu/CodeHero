package com.ccat.coderhero;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Colours Java keywords, strings, numbers and comments; the blank "____" is shown in white. */
public final class CodeHighlighter {
    private CodeHighlighter() {}

    // 1 string, 2 blank, 3 comment, 4 word, 5 number
    private static final Pattern TOKEN = Pattern.compile(
            "(\"(?:\\\\.|[^\"\\\\\\n])*\")|(____)|(//[^\\n]*)|([A-Za-z_][A-Za-z0-9_]*)|(\\b\\d+(?:\\.\\d+)?\\b)");

    public static CharSequence format(Context c, String code) {
        Set<String> keywords = new HashSet<>(Arrays.asList(c.getResources().getStringArray(R.array.code_keywords)));
        int kw = c.getColor(R.color.code_keyword), str = c.getColor(R.color.code_string),
                num = c.getColor(R.color.code_number), com = c.getColor(R.color.code_comment),
                blank = c.getColor(R.color.code_blank);
        SpannableString out = new SpannableString(code);
        Matcher m = TOKEN.matcher(code);
        while (m.find()) {
            int color = 0;
            boolean bold = false;
            if (m.group(1) != null) color = str;
            else if (m.group(2) != null) { color = blank; bold = true; }
            else if (m.group(3) != null) color = com;
            else if (m.group(4) != null) { if (keywords.contains(m.group(4))) { color = kw; bold = true; } }
            else if (m.group(5) != null) color = num;
            if (color != 0) {
                out.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (bold) out.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return out;
    }
}

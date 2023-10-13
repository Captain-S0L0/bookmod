package com.terriblefriends.bookmod;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.nbt.*;

import java.util.Stack;
import java.util.regex.Pattern;

public class JsonNbtTools {
    private static final Pattern INT_ARRAY_MATCHER = Pattern.compile("\\[[-+\\d|,\\s]+]");

    public static NbtCompound getTagFromJson(String jsonString) throws NbtException
    {
        jsonString = jsonString.trim();

        if (!jsonString.startsWith("{"))
        {
            throw new NbtException("Invalid tag encountered, expected \'{\' as first char.");
        }
        else if (topTagsCount(jsonString) != 1)
        {
            throw new NbtException("Encountered multiple top tags, only one expected");
        }
        else
        {
            return (NbtCompound)nameValueToNBT("tag", jsonString).parse();
        }
    }

    static int topTagsCount(String str) throws NbtException
    {
        int i = 0;
        boolean flag = false;
        Stack<Character> stack = new Stack<>();

        for (int j = 0; j < str.length(); ++j)
        {
            char c0 = str.charAt(j);

            if (c0 == 34)
            {
                if (isCharEscaped(str, j))
                {
                    if (!flag)
                    {
                        throw new NbtException("Illegal use of \\\": " + str);
                    }
                }
                else
                {
                    flag = !flag;
                }
            }
            else if (!flag)
            {
                if (c0 != 123 && c0 != 91)
                {
                    if (c0 == 125 && (stack.isEmpty() || stack.pop() != 123))
                    {
                        throw new NbtException("Unbalanced curly brackets {}: " + str);
                    }

                    if (c0 == 93 && (stack.isEmpty() || stack.pop() != 91))
                    {
                        throw new NbtException("Unbalanced square brackets []: " + str);
                    }
                }
                else
                {
                    if (stack.isEmpty())
                    {
                        ++i;
                    }

                    stack.push(c0);
                }
            }
        }

        if (flag)
        {
            throw new NbtException("Unbalanced quotation: " + str);
        }
        else if (!stack.isEmpty())
        {
            throw new NbtException("Unbalanced brackets: " + str);
        }
        else
        {
            if (i == 0 && !str.isEmpty())
            {
                i = 1;
            }

            return i;
        }
    }

    static JsonNbtTools.Any joinStrToNBT(String... args) throws NbtException
    {
        return nameValueToNBT(args[0], args[1]);
    }

    static JsonNbtTools.Any nameValueToNBT(String key, String value) throws NbtException
    {
        value = value.trim();

        if (value.startsWith("{"))
        {
            value = value.substring(1, value.length() - 1);
            JsonNbtTools.Compound JsonNbtTools$compound;
            String s1;

            for (JsonNbtTools$compound = new JsonNbtTools.Compound(key); value.length() > 0; value = value.substring(s1.length() + 1))
            {
                s1 = nextNameValuePair(value, true);

                if (s1.length() > 0)
                {
                    JsonNbtTools$compound.tagList.add(getTagFromNameValue(s1, false));
                }

                if (value.length() < s1.length() + 1)
                {
                    break;
                }

                char c1 = value.charAt(s1.length());

                if (c1 != 44 && c1 != 123 && c1 != 125 && c1 != 91 && c1 != 93)
                {
                    throw new NbtException("Unexpected token '" + c1 + "' at: " + value.substring(s1.length()));
                }
            }

            return JsonNbtTools$compound;
        }
        else if (value.startsWith("[") && !INT_ARRAY_MATCHER.matcher(value).matches())
        {
            value = value.substring(1, value.length() - 1);
            JsonNbtTools.List JsonNbtTools$list;
            String s;

            for (JsonNbtTools$list = new JsonNbtTools.List(key); value.length() > 0; value = value.substring(s.length() + 1))
            {
                s = nextNameValuePair(value, false);

                if (s.length() > 0)
                {
                    JsonNbtTools$list.tagList.add(getTagFromNameValue(s, true));
                }

                if (value.length() < s.length() + 1)
                {
                    break;
                }

                char c0 = value.charAt(s.length());

                if (c0 != 44 && c0 != 123 && c0 != 125 && c0 != 91 && c0 != 93)
                {
                    throw new NbtException("Unexpected token '" + c0 + "' at: " + value.substring(s.length()));
                }
            }

            return JsonNbtTools$list;
        }
        else
        {
            return new JsonNbtTools.Primitive(key, value);
        }
    }

    private static JsonNbtTools.Any getTagFromNameValue(String str, boolean isArray) throws NbtException
    {
        String s = locateName(str, isArray);
        String s1 = locateValue(str, isArray);
        return joinStrToNBT(s, s1);
    }

    private static String nextNameValuePair(String str, boolean isCompound) throws NbtException
    {
        int i = getNextCharIndex(str, ':');
        int j = getNextCharIndex(str, ',');

        if (isCompound)
        {
            if (i == -1)
            {
                throw new NbtException("Unable to locate name/value separator for string: " + str);
            }

            if (j != -1 && j < i)
            {
                throw new NbtException("Name error at: " + str);
            }
        }
        else if (i == -1 || i > j)
        {
            i = -1;
        }

        return locateValueAt(str, i);
    }

    private static String locateValueAt(String str, int index) throws NbtException
    {
        Stack<Character> stack = new Stack<>();
        int i = index + 1;
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;

        for (int j = 0; i < str.length(); ++i)
        {
            char c0 = str.charAt(i);

            if (c0 == 34)
            {
                if (isCharEscaped(str, i))
                {
                    if (!flag)
                    {
                        throw new NbtException("Illegal use of \\\": " + str);
                    }
                }
                else
                {
                    flag = !flag;

                    if (flag && !flag2)
                    {
                        flag1 = true;
                    }

                    if (!flag)
                    {
                        j = i;
                    }
                }
            }
            else if (!flag)
            {
                if (c0 != 123 && c0 != 91)
                {
                    if (c0 == 125 && (stack.isEmpty() || stack.pop() != 123))
                    {
                        throw new NbtException("Unbalanced curly brackets {}: " + str);
                    }

                    if (c0 == 93 && (stack.isEmpty() || stack.pop() != 91))
                    {
                        throw new NbtException("Unbalanced square brackets []: " + str);
                    }

                    if (c0 == 44 && stack.isEmpty())
                    {
                        return str.substring(0, i);
                    }
                }
                else
                {
                    stack.push(c0);
                }
            }

            if (!Character.isWhitespace(c0))
            {
                if (!flag && flag1 && j != i)
                {
                    return str.substring(0, j + 1);
                }

                flag2 = true;
            }
        }

        return str.substring(0, i);
    }

    private static String locateName(String str, boolean isArray) throws NbtException
    {
        if (isArray)
        {
            str = str.trim();

            if (str.startsWith("{") || str.startsWith("["))
            {
                return "";
            }
        }

        int i = getNextCharIndex(str, ':');

        if (i == -1)
        {
            if (isArray)
            {
                return "";
            }
            else
            {
                throw new NbtException("Unable to locate name/value separator for string: " + str);
            }
        }
        else
        {
            return str.substring(0, i).trim();
        }
    }

    private static String locateValue(String str, boolean isArray) throws NbtException
    {
        if (isArray)
        {
            str = str.trim();

            if (str.startsWith("{") || str.startsWith("["))
            {
                return str;
            }
        }

        int i = getNextCharIndex(str, ':');

        if (i == -1)
        {
            if (isArray)
            {
                return str;
            }
            else
            {
                throw new NbtException("Unable to locate name/value separator for string: " + str);
            }
        }
        else
        {
            return str.substring(i + 1).trim();
        }
    }

    private static int getNextCharIndex(String str, char targetChar)
    {
        int i = 0;

        for (boolean flag = true; i < str.length(); ++i)
        {
            char c0 = str.charAt(i);

            if (c0 == 34)
            {
                if (!isCharEscaped(str, i))
                {
                    flag = !flag;
                }
            }
            else if (flag)
            {
                if (c0 == targetChar)
                {
                    return i;
                }

                if (c0 == 123 || c0 == 91)
                {
                    return -1;
                }
            }
        }

        return -1;
    }

    private static boolean isCharEscaped(String str, int index)
    {
        return index > 0 && str.charAt(index - 1) == 92 && !isCharEscaped(str, index - 1);
    }

    abstract static class Any
    {
        protected String json;

        public abstract NbtElement parse() throws NbtException;
    }

    static class Compound extends JsonNbtTools.Any
    {
        protected java.util.List<JsonNbtTools.Any> tagList = Lists.newArrayList();

        public Compound(String jsonIn)
        {
            this.json = jsonIn;
        }

        public NbtElement parse() throws NbtException
        {
            NbtCompound NbtCompound = new NbtCompound();

            for (JsonNbtTools.Any JsonNbtTools$any : this.tagList)
            {
                NbtCompound.put(JsonNbtTools$any.json, JsonNbtTools$any.parse());
            }

            return NbtCompound;
        }
    }

    static class List extends JsonNbtTools.Any
    {
        protected java.util.List<JsonNbtTools.Any> tagList = Lists.newArrayList();

        public List(String json)
        {
            this.json = json;
        }

        public NbtElement parse() throws NbtException
        {
            NbtList nbttaglist = new NbtList();

            for (JsonNbtTools.Any JsonNbtTools$any : this.tagList)
            {
                nbttaglist.method_1217(JsonNbtTools$any.parse());
            }

            return nbttaglist;
        }
    }

    static class Primitive extends JsonNbtTools.Any
    {
        private static final Pattern DOUBLE = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[d|D]");
        private static final Pattern FLOAT = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[f|F]");
        private static final Pattern BYTE = Pattern.compile("[-+]?[0-9]+[b|B]");
        private static final Pattern LONG = Pattern.compile("[-+]?[0-9]+[l|L]");
        private static final Pattern SHORT = Pattern.compile("[-+]?[0-9]+[s|S]");
        private static final Pattern INTEGER = Pattern.compile("[-+]?[0-9]+");
        private static final Pattern DOUBLE_UNTYPED = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings();
        protected String jsonValue;

        public Primitive(String jsonIn, String valueIn)
        {
            this.json = jsonIn;
            this.jsonValue = valueIn;
        }

        public NbtElement parse()
        {
            try
            {
                if (DOUBLE.matcher(this.jsonValue).matches())
                {
                    return new NbtDouble("", Double.parseDouble(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if (FLOAT.matcher(this.jsonValue).matches())
                {
                    return new NbtFloat("", Float.parseFloat(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if (BYTE.matcher(this.jsonValue).matches())
                {
                    return new NbtByte("", Byte.parseByte(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if (LONG.matcher(this.jsonValue).matches())
                {
                    return new NbtLong("", Long.parseLong(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if (SHORT.matcher(this.jsonValue).matches())
                {
                    return new NbtShort("", Short.parseShort(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
                }

                if (INTEGER.matcher(this.jsonValue).matches())
                {
                    return new NbtInt("", Integer.parseInt(this.jsonValue));
                }

                if (DOUBLE_UNTYPED.matcher(this.jsonValue).matches())
                {
                    return new NbtDouble("", Double.parseDouble(this.jsonValue));
                }

                if ("true".equalsIgnoreCase(this.jsonValue) || "false".equalsIgnoreCase(this.jsonValue))
                {
                    return new NbtByte("", (byte)(Boolean.parseBoolean(this.jsonValue) ? 1 : 0));
                }
            }
            catch (NumberFormatException var6)
            {
                this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
                return new NbtString(null, this.jsonValue);
            }

            if (this.jsonValue.startsWith("[") && this.jsonValue.endsWith("]"))
            {
                String s = this.jsonValue.substring(1, this.jsonValue.length() - 1);
                String[] astring = Iterables.toArray(SPLITTER.split(s), String.class);

                try
                {
                    int[] aint = new int[astring.length];

                    for (int j = 0; j < astring.length; ++j)
                    {
                        aint[j] = Integer.parseInt(astring[j].trim());
                    }

                    return new NbtIntArray("", aint);
                }
                catch (NumberFormatException var5)
                {
                    return new NbtString(null, this.jsonValue);
                }
            }
            else
            {
                if (this.jsonValue.startsWith("\"") && this.jsonValue.endsWith("\""))
                {
                    this.jsonValue = this.jsonValue.substring(1, this.jsonValue.length() - 1);
                }

                this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
                StringBuilder stringbuilder = new StringBuilder();

                for (int i = 0; i < this.jsonValue.length(); ++i)
                {
                    if (i < this.jsonValue.length() - 1 && this.jsonValue.charAt(i) == 92 && this.jsonValue.charAt(i + 1) == 92)
                    {
                        stringbuilder.append('\\');
                        ++i;
                    }
                    else
                    {
                        stringbuilder.append(this.jsonValue.charAt(i));
                    }
                }

                return new NbtString(null, stringbuilder.toString());
            }
        }
    }
}

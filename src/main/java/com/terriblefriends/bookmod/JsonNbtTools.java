package com.terriblefriends.bookmod;

import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsonNbtTools
{
    private static final Pattern PATTERN_UNTAGGED_DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FLOAT = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_BYTE = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_LONG = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)l", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_SHORT = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)s", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_INT = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)");
    private final String inputJson;
    private int readIndex;

    public static NbtCompound getTagFromJson(String jsonString) throws NbtException
    {
        return new JsonNbtTools(jsonString).parseCompoundFromJson();
    }

    private NbtCompound parseCompoundFromJson() throws NbtException
    {
        NbtCompound compound = this.parseCompound();
        this.consumeWhitespace();

        if (this.canReadNext())
        {
            ++this.readIndex;
            throw this.createException("Trailing data found");
        }
        else
        {
            return compound;
        }
    }

    private JsonNbtTools(String input)
    {
        this.inputJson = input;
    }

    private String readTagKey() throws NbtException
    {
        this.consumeWhitespace();

        if (!this.canReadNext())
        {
            throw this.createException("Expected key");
        }
        else
        {
            return this.peek() == '"' ? this.readStringTag() : this.readWord();
        }
    }

    private NbtException createException(String p_193602_1_)
    {
        return new NbtException(p_193602_1_, this.inputJson, this.readIndex);
    }

    private NbtElement parseNonList() throws NbtException
    {
        this.consumeWhitespace();

        if (this.peek() == '"')
        {
            return new NbtString("", this.readStringTag());
        }
        else
        {
            String s = this.readWord();

            if (s.isEmpty())
            {
                throw this.createException("Expected value");
            }
            else
            {
                return this.parseElement(s);
            }
        }
    }

    private NbtElement parseElement(String p_193596_1_)
    {
        try
        {
            if (PATTERN_FLOAT.matcher(p_193596_1_).matches())
            {
                return new NbtFloat("", Float.parseFloat(p_193596_1_.substring(0, p_193596_1_.length() - 1)));
            }

            if (PATTERN_BYTE.matcher(p_193596_1_).matches())
            {
                return new NbtByte("", Byte.parseByte(p_193596_1_.substring(0, p_193596_1_.length() - 1)));
            }

            if (PATTERN_LONG.matcher(p_193596_1_).matches())
            {
                return new NbtLong("", Long.parseLong(p_193596_1_.substring(0, p_193596_1_.length() - 1)));
            }

            if (PATTERN_SHORT.matcher(p_193596_1_).matches())
            {
                return new NbtShort("", Short.parseShort(p_193596_1_.substring(0, p_193596_1_.length() - 1)));
            }

            if (PATTERN_INT.matcher(p_193596_1_).matches())
            {
                return new NbtInt("", Integer.parseInt(p_193596_1_));
            }

            if (PATTERN_DOUBLE.matcher(p_193596_1_).matches())
            {
                return new NbtDouble("", Double.parseDouble(p_193596_1_.substring(0, p_193596_1_.length() - 1)));
            }

            if (PATTERN_UNTAGGED_DOUBLE.matcher(p_193596_1_).matches())
            {
                return new NbtDouble("", Double.parseDouble(p_193596_1_));
            }

            if ("true".equalsIgnoreCase(p_193596_1_))
            {
                return new NbtByte("", (byte)1);
            }

            if ("false".equalsIgnoreCase(p_193596_1_))
            {
                return new NbtByte("", (byte)0);
            }
        }
        catch (NumberFormatException ignored) {}

        return new NbtString("", p_193596_1_);
    }

    private String readStringTag() throws NbtException
    {
        int i = ++this.readIndex;
        StringBuilder stringbuilder = null;
        boolean flag = false;

        while (this.canReadNext())
        {
            char c0 = this.readNextChar();

            if (flag)
            {
                if (c0 != '\\' && c0 != '"')
                {
                    throw this.createException("Invalid escape of '" + c0 + "'");
                }

                flag = false;
            }
            else
            {
                if (c0 == '\\')
                {
                    flag = true;

                    if (stringbuilder == null)
                    {
                        stringbuilder = new StringBuilder(this.inputJson.substring(i, this.readIndex - 1));
                    }

                    continue;
                }

                if (c0 == '"')
                {
                    return stringbuilder == null ? this.inputJson.substring(i, this.readIndex - 1) : stringbuilder.toString();
                }
            }

            if (stringbuilder != null)
            {
                stringbuilder.append(c0);
            }
        }

        throw this.createException("Missing termination quote");
    }

    private String readWord()
    {
        int i = this.readIndex;

        while (this.canReadNext() && this.isNonSpecialCharacter(this.peek())) {
            ++this.readIndex;
        }

        return this.inputJson.substring(i, this.readIndex);
    }

    private NbtElement parseElement() throws NbtException
    {
        this.consumeWhitespace();

        if (!this.canReadNext())
        {
            throw this.createException("Expected value");
        }
        else
        {
            char c0 = this.peek();

            if (c0 == '{')
            {
                return this.parseCompound();
            }
            else
            {
                return c0 == '[' ? this.parseLists() : this.parseNonList();
            }
        }
    }

    private NbtElement parseLists() throws NbtException
    {
        return this.canReadChars(2) && this.peekAhead(1) != '"' && this.peekAhead(2) == ';' ? this.parseArray() : this.parseList();
    }

    private NbtCompound parseCompound() throws NbtException
    {
        this.consumeChar('{');
        NbtCompound compound = new NbtCompound();
        this.consumeWhitespace();

        while (this.canReadNext() && this.peek() != '}')
        {
            String s = this.readTagKey();

            if (s.isEmpty())
            {
                throw this.createException("Expected non-empty key");
            }

            this.consumeChar(':');
            compound.put(s, this.parseElement());

            if (!this.consumeElementSplit())
            {
                break;
            }

            if (!this.canReadNext())
            {
                throw this.createException("Expected key");
            }
        }

        this.consumeChar('}');
        return compound;
    }

    private NbtElement parseList() throws NbtException
    {
        this.consumeChar('[');
        this.consumeWhitespace();

        if (!this.canReadNext())
        {
            throw this.createException("Expected value");
        }
        else
        {
            NbtList list = new NbtList();
            int i = -1;

            while (this.peek() != ']')
            {
                NbtElement element = this.parseElement();
                int j = element.getType();

                if (i < 0)
                {
                    i = j;
                }
                else if (j != i)
                {
                    throw this.createException("Unable to insert " + j + " into ListTag of type " + i);
                }

                list.method_1217(element);

                if (!this.consumeElementSplit())
                {
                    break;
                }

                if (!this.canReadNext())
                {
                    throw this.createException("Expected value");
                }
            }

            this.consumeChar(']');
            return list;
        }
    }

    private NbtElement parseArray() throws NbtException
    {
        this.consumeChar('[');
        char c0 = this.readNextChar();
        this.readNextChar();
        this.consumeWhitespace();

        if (!this.canReadNext())
        {
            throw this.createException("Expected value");
        }
        else if (c0 == 'B')
        {
            return new NbtByteArray("", this.parseByteArray());
        }
        else if (c0 == 'I')
        {
            return new NbtIntArray("", this.parseIntArray());
        }
        else
        {
            throw this.createException("Invalid array type '" + c0 + "' found");
        }
    }

    private byte[] parseByteArray() throws NbtException
    {
        List<Byte> list = new ArrayList<>();

        while (true)
        {
            if (this.peek() != ']')
            {
                NbtElement element = this.parseElement();
                int elementType = element.getType();

                if (elementType != 1)
                {
                    throw this.createException("Unable to insert " + elementType + " into " + 7);
                }

                list.add(((NbtByte)element).value);

                if (this.consumeElementSplit())
                {
                    if (!this.canReadNext())
                    {
                        throw this.createException("Expected value");
                    }

                    continue;
                }
            }

            this.consumeChar(']');

            byte[] returnValue = new byte[list.size()];

            for (int i = 0; i < list.size(); i++) {
                returnValue[i] = list.get(i);
            }

            return returnValue;
        }
    }

    private int[] parseIntArray() throws NbtException
    {
        List<Integer> list = new ArrayList<>();

        while (true)
        {
            if (this.peek() != ']')
            {
                NbtElement element = this.parseElement();
                int elementType = element.getType();

                if (elementType != 3)
                {
                    throw this.createException("Unable to insert " + elementType + " into " + 11);
                }

                list.add(((NbtInt)element).value);

                if (this.consumeElementSplit())
                {
                    if (!this.canReadNext())
                    {
                        throw this.createException("Expected value");
                    }

                    continue;
                }
            }

            this.consumeChar(']');

            int[] returnValue = new int[list.size()];

            for (int i = 0; i < list.size(); i++) {
                returnValue[i] = list.get(i);
            }

            return returnValue;
        }
    }

    private void consumeWhitespace()
    {
        while (this.canReadNext() && Character.isWhitespace(this.peek()))
        {
            ++this.readIndex;
        }
    }

    private boolean consumeElementSplit()
    {
        this.consumeWhitespace();

        if (this.canReadNext() && this.peek() == ',')
        {
            ++this.readIndex;
            this.consumeWhitespace();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void consumeChar(char p_193604_1_) throws NbtException
    {
        this.consumeWhitespace();
        boolean flag = this.canReadNext();

        if (flag && this.peek() == p_193604_1_)
        {
            ++this.readIndex;
        }
        else
        {
            throw new NbtException("Expected '" + p_193604_1_ + "' but got '" + (flag ? this.peek() : "<EOF>") + "'", this.inputJson, this.readIndex + 1);
        }
    }

    private boolean isNonSpecialCharacter(char p_193599_1_)
    {
        return p_193599_1_ >= '0' && p_193599_1_ <= '9' || p_193599_1_ >= 'A' && p_193599_1_ <= 'Z' || p_193599_1_ >= 'a' && p_193599_1_ <= 'z' || p_193599_1_ == '_' || p_193599_1_ == '-' || p_193599_1_ == '.' || p_193599_1_ == '+';
    }

    private boolean canReadChars(int p_193608_1_)
    {
        return this.readIndex + p_193608_1_ < this.inputJson.length();
    }

    private boolean canReadNext()
    {
        return this.canReadChars(0);
    }

    private char peekAhead(int p_193597_1_)
    {
        return this.inputJson.charAt(this.readIndex + p_193597_1_);
    }

    private char peek()
    {
        return this.peekAhead(0);
    }

    private char readNextChar()
    {
        return this.inputJson.charAt(this.readIndex++);
    }
}
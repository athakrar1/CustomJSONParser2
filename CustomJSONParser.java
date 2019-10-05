import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.*;

public class CustomJSONParser {

    public CustomJSONParser(String json) {

        branches = new ArrayList<>();

        data = new DataMaps();

        asString = json;

        //get to the start of the json object
        int index = json.indexOf('{');

        //loop while ______
        while (index > 0) {
            //get the key by searching from the current position to the next colon
            String key = json.substring(index = index + 1, json.indexOf(':', index)).trim();

            //put the marker in the position one after the colon
            index += key.length() + 1;

            //get to next nonwhitespace character

            char currChar;

            while ((currChar = json.charAt(index)) == ' ') {
                index++;
            }

            //now, idx is not on a space

            if(currChar == '\''){
                data.putString(key,
                        json.substring(index = index + 1, index = json.indexOf('\'', index))
                );
            }
            else if(currChar == '{') {
                data.putObj(key, new CustomJSONParser(json.substring(index, getBoundedValue(json, index, '{', '}') + 1))); // hmm this might not work
            }

            else if(currChar == '[') {
                data.putArr(key, getArray(json.substring(index, getArrayEnd(json, index) + 1)));
            }

            else{
                int start = index;
                boolean isFloat = false;
                while (Character.isDigit(currChar) || ((currChar == '.') ? (isFloat = true) : false)) {
                    index++;
                    currChar = json.charAt(index);
                }

                String number = json.substring(start, index);

                if (isFloat) {
                    data.putFloat(key, Float.parseFloat(number));
                } else {
                    data.putInt(key, Integer.parseInt(number));
                }
            }

            index = json.indexOf(',', index) + 1;
            //now, idx is one space after the next comma
        }
    }

    public static CustomJSONParser initialize(String json) {
        return new CustomJSONParser(json);
    }

    public List<String> listAllKeysAtCurrentLevel(int depth) {
        List<String> keys = new ArrayList<>();

        for (String key : data.allKeys) {
            keys.add(key);
        }

        return keys;
    }

    public String getJSONObject(String key) {
        return data.objectMap.get(key).asString;
    }

    public boolean checkIfTagExists(String tag) {
        return data.allKeys.contains(tag);
    }

    public int maxDepth() {
        int max = 1;
        for (CustomJSONParser branch : branches) {
            int childMaxDepth = 1 + branch.maxDepth();
            if (childMaxDepth > max) {
                max = childMaxDepth;
            }
        }
        return max;
    }

    // helper functions for the stuff that's above

    public List<Object> getJSONArray(String key) {
        return data.arrayMap.get(key);
    }

    public String getString(String key) {
        return data.stringMap.get(key);
    }


    public Float getFloat(String key) {
        return data.floatMap.get(key);
    }

    public Integer getInt(String key) {
        return data.intMap.get(key);
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private DataMaps data;

    private String asString;

    private List<CustomJSONParser> branches;

    class DataMaps {
        private Map<String, CustomJSONParser> objectMap;
        private Map<String, String> stringMap;
        private Map<String, Integer> intMap;
        private Map<String, Float> floatMap;
        private Map<String, List<Object>> arrayMap;

        private Set<String> allKeys;

        DataMaps() {
            objectMap = new HashMap<>();
            stringMap = new HashMap<>();
            intMap = new HashMap<>();
            floatMap = new HashMap<>();
            arrayMap = new HashMap<>();

            allKeys = new HashSet<>();
        }

        CustomJSONParser getObject(String key) {
            return objectMap.get(key);
        }

        void putString(String key, String string) {
            if (allKeys.add(key))
                stringMap.put(key, string);
        }

        void putArr(String key, List<Object> array) {
            if (allKeys.add(key))
                arrayMap.put(key, array);
        }

        void putFloat(String key, Float aFloat) {
            if (allKeys.add(key))
                floatMap.put(key, aFloat);
        }

        void putInt(String key, Integer integer) {
            if (allKeys.add(key))
                intMap.put(key, integer);
        }

        void putObj(String key, CustomJSONParser object) {
            if (allKeys.add(key))
                objectMap.put(key, object);
        }
    }

    private List<Object> getArray(String s) {

        List<Object> array = new ArrayList<>();

        int index = 1;

        while (index > 0) {
            //get to next nonwhitespace character

            char currChar;

            while ((currChar = s.charAt(index)) == ' ') {
                index++;
            }

            //now, idx is not on a space

            if(currChar == '\''){
                array.add(s.substring(index = index + 1, index = s.indexOf('\'', index)));
            }
            else if(currChar == '{') {
                array.add(new CustomJSONParser(s.substring(index, getBoundedValue(s, index, '{', '}') + 1))); //hmm might now work
            }

            else if(currChar == '[') {
                array.add(getArray(s.substring(index, getArrayEnd(s, index) + 1)));
            }

            else{
                int start = index;
                boolean isFloat = false;
                while (Character.isDigit(currChar) || currChar == '.' ? isFloat = true : false) {
                    index++;
                    currChar = s.charAt(index);
                }
            }

            index = s.indexOf(',', index) + 1;
            //now, idx is one space after the next comma

        }

        return array;

    }


    /**
     * Returns the ending index of the object in string that starts at the index "start"
     */
    private static int getObjectEnd(String s, int start) {
        return getBoundedValue(s, start, '{', '}');
    }

    private static int getArrayEnd(String s, int start) {
        return getBoundedValue(s, start, '[', ']');
    }

    private static int getBoundedValue(String s, int start, final char START_STRING, final char END_STRING) {
        int closedVal = 1;

        while (closedVal > 0) {
            //shift start one ahead of starting brace
            start++;
            int openBrace = s.indexOf(START_STRING, start), closedBrace = s.indexOf(END_STRING, start);
            if (openBrace > 0 && openBrace < closedBrace) {
                closedVal++;
                start = openBrace;
            } else {
                closedVal--;
                start = closedBrace;
            }
        }

        return start;
    }
}
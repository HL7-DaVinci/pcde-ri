package ca.uhn.fhir.jpa.starter.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/**
 * JSONWrapper is designed to make the simple json class easier to use
 */
public class JSONWrapper {
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private Object value;
    /**
     * Constructor for JSONWrapper
     * @param json an object containing an element of json
     */
    public JSONWrapper(Object json) {
        if (json instanceof JSONObject) {
            jsonObject = (JSONObject) json;
        } else if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        }
        value = json;
        if (json instanceof JSONWrapper) {
            Object v = ((JSONWrapper) json).getValue();
            if (v instanceof JSONObject) {
                jsonObject = (JSONObject) v;
            } else if (v instanceof JSONArray) {
                jsonArray = (JSONArray) v;
            }
            value = v;
        }
    }
    /**
     * checks if the jsonObject has the key
     * @param  value the string to check
     * @return       true if the key is exists and false if it doesn't
     */
    public boolean hasKey(String value) {
        if (jsonObject == null) {
            return false;
        }
        try {
            return jsonObject.containsKey(value) && jsonObject.get(value) != null;
        } catch(Exception e) {
            return false;
        }
    }
    /**
     * checks if the jsonArray has the key
     * @param  value the int to check
     * @return       true if the key is exists and false if it doesn't
     */
    public boolean hasKey(int value) {
        if (jsonObject == null) {
            return false;
        }
        try {
            return jsonArray.get(value) != null;
        } catch(Exception e) {
            return false;
        }
    }
    /**
     * Get the value of the key value pair in the json
     * @param  value the key to use
     * @return       the value
     */
    public JSONWrapper get(String value) {
        if (jsonObject == null) {
            throw new IllegalArgumentException("Invalid key type");
        }
        try {
            return new JSONWrapper(jsonObject.get(value));
        } catch(Exception e) {
            throw new IllegalArgumentException("Key " + value + " does not exist");
        }
    }
    /**
     * Get the value of the key value pair in the json
     * @param  value the index of the jsonArray
     * @return       the value at the index
     */
    public JSONWrapper get(int value){
        if (jsonArray == null) {
            throw new IllegalArgumentException("Invalid key type");
        }
        try {
            return new JSONWrapper(jsonArray.get(value));
        } catch (Exception e) {
            throw new IllegalArgumentException("Key " + value + " does not exist");
        }
    }
    /**
     * Add key value pair
     * @param key      the key to add
     * @param newValue the value to add
     */
    public void put(String key, Object newValue) {
        jsonObject.put(key, newValue);
        value = jsonObject;
    }
    /**
     * Add object to jsonArray
     * @param newValue the value to add
     */
    public void add(Object newValue) {
        jsonArray.add(newValue);
        value = jsonArray;
    }
    /**
     * @return the base object stored by the JSONWrapper
     */
    public Object getValue() {
        return value;
    }
    /**
     * The String value of the JSONWrapper
     * @return the string value
     */
    public String toString() {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
    /**
     * Returns the size of the jsonArray if it exists
     * @return the size of the jsonArray
     */
    public int size() {
        if (jsonArray != null) {
            return jsonArray.size();
        }
        return 0;
    }
}

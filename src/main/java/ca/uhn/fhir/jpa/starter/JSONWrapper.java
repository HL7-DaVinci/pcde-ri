package ca.uhn.fhir.jpa.starter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONWrapper {
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private Object value;
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
    public void put(String key, Object newValue) {
        jsonObject.put(key, newValue);
        value = jsonObject;
    }
    public void add(Object newValue) {
        jsonArray.add(newValue);
        value = jsonArray;
    }
    public Object getValue() {
        return value;
    }
    public String toString() {
        if (value == null) {
            return this.toString();
        }
        return value.toString();
    }
    public int size() {
        if (jsonArray != null) {
            return jsonArray.size();
        }
        return 0;
    }
}

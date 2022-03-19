package com.cimraffi.android.birdeye.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * SharedPreference工具类
 */
public class SharedPreferenceUtils {
    public static final String SP_NAME = "mcvision";
    /**
     * SharedPreferences
     */
    public SharedPreferences sp;

    public SharedPreferenceUtils(Context context, String filename, int model) {
        this.sp = context.getSharedPreferences(filename, model);
    }

    public SharedPreferenceUtils(Context context) {
        this(context, SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 将Object保存到sp中
     *
     * @param key
     * @param value
     */
    final public <T extends Object> void saveData(String key, T value) {
        try {
            Gson gson = new Gson();
            String valueString = gson.toJson(value);
            saveData(key, valueString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将String保存到sp中
     *
     * @param key
     * @param value
     */
    final public void saveData(String key, String value) {
        sp.edit().putString(key, value).commit();
    }

    /**
     * 将int保存到sp中
     *
     * @param key
     * @param value
     */
    final public void saveData(String key, int value) {
        sp.edit().putInt(key, value).commit();
    }

    /**
     * 将boolean保存到sp中
     *
     * @param key
     * @param value
     */
    final public void saveData(String key, boolean value) {
        sp.edit().putBoolean(key, value).commit();
    }

    /**
     * 将float保存到sp中
     *
     * @param key
     * @param value
     */
    final public void saveData(String key, float value) {
        sp.edit().putFloat(key, value).commit();
    }

    /**
     * 将long保存到sp中
     *
     * @param key
     * @param value
     */
    final public void saveData(String key, long value) {
        sp.edit().putLong(key, value).commit();
    }

    /**
     * 从sp中获取Object
     *
     * @param key
     * @return
     */
    final public <T extends Object> T getSaveObjectData(String key, Class<T> clazz) {
        try {
            Gson g = new Gson();
            String stringJson = getSaveStringData(key, "");
            return g.fromJson(stringJson, clazz);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从sp中获取String
     *
     * @param key
     * @param defaultValue
     * @return
     */
    final public String getSaveStringData(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    /**
     * 从sp中获取int
     *
     * @param key
     * @param defaultValue
     * @return
     */
    final public int getSaveIntData(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    /**
     * 从sp中获取boolean
     *
     * @param key
     * @param defaultValue
     * @return
     */
    final public boolean getSaveBooleanData(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * 从sp中获取float
     *
     * @param key
     * @param defaultValue
     * @return
     */
    final public float getSaveFloatData(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    /**
     * 从sp中获取long
     *
     * @param key
     * @param defaultValue
     * @return
     */
    final public long getSaveLongData(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    final public void removeData(String key) {
        sp.edit().remove(key).commit();
    }

    public void clearAll() {
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        }
    }
}

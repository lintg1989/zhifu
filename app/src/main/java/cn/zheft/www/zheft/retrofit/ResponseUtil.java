package cn.zheft.www.zheft.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 处理新接口返回的data
 * 返回的data为String字符串，需要先获取String，解析Json后转Map
 */

public class ResponseUtil {
    private static final String TAG = ResponseUtil.class.getSimpleName();

    // 默认值
    public static final String SHARE_STRING = "";
    public static final Integer SHARE_INTEGER = 0;
    public static final Boolean SHARE_BOOLEAN = false;

    public static Map getMap(Object data) {
        Map<String, Object> map = new HashMap<>();
        if (data == null) {
            LogUtil.e(TAG, "Data is null in getMap()");
            return map;
        }
        if (data instanceof String) {
            return new Gson().fromJson(data.toString(), new TypeToken<Map>(){}.getType());
        } else {
            LogUtil.e(TAG, data.getClass().getName());
            return map;
        }
    }

    public static <T> T getData(Object data, final Class<T> cls) {
        if (data == null) {
            LogUtil.e(TAG, "Data is null in getData()");
            return null;
        }
        if (data instanceof String) {
            return new Gson().fromJson(data.toString(), cls);
        } else {
            LogUtil.e(TAG, data.getClass().getName());
            return null;
        }
    }

    public static <T> List<T> getList(Object data, Class<T> cls) {
        List<T> list = new ArrayList<>();
        if (data == null) {
            LogUtil.e(TAG, "Data is null in getList()");
            return list;
        }
        if (data instanceof String) {
//            list = new Gson().fromJson(data.toString(), new TypeToken<List<T>>(){}.getType());
            JSONArray array= null;
            try {
                array = new JSONArray(data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(array!=null){
                for (int i=0;i<array.length();i++) {
                    try {
                        JSONObject jo= new JSONObject(array.get(i).toString());
                        Map<String,Object> item=TransJsonObjToMap(jo);
                        T entity= TransToEntity(cls,item);
                        if(entity!=null){
                            list.add(entity);
                        }
                    } catch (JSONException e) {

                    }

                }
            }
        } else {
            LogUtil.e(TAG, data.getClass().getName());
        }
        return list;
    }

    public static final <T> List<T> getList(Object data) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Type typeOfList = new TypeToken<List<T>>(){}.getType();
        return gson.fromJson(data.toString(), typeOfList);
    }

    public static Object getValueByKey(Object data, Object object) {
        return getValueByKey(getMap(data), object);
    }

    public static Object getValueByKey(Map data, Object object) {
        if (object instanceof String) {
            //
        } else if (object instanceof Integer) {
            //
        } else if (object instanceof Boolean) {
            //
        } else {
            LogUtil.e(TAG, object.getClass().getName());
        }
        return null;
    }




    private static Map<String, Object> buildResult(String jsonString) {
        Map<String, Object> result = new HashMap<>();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Iterator<?> iterator = jsonObject.keys();
        String key = null;
        String value = null;

        while (iterator.hasNext()) {

            key = (String) iterator.next();
            try {
                value = jsonObject.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (value.startsWith("[")) {
                if(key.equals("result")){
                    result.put(key,buildHead(value));
                }else {
                    result.put(key,buildJsonArray(value));
                }
            } else if (value.startsWith("{")) {
                result.put(key, buildResult(value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    private static Object buildJsonArray(String jsonString){
        JSONArray array= null;
        try {
            array = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<Object> res=new ArrayList<>();
        for (int i=0;i<array.length();i++) {
            String value= null;
            try {
                value = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Map<String, Object> map = new HashMap<>();
            if (value.startsWith("[")) {
                res.add(buildJsonArray(value));
            } else if (value.startsWith("{")) {
                map=buildResult(value);
                if(map!=null&& map.size()>0){
                    res.add(map);
                }
            } else {
                res.add(value);
            }
//			System.err.println(value);
        }
        return res;
    }

    private static List<Map<String, Object>> buildHead(String jString) {
        List<Map<String, Object>> res=new ArrayList<>();
        JSONArray array= null;
        try {
            array = new JSONArray(jString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0;i<array.length();i++) {
            Map<String, Object> map = new HashMap<>();
            String value= null;
            try {
                value = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (value.startsWith("[")) {
                buildJsonArray(value);
            } else if (value.startsWith("{")) {
                map=buildResult(value);
                if(map!=null&& map.size()>0){
                    res.add(map);
                }
            } else {
//				return value;
            }
//			System.err.println(value);
        }

        return res;
    }

//    public static String TransValueToUrl(String content) {
//        String result = content;
//        try {
//            result = URLEncoder.encode(result, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return result;
//    }

    private static <T> T TransToEntity(Class<T> entityClzz, Map<String, Object> entityMap) {

        T entity = null;
        try {
            entity = entityClzz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (entityMap == null || entityMap.size() < 1) {
            return entity;
        }
        Field[] fields = entityClzz.getDeclaredFields();
        if (fields == null) {
            return null;
        }
        for (Field field : fields) {
            String key = field.getName();
            field.setAccessible(true);
            Object value = null;
            Class<?> ftype = field.getType();
            if (entityMap.containsKey(key) || entityMap.containsKey(key.toLowerCase())
                    || entityMap.containsKey(key.toUpperCase())) {
                if (entityMap.containsKey(key)) {
                    value = entityMap.get(key);
                } else if (entityMap.containsKey(key.toLowerCase())) {
                    value = entityMap.get(key.toLowerCase());
                } else {
                    value = entityMap.get(key.toUpperCase());
                }
            }
            if (value != null) {
                try {
                    String typeName = ftype.getName();
                    switch (typeName) {
                        case "java.lang.Integer":
                            Integer _v = Integer.parseInt(String.valueOf(value));
                            field.set(entity, _v);
                            break;
                        default:
                            field.set(entity, String.valueOf(value));
                            break;
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
                }
            }
        }
        return entity;
    }

    private static  Map<String,Object> TransJsonObjToMap(JSONObject json){
        Map<String,Object> map=new HashMap<>();
        if(json==null){
            return map;
        }
        Iterator<String> keys=json.keys();
        while (keys.hasNext()){
            String key=keys.next();
            try {
                map.put(key,json.get(key));
            } catch (JSONException e) {

            }
        }
        return  map;
    }
}

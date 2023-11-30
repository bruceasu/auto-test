package me.asu.test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.asu.test.util.StringUtils.isEmpty;


public class JacksonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

    public static String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeAsBytes(Object data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeForPrint(Object data) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode deserialize(String data) {
        try {
            return objectMapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode deserialize(byte[] data) {
        try {
            return objectMapper.readTree(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T deserialize(String data, Class<T> cls) {
        if (cls == String.class) {
            return (T) data;
        } else {
            try {
                return objectMapper.readValue(data, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return cls;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> cls) {
        if (cls == String.class) {
            return (T) data;
        } else {
            try {
                return objectMapper.readValue(data, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return cls;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T deserialize(String data, TypeReference type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    // 使用 TypeReference 反序列化含有泛型的对象
//    public static <T> Response<T> fromJson(String json, Class<T> clazz) throws IOException {
//        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(Response.class, clazz));
//    }

//    // 反序列化泛型集合
//    public static <T> List<T> listFromJson(String json, Class<T> clazz) throws IOException {
//        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
//    }



    /**
     * to Map
     *
     * @param data json String
     * @return a Map
     */
    public static Map deserializeToMap(String data) {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map deserializeToMap(byte[] data) {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> deserializeToList(String data, Class<T> cls) {
        CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, cls);
        try {
            return objectMapper.readValue(data, listType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Map> deserializeToList(String data) {
        return deserializeToList(data, Map.class);
    }

    public static Map convertToMap(Object object) {
        //用jackson将bean转换为map
        return objectMapper.convertValue(object, new TypeReference<Map>() {
        });
    }


    public static List<Map> convertToListMap(List<Object> list) {
        //用jackson将bean转换为List<Map>
        return objectMapper.convertValue(list, new TypeReference<List<Map<String, String>>>() {
        });
    }

    public static <T> T convertToObject(Object data, Class<T> cls) {
        if (data == null) return null;
        if (data.getClass() == JsonNode.class) {
            return (T) data;
        } else {
            try {
                return objectMapper.convertValue(data, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return cls;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T readToObject(String data,TypeReference<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String asText(JsonNode dtNode, String item) {
        return asText(dtNode, item, null);
    }

    public static String asText(JsonNode dtNode, String item, String defaultValue) {
        if (dtNode == null) {
            return defaultValue;
        }

        JsonNode node = dtNode.get(item);
        if (node == null) {
            return defaultValue;
        }
        return node.asText();
    }

    public static JsonNode at(JsonNode dtNode, String path) {
        if (dtNode == null || isEmpty(path)) {
            return null;
        }

        return dtNode.at(path);
    }

    public static String atAsText(JsonNode dtNode, String path) {
        if (dtNode == null || isEmpty(path)) {
            return null;
        }

        return dtNode.at(path).asText();
    }

    public static ObjectNode createObject() {
        return objectMapper.createObjectNode();

    }

    public static ArrayNode createArray() {
        return objectMapper.createArrayNode();
    }
}

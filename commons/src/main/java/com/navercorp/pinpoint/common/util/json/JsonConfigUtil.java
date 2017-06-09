package com.navercorp.pinpoint.common.util.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfigUtil {
	public static Map<String,Object> convert(String json){
		JsonObject datas = Json.parse(json).asObject().get("data").asObject();
		List<String> names= datas.names();
		Map<String,Object> values=new HashMap<String,Object>();
		for(String name:names){
			values.put(name,datas.get(name));
		}
		return values;
	}
}

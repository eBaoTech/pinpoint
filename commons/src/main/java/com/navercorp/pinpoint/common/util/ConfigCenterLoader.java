package com.navercorp.pinpoint.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import com.navercorp.pinpoint.common.util.json.JsonConfigUtil;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import sun.misc.BASE64Encoder;


/**
 * 通过环境变量或者属性系统或者配置中心来获取
 * @param origin
 */
public class ConfigCenterLoader {
	private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(ConfigCenterLoader.class.getName());
	
	public Properties loader(){
		File bootstrap= new File(".","conf/bootstrap.properties");
		Properties properties=null;
		if(bootstrap.exists())
		{
			try {
				properties=PropertyUtils.loadProperty(bootstrap.getAbsolutePath());
			} catch (Exception e) {
				logger.warn("error when load "+ bootstrap.getAbsolutePath(), e);
			}
		}
		
		if(properties==null)
		{
			properties=new Properties();
		}
		
		return loader(properties);
	}
	
	public Properties loader(Properties origin){
		ConfigDef configDef=new ConfigDef(origin);
		return loadFromConfigcenter(configDef);
	}
	
	public static void overrideProperies(Map<?,?> source, Properties target) {
		for(Object key:source.keySet())
		{
			//如果target中不含有此配置项,放弃加入
			if(target.containsKey(key)==false)
			{
				continue;
			}
			
			target.put(key, source.get(key));
		}
	}

	private Properties loadFromConfigcenter(ConfigDef configDef) {
		logger.info("[ebao.config.center.url="+configDef.getUrl()+"]");
		logger.info("[ebao.config.center.name="+configDef.getApplication()+"]");
		logger.info("[ebao.config.center.profile="+configDef.getProfile()+"]");
		logger.info("[ebao.config.center.label="+configDef.getLabel()+"]");
		logger.info("[ebao.config.center.enabled="+configDef.getEnabled()+"]");
		logger.info("[ebao.config.center.username="+configDef.getUsername()+"]");
		
		Properties result=new Properties();
		if(configDef.validate())
		{
			logger.info("will load pinpoint config from config center..");
		}else
		{
			logger.info("ignore load pinpoint config from config center according above information!");
			return result;
		}
		
		
		Properties publicConfig=loadFromConfigcenter(configDef,"public");
		Properties applicationConfig=loadFromConfigcenter(configDef,configDef.getApplication());
		publicConfig.putAll(applicationConfig);
		
		
		return publicConfig;
	}

	@SuppressWarnings({ "rawtypes", "unchecked"})
	private Properties loadFromConfigcenter(ConfigDef configDef,String name)
	{
		String json=getJSon(configDef,name);
		Properties result= new Properties();
		if(json==null)
		{
			return result;
		}
		
		Map datas=JsonConfigUtil.convert(json);
		result.putAll(datas);
		return result;
	}

	private String getJSon(ConfigDef configDef, String name)  {
		String url=MessageFormat.format("{0}/configs?profile={1}&application={2}&label={3}",new Object[]{
			configDef.getUrl(),
			configDef.getProfile(),
			name,
			configDef.getLabel()
		});
		
		InputStream is=null;
		try{
			URL target = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)target.openConnection();
			String userCredentials = configDef.getUsername()+":"+configDef.getPassword();
			String basicAuth = "Basic " + new String(new BASE64Encoder().encode(userCredentials.getBytes()));
			conn.setRequestProperty ("Authorization", basicAuth);
			conn.setRequestProperty("Content-Language", "en-US");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.connect();
			is = conn.getInputStream();
	        String result = convertStreamToString(is);
	        return result;
		}catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					
				}
			} 
		}
		
	}
	
	 private String convertStreamToString(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try{
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }
	public static class ConfigDef{
		private String application=null;
		private String url=null;
		private  String username=null;
		private String password=null;
		private String profile=null;
		private String label=null;
		private Boolean enabled=false;
		
		public ConfigDef(Properties bootstrap){
			init(bootstrap);
		}
		
		private void init(Properties bootstrap) {
			application=findConfig(bootstrap,"spring.application.name");
			url=findConfig(bootstrap,"ebao.config.center.url");
			username=findConfig(bootstrap,"ebao.config.center.username");
			password=findConfig(bootstrap,"ebao.config.center.password");
			profile=findConfig(bootstrap,"ebao.config.center.profile");
			label=findConfig(bootstrap,"ebao.config.center.label","snapshot");
			enabled=Boolean.valueOf(findConfig(bootstrap,"ebao.config.center.enabled","true"));
		}
		
		public boolean validate(){
			return enabled.equals(true) && hasText(url) && hasText(application) && hasText(profile);
		}
		
		private String findConfig(Properties bootstrap, String name,String defaultValue) {
			String value=System.getenv().get(name);
			if(hasText(value))
			{
				return value;
			}
			value=System.getProperty(name);
			if(hasText(value))
			{
				return value;
			}
			
			value=bootstrap.getProperty(name);
			if(!hasText(value))
			{
				value=defaultValue;
			}
			return value;
		}
		
		private String findConfig(Properties bootstrap, String name) {
			return findConfig(bootstrap,name,null);
		}
		
		private boolean  hasText(String value){
			return value!=null && value.trim().length()>0;
		}

		public String getApplication() {
			return application;
		}

		public void setApplication(String application) {
			this.application = application;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getProfile() {
			return profile;
		}

		public void setProfile(String profile) {
			this.profile = profile;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Boolean getEnabled() {
			return enabled;
		}

		public void setEnabled(Boolean enabled) {
			this.enabled = enabled;
		}
	}
	
	public static class Config{
		private String profile=null;
		private String application=null;
		private String label=null;
		private Map<String,Object> data=null;
		public String getProfile() {
			return profile;
		}
		public void setProfile(String profile) {
			this.profile = profile;
		}
		public String getApplication() {
			return application;
		}
		public void setApplication(String application) {
			this.application = application;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public Map<String, Object> getData() {
			return data;
		}
		public void setData(Map<String, Object> data) {
			this.data = data;
		}
	}
}

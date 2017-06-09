package com.navercorp.pinpoint.web.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;

import com.navercorp.pinpoint.common.util.ConfigCenterLoader;

public class ExPropertiesFactory extends PropertiesFactoryBean {
	protected Properties mergeProperties() throws IOException {
		Properties result=new Properties();
		ConfigCenterLoader configCenterLoader=new ConfigCenterLoader();
		
		Properties properties= configCenterLoader.loader();
		ConfigCenterLoader.overrideProperies(properties, result);
		return result;
		
	}
}

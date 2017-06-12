package com.navercorp.pinpoint.collector.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.navercorp.pinpoint.common.util.ConfigCenterLoader;

public class ExPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	protected Properties mergeProperties() throws IOException {
		Properties result=super.mergeProperties();
		ConfigCenterLoader configCenterLoader=new ConfigCenterLoader();
		
		Properties properties= configCenterLoader.loader(result);
		ConfigCenterLoader.overrideProperies(properties, result);
		return result;
		
	}
}

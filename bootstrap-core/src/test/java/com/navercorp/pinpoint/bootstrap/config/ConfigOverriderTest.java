package com.navercorp.pinpoint.bootstrap.config;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class ConfigOverriderTest {
	@Test
	public void testOverride(){
		ConfigOverrider override=new ConfigOverrider();
		
		File pluginsHome=new File("D:/pinpoint/release/agent");
		File bootstrap=new File("D:/temp/bootstrap.properties");
		
		try{
			Properties properties=new Properties();
			override.process(pluginsHome, properties,bootstrap);
		}catch(Exception ex)
		{
			ex.printStackTrace();
			fail(ex.getMessage());
		}
		
	}
	
}

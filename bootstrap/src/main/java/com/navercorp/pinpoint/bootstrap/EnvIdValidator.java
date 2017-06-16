package com.navercorp.pinpoint.bootstrap;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import com.navercorp.pinpoint.common.util.PropertyUtils;

/**
 * conf/bootstrap.properties来自动推断applicationName
 * 基于env来自动获取agentId
 * @author shawn.yang
 *
 */
public class EnvIdValidator extends IdValidator {
	private final BootLogger logger = BootLogger.getLogger(EnvIdValidator.class.getName());
	private static final int MAX_SIZE=24;
	private static final String ID_STRATEGY_RANDOM="random";
	private static final String ID_STRATEGY_SAME="same";
	private static final String ID_STRATEGY_ASSIGN="assign";
	private String pluginDir=null;
	private String appName=null;
	private String agentId=null;
	public EnvIdValidator(String pluginDir){
		super();
		this.pluginDir=pluginDir;
		appName=resolveAppName();
		agentId=resolveAgentId();
	}
	
	/**
	 * control agent id base on strategy
	 * @return
	 */
	protected String resolveAgentId() {
		String idStrategy=System.getenv("pinpoint.agent.id.strategy");
		String result=null;
		
		logger.info("using ["+idStrategy+"] for building agent id");
		if(idStrategy==null)
		{
			idStrategy=ID_STRATEGY_SAME;
		}
		
		if(idStrategy!=null)
		{
			if(idStrategy.equals(ID_STRATEGY_SAME))
			{
				result=appName;
			}
			
			if(idStrategy.equals(ID_STRATEGY_RANDOM))
			{
				String hostName=System.getenv("HOSTNAME");
				hostName=hostName+"-"+appName+"-"+pluginDir;
				result= IdBuilder.generateId(hostName);
				logger.info("build random id base on ["+hostName+"],encoded result is " + result);
			}
			
			if(idStrategy.equals(ID_STRATEGY_ASSIGN))
			{
				String key="pinpoint.agentId";
				result=System.getenv(key);
				if(result==null)
				{
					result=System.getProperty(key);
				}
			}
		}
		
		if(result==null || result.length()==0)
		{
			throw new RuntimeException("fail  to decide the pinpoint agent id");
		}
		
		logger.info("using ["+idStrategy+"] for building agent id,encoded result is " + result);
		return result;
	}

	private String resolveAppName() {
		String applicationName=null;
		try{
			File pluginHome=new File(pluginDir);
			if(pluginHome.exists())
			{
				File bootstrapFile= new File(".","conf/bootstrap.properties");
				if(bootstrapFile.exists() && bootstrapFile.isFile())
				{
					Properties bootstrap=PropertyUtils.loadProperty(bootstrapFile.getCanonicalPath());
					logger.info("success find bootstrap.properties,use it to get applicationName");
					applicationName=format(bootstrap.getProperty("spring.application.name"));
				}else
				{
					logger.warn("can not find bootstrap.properties at "+bootstrapFile.getCanonicalPath());
				}
			}
		}catch(Exception ex)
		{
			logger.warn("error when locate bootstrap.properties,root cause:"+ex.getMessage());
		}
		if(applicationName==null)
		{
			logger.info("not find bootstrap.properties,use env to get applicationName");
			applicationName=format(System.getenv().get("spring.application.name"));
		}
		
		if(applicationName==null)
		{
			logger.info("not find bootstrap.properties,use env to get applicationName");
			applicationName=format(System.getProperty("spring.application.name"));
		}
		
		if(applicationName==null)
		{
			applicationName=super.getApplicationName();
		}
		
		if(applicationName!=null && applicationName.length()>MAX_SIZE){
			logger.warn("the application name["+applicationName+"] detect,but it is too long(should be no more than "+MAX_SIZE+")");
			throw new RuntimeException("the application name["+applicationName+"] is long than " +MAX_SIZE);
		}
		return applicationName;
	}

	private String format(String value) {
		return value;
	}

	public String getApplicationName() {
		return appName;
    }

    public String getAgentId() {
    	return agentId;
    }
    
    public static final class IdBuilder{
    	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    			'a', 'b', 'c', 'd', 'e', 'f' };

    	public static String generateId(String value) {
    		try {
    			MessageDigest digest = MessageDigest.getInstance("SHA-1");
    			byte[] bytes = digest.digest(value.getBytes(Charset.forName("UTF-8")));
    			return new String(encodeHex(bytes, 0, 8));
    		} catch (NoSuchAlgorithmException e) {
    			throw new IllegalStateException(e);
    		}
    	}

    	private static char[] encodeHex(byte[] bytes, int offset, int length) {
    		char chars[] = new char[length];
    		for (int i = 0; i < length; i = i + 2) {
    			byte b = bytes[offset + (i / 2)];
    			chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
    			chars[i + 1] = HEX_CHARS[b & 0xf];
    		}
    		return chars;
    	}
    }
}

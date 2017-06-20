/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.log4j2;

import java.security.ProtectionDomain;
import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

/**
 * 
 * @author shawn.yang
 *
 */
public class Log4j2Plugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private TransformTemplate transformTemplate;
    

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final Log4j2Config config = new Log4j2Config(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("Log4j2Plugin config:{}", config);
        }
        
        if (!config.isLog4j2LoggingTransactionInfo()) {
            logger.info("Log4j2 plugin is not executed because log4j2 transform enable config value is false.");
            return;
        }
        
        transformTemplate.transform("org.apache.logging.log4j.ThreadContext", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
               
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                
                final String interceptorClassName = "com.navercorp.pinpoint.plugin.log4j2.interceptor.LoggingEventOfLog4j2Interceptor";
                addInterceptor(target,"getImmutableContext",new String[]{}, interceptorClassName);
                addInterceptor(target,"getContext",new String[]{}, interceptorClassName);
                
                return target.toBytecode();
            }

            private void addInterceptor(InstrumentClass target, String methodName,String[] parameterTypes, String interceptorClassName) throws InstrumentException {
                InstrumentMethod method = InstrumentUtils.findMethod(target,methodName,parameterTypes);
                if (method == null) {
                    throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(parameterTypes));
                }
                method.addInterceptor(interceptorClassName);
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}

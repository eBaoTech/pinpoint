package com.navercorp.pinpoint.web.controller;

import static org.junit.Assert.*;

import org.junit.Test;

public class BusinessTransactionControllerTest {

	@Test
	public void testBuildLogUrl() {
		BusinessTransactionController controller=new BusinessTransactionController();
	 	String result= controller.buildLogUrl("http://172.25.13.46:5621/app/kibana#/discover?_g=()&_a=(index:current.applog.cloud.ebao,query:(query_string:(analyze_wildcard:!t,query:'xebao-PtxId:${traceid}'))) ", "gateway^1497521435550^4");
	 	System.out.println(result);
	}

}

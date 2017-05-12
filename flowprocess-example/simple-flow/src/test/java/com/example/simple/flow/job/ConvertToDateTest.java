package com.example.simple.flow.job;

import java.util.HashMap;
import java.util.Map;

import com.flowprocess.cedf.flow.IJob;

import junit.framework.TestCase;

public class ConvertToDateTest extends TestCase {

	public void testExec() {
		Map<String,Object> event;
		event=new HashMap<String,Object>();
		event.put("msg", System.currentTimeMillis());
		IJob job=new ConvertToDate();
		String ret=job.exec(event);
		System.out.println(ret);
		System.out.println(event);
		assertNotNull(ret);

	}

}

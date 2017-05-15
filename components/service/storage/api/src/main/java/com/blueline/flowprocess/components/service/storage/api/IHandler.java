package com.blueline.flowprocess.components.service.storage.api;

public interface IHandler<V> {
	
		public static final String PARAM_DEST_PROCESS_QUEUE="DestProcessQueue";
		public static final String PARAM_EXPIRED_DATA="ExpiredData";

		public boolean handle(V param);

}

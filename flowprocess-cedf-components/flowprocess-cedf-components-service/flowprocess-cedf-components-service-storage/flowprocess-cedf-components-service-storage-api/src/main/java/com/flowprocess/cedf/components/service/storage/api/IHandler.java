/**    
* @Title IHandler.java 
* @Package com.wisdom.csmp.service 
* @author Bailin
* @QQ 17717776 
* @date 2016��7��6�� ����11:18:20 
* @version $Id$
*/ 
package com.flowprocess.cedf.components.service.storage.api;

/** 
 * @ClassName IHandler 
 * @author Bailin 
 * @QQ 17717776 
 * @date 2016��7��6�� ����11:18:20 
 * @version $Id$
 * @since JDK 1.6 
 */
public interface IHandler<V> {
	
		public static final String PARAM_DEST_PROCESS_QUEUE="DestProcessQueue";
		public static final String PARAM_EXPIRED_DATA="ExpiredData";

		public boolean handle(V param);

}

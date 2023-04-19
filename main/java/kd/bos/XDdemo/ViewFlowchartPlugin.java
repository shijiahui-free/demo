package kd.bos.XDdemo;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kd.bos.servicehelper.workflow.WorkflowServiceHelper;
import kd.bos.workflow.api.BizProcessStatus;

public class ViewFlowchartPlugin {
	/**
//	 * 描述：根据单据的id获取单据当前的工作流节点当前处理人信息	
//	 */
	public static Map<String, String> getNextAuditor(List<String> pkIds) {
	    Map<String, String> nodeMap = new HashMap<>(10);
	    String[] ids = new String[pkIds.size()];
	    pkIds.toArray(ids);
	    Map<String, List<BizProcessStatus>> allPro = WorkflowServiceHelper.getBizProcessStatus(ids);
	    Iterator<Map.Entry<String, List<BizProcessStatus>>> var5 = allPro.entrySet().iterator();

	    while(var5.hasNext()) {
	        String pkid = var5.next().getKey();
	        List<BizProcessStatus> node = allPro.get(pkid);
	        node.forEach((e) -> {
	            String nodeStr = e.getCurrentNodeName();
	            String auditor = e.getParticipantName();
	            if (auditor != null && !"".equals(auditor.trim())) {
	                nodeStr = nodeStr + " / " + auditor;
	            }
	            nodeMap.put(pkid, nodeStr);
	        });
	    }
	    return nodeMap;
	}
}

package kd.bos.XDdemo;

import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;

public class LeaveApplyHandlerWF implements IWorkflowPlugin{
	@Override
	public void notify(AgentExecution execution) {
		String pkid=execution.getBusinessKey();
		List<Long> users=execution.getCurrentApprover();
		DynamicObject obj=BusinessDataServiceHelper.loadSingle(pkid,execution.getEntityNumber());
		if(!users.isEmpty()) {
			obj.set("handler", users.get(0));
		}
		SaveServiceHelper.save(new DynamicObject[] {obj});
	}
}

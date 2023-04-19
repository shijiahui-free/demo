package kd.bos.XDdemo;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;

public class XdApplyRejectWF implements IWorkflowPlugin{
	@Override
	public void notify(AgentExecution execution) {
		String pkid=execution.getBusinessKey();
		DynamicObject obj=BusinessDataServiceHelper.loadSingle(pkid,execution.getEntityNumber());
		obj.set("billstatus", "B");
		SaveServiceHelper.save(new DynamicObject[] {obj});
	}
}

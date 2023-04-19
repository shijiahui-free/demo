package kd.bos.XDdemo;

import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;

public class XdNotifyWF implements IWorkflowPlugin {

	@Override
	public List<Long> calcUserIds(AgentExecution execution) {
		String pkid = execution.getBusinessKey();
		DynamicObject obj = BusinessDataServiceHelper.loadSingle(pkid, execution.getEntityNumber());
		Long cjid = obj.getLong("rt00_entry_cj.id");
		List<Long> userids = UserServiceHelper.getAllUsersOfOrg(cjid);
		return userids;
	}
}

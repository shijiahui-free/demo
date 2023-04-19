package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.workflow.api.AgentExecution;
import kd.bos.workflow.engine.extitf.IWorkflowPlugin;

public class XdApplyCommitWF implements IWorkflowPlugin {
	@Override
	public void notify(AgentExecution execution) {
		String pkid = execution.getBusinessKey();
		DynamicObject obj = BusinessDataServiceHelper.loadSingle(pkid, execution.getEntityNumber());

		// 更新消毒状态
		String applyno = obj.getString("billno");

		List<QFilter> searchFilterList = new ArrayList<>();
		searchFilterList.add(new QFilter("billno", QCP.equals, applyno));
		DynamicObject[] dot = BusinessDataServiceHelper.load("rt00_xd_apply", "billno,billstatus",
				searchFilterList.toArray(new QFilter[] {}));
		if (dot[0].get("billstatus").equals("B")) {
			dot[0].set("billstatus", "C");
			SaveServiceHelper.update(dot);
		} else if (dot[0].get("billstatus").equals("C")) {
			dot[0].set("billstatus", "D");
			SaveServiceHelper.update(dot);
		}

//		obj.set("handler", "");
//		SaveServiceHelper.save(new DynamicObject[] { obj });

	}

	@Override
	public void notifyByWithdraw(AgentExecution execution) {
		String pkid = execution.getBusinessKey();
		DynamicObject obj = BusinessDataServiceHelper.loadSingle(pkid, execution.getEntityNumber());
		obj.set("billstatus", "B");
		SaveServiceHelper.save(new DynamicObject[] { obj });
	}
}

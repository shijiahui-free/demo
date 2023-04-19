package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.exception.KDException;
import kd.bos.message.api.MessageChannels;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;

public class XdTask extends AbstractTask {

	@Override
	public void execute(RequestContext context, Map<String, Object> param) throws KDException {

		QFilter qf = new QFilter("billstatus", QCP.equals, "D");
		// QFilter qf1=new QFilter("quarter", QCP.equals,getQuarter());
		DynamicObject[] datas = BusinessDataServiceHelper.load("rt00_xd_apply",
				"id,rt00_user_apply.id,rt00_user_apply.name,rt00_entry_date,rt00_entry_cj.name,billstatus", new QFilter[] { qf });

		if (datas != null) {
			try {
				Calendar calendarNew = Calendar.getInstance();
				for (int i = 0; i < datas.length; i++) {
					String rt00_user_applyId = datas[i].getString("rt00_user_apply.id");
					String rt00_user_applyName = datas[i].getString("rt00_user_apply.name");
					Date rt00_entry_date = datas[i].getDate("rt00_entry_date");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(rt00_entry_date);
					calendar.add(Calendar.DAY_OF_MONTH, 1);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					String rt00_entry_cjName = datas[i].getString("rt00_entry_cj.name");
					//if (calendarNew.compareTo(calendar) > 0) {
						datas[i].set("billstatus", "G");
						MessageInfo msg = new MessageInfo();
						msg.setTitle("消毒申请过期提醒");
						StringBuffer strBuf = new StringBuffer("亲爱的棕熊工厂员工").append(rt00_user_applyName).append("你所提交的")
								.append(rt00_entry_date).append("时间进入").append(rt00_entry_cjName)
								.append("的申请单已超期并自动关闭，如还需进入车间，请重新发起申请！");
						msg.setContent(strBuf.toString());

						List<Long> users = new ArrayList<Long>();
						users.add(Long.parseLong(rt00_user_applyId));
						msg.setUserIds(users);
						msg.setTag("提醒");
						msg.setType(MessageInfo.TYPE_WARNING);
						msg.setEntityNumber("rt00_xd_apply");
						msg.setNotifyType(MessageChannels.EMAIL.getNumber());
						// msg.setTplScene("success");
						MessageCenterServiceHelper.sendMessage(msg);
						System.out.println(rt00_user_applyId);
					//}
				}
				SaveServiceHelper.save(datas);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

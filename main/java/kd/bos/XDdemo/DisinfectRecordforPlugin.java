package kd.bos.XDdemo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyun.odps.utils.StringUtils;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.DateRangeEdit;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;

//消毒记录单
public class DisinfectRecordforPlugin extends AbstractFormPlugin {

	@Override
	public void afterCreateNewData(EventObject e) {
		// TODO Auto-generated method stub
		super.afterCreateNewData(e);
		// 获取用户id
		long userId = UserServiceHelper.getCurrentUserId();
		// 设置用户主业务组织为默认的申请部门/申请进入车间
		this.getModel().setValue("orgapply", UserServiceHelper.getUserMainOrgId(userId));
		OrgUnitServiceHelper.getRootOrgId();
	}

	@Override
	public void registerListener(EventObject event) {
		super.registerListener(event);
		// this.addItemClickListeners("toolbarap");
	}

	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		String key = evt.getItemKey();
		// 提交
		if ("bar_submit".equals(key)) {
			updateQjTs("0"); // 扣减对应员工的年假剩余天数
			updateQjTs("1"); // 扣减对应员工的年假剩余天数
		}

	}

	private void updateQjTs(String jq_type) {
		long userId = UserServiceHelper.getCurrentUserId();
		List<QFilter> searchFilterList = new ArrayList<>();
		searchFilterList.add(new QFilter("userfield.id", QCP.equals, userId));
		searchFilterList.add(new QFilter("type", QCP.equals, jq_type));

		// DynamicObject dot = QueryServiceHelper.queryOne("rt00_sh001",
		// "jq_staff.id,jq_type,jq_days", searchFilterList.toArray(new QFilter [] {}));
		DynamicObject[] dot = BusinessDataServiceHelper.load("paul_vacation_infor", "userfield.id,type,surplusday",
				searchFilterList.toArray(new QFilter[] {}));
		int jq_days = (int) this.getModel().getValue("year_remain");
		if (jq_type.equals("1")) {
			jq_days = (int) this.getModel().getValue("tx_remain");
		}

		dot[0].set("surplusday", jq_days);

		SaveServiceHelper.update(dot);
	}

	@Override
	public void afterBindData(EventObject e) {
		super.afterBindData(e);
		super.afterBindData(e);
		// 设置字体颜色（状态有A:保存、B:已提交（橙色）、C:审核中、 D:已审核（绿色）、E:消毒中、F:已完成消毒(红色)、G:废弃）
		HashMap<String, Object> map = new HashMap<>();
		if (this.getModel().getValue("billstatusfield") != null) {
//			this.disVailbe(false);
			if (this.getModel().getValue("billstatusfield").equals("A")) { // 暂存
				map.put(ClientProperties.ForeColor, "grey");
//				this.disVailbe(true);
			} else if (this.getModel().getValue("billstatusfield").equals("B")) { // 已提交
				map.put(ClientProperties.ForeColor, "red");
			} else if (this.getModel().getValue("billstatusfield").equals("C")) { // 审核中
				map.put(ClientProperties.ForeColor, "grey");
			} else if (this.getModel().getValue("billstatusfield").equals("D")) { // 已审核
				map.put(ClientProperties.ForeColor, "green");
			} else if (this.getModel().getValue("billstatusfield").equals("E")) { // E:消毒中(红色)
				map.put(ClientProperties.ForeColor, "grey");
			} else if (this.getModel().getValue("billstatusfield").equals("F")) { // F:已完成消毒(红色)
				map.put(ClientProperties.ForeColor, "red");
			} else {
				map.put(ClientProperties.ForeColor, "#fffff");
			}
		}
		this.getView().updateControlMetadata("billstatus", map);
	}
	@Override
	public void propertyChanged(PropertyChangedArgs e) {
		super.propertyChanged(e);
		String name = e.getProperty().getName();
		if (StringUtils.equals("step", name)) {
			Object step1 = e.getChangeSet()[0].getNewValue();
			String step = this.getModel().getValue("step").toString();

			QFilter filterLevel = new QFilter("name", QFilter.equals, "测试消毒方案");
			DynamicObjectCollection policycolScheme = QueryServiceHelper.query("rt00_xd_scheme", "id",
					new QFilter[] { filterLevel }, "");
			System.out.println(step1);
		}
	}

	public static Date getTomorrow() {
		LocalDate localdate = LocalDate.now().plusDays(1);
		return Date.from(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

}

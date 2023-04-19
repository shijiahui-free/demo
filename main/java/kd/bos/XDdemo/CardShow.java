package kd.bos.XDdemo;

import java.util.EventObject;
import java.util.List;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.form.control.Label;

public class CardShow  extends AbstractFormPlugin {
	@Override
	public void afterCreateNewData(EventObject e) {
		super.afterCreateNewData(e);
		
		ORM orm = ORM.create();
		
		// 人员申请单待消毒数
		QFilter[] filters = new QFilter[] { new QFilter("billstatus", "=", "D") };
		List<DynamicObject> materialList = orm.query("rt00_xd_apply", filters);
		if (materialList.size() > 0 && materialList != null) {
			// 设置人员申请单待消毒数
			Label label = this.getView().getControl("rt00_applynum");
			label.setText(String.valueOf(materialList.size()));
		}
		
		// 消毒中数
		QFilter[] filters2 = new QFilter[] { new QFilter("billstatus", "=", "E") };
		List<DynamicObject> materialList2 = orm.query("rt00_xd_apply", filters2);
		if (materialList2.size() > 0 && materialList2 != null) {
			// 设置消毒中数
			Label label = this.getView().getControl("rt00_disinfrectingnum");
			label.setText(String.valueOf(materialList2.size()));
		}
		
		// 已完成消毒数
		QFilter[] filters3 = new QFilter[] { new QFilter("billstatus", "=", "F") };
		List<DynamicObject> materialList3 = orm.query("rt00_xd_apply", filters3);
		if (materialList3.size() > 0 && materialList3 != null) {
			// 设置已完成消毒数
			Label label = this.getView().getControl("rt00_disinfrectednum");
			label.setText(String.valueOf(materialList3.size()));
		}
	}

}

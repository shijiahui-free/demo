package kd.bos.XDdemo;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

public class XdRecordBillPlugin extends AbstractBillPlugIn {

	@Override
	public void afterCreateNewData(EventObject e) {
		super.afterCreateNewData(e);
		// 调用控件方法
		String status = (String) this.getModel().getValue("billstatus");
		if (status.equals("A")) { // 单据状态为A：进行中
			this.getView().setVisible(true, "rt00_bar_next");
		} else {
			this.getView().setVisible(false, "rt00_bar_next");
		}
	}

	@Override
	public void itemClick(ItemClickEvent evt) {
		String key = evt.getItemKey();
		// 单据列表工具栏："下一步"按钮
		if ("rt00_bar_next".equals(key)) {
			// 获取当前单据的单据体 (单据体是分录)
			DynamicObjectCollection entryRows = this.getModel().getEntryEntity("rt00_xd_record_entity");
			// 对单据体行循环
			if (entryRows != null && entryRows.size() > 0) {
				String flag = "";
				int loop = 0;
				int size = entryRows.size();
				for (loop = 0; loop < size; loop++) {
					DynamicObject entryRow = entryRows.get(loop);
					String status = (String) entryRow.get("rt00_xd_billstatus");
					// 1.消毒状态为进行中方可操作，点击后当前步骤消毒状态改成已完成，下一行步骤的消毒状态改成进行中；A: 未进行、B:进行中、C:已完成
					if (status.equals("B")) {
						// 3.点击时，若当前进行中步骤没有上传图片则给出提示
//						String img = (String) entryRow.get("rt00_xd_img");
//						if (img == null || "".equals(img)) {
//							this.getView().showTipNotification("请先上传图片，再进行下一步！");
//							break;
//						} else {
							entryRow.set("rt00_xd_billstatus", "C");
							//entryRow.set("rt00_xd_img", img);
							entryRow.set("rt00_xd_finish_date", new Date());
							SaveServiceHelper.update(entryRow);
							flag = "B";
							continue;
						//}
					}
					if ("B".equals(flag) && status.equals("A")) {
						entryRow.set("rt00_xd_billstatus", "B");
						
						SaveServiceHelper.update(entryRow);
						flag = "A";
						break;
					}
				}
				// 2.最后一个消毒步骤完成后，消毒记录单的单据状态变成已完成，对应的申请单的状态变为已完成消毒
				if (loop >= size && ("B".equals(flag) || flag.equals(""))) {
					// 设置本单状态
					Object billno = this.getModel().getValue("billno");
					List<QFilter> searchFilterList = new ArrayList<>();
			        searchFilterList.add(new QFilter("billno", QCP.equals, billno));
			        DynamicObject dot = BusinessDataServiceHelper.loadSingle("rt00_xd_record", "id, billno, billstatus", searchFilterList.toArray(new QFilter [] {}));
			        dot.set("billstatus", "B");
			        SaveServiceHelper.update(dot);
			        
			        // 设置对应人员申请单状态
			        String applyno = (String)this.getModel().getValue("rt00_xd_apply_no");
					List<QFilter> searchFilterList2 = new ArrayList<>();
					searchFilterList2.add(new QFilter("billno", QCP.equals, applyno));
					DynamicObject dotApply = BusinessDataServiceHelper.loadSingle("rt00_xd_apply", "billno,billstatus", searchFilterList2.toArray(new QFilter [] {}));
					dotApply.set("billstatus", "F");
					SaveServiceHelper.update(dotApply);
				}
				
				 this.getView().updateView("rt00_xd_record_entity");
			}
		}
		super.itemClick(evt);
	}

}

package kd.bos.DB;

import dm.jdbc.util.StringUtil;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ICloseCallBack;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;

import java.util.EventObject;

/**
 * 地磅信息单据插件
 *
 * @author sjh
 * on 2023/5/18
 */
public class BangdanInfoBillPlugin extends AbstractBillPlugIn {
    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        addItemClickListeners("tbmain");
    }

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);

        //初始化界面用于控制《往来单位》是否展示
        String wmq_unithid = this.getModel().getValue("wmq_unithid").toString();
        if (StringUtil.isEmpty(wmq_unithid)) {
            this.getView().setVisible(false, "wmq_unithid");
        }
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        String itemKey = evt.getItemKey();
//        if ("bar_save".equals(itemKey) || "bar_submit".equals(itemKey)){
//
//        }
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);

        String name = e.getProperty().getName();
        switch (name) {
            //控制文本字段《往来单位》的显示
            case "wmq_unittype":
                this.getView().setVisible(true, "wmq_unithid");
                break;
            //基础资料《往来单位》赋值给文本字段《往来单位》
            case "wmq_unit":
                DynamicObject wmq_unit = (DynamicObject) this.getModel().getValue("wmq_unit");
                this.getModel().setValue("wmq_unithid", wmq_unit.getString("name"));
                break;
            //《磅单类型》改变赋值给隐藏字段《磅单类型简写》字段，用于《磅单号》的编码规则
            case "wmq_poundlisttype":
                String wmq_poundlisttype = (String) this.getModel().getValue("wmq_poundlisttype");
                //0：采购 ；1：销售；2：其他
                switch (wmq_poundlisttype) {
                    case "0":
                        this.getModel().setValue("wmq_poundlisttype_jc", "CG");
                        break;
                    case "1":
                        this.getModel().setValue("wmq_poundlisttype_jc", "XS");
                        break;
                    case "2":
                        this.getModel().setValue("wmq_poundlisttype_jc", "QT");
                        break;
                }
                break;
        }

    }
}

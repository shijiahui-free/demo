package kd.bos.billplugin;

import json.JSON;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;

import java.util.HashMap;

/**
 * 采购申请单弹窗插件服务
 *
 * @author sjh
 * on 2023/2/23
 */
public class PurApplyBillServicePlugin<afterBindData> extends AbstractBillPlugIn {
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        IDataEntityProperty property = e.getProperty();
        String name = property.getName();
        if (name.equals("wmq_applyqty")){
            //创建弹出页面对象，FormShowParameter表示弹出页面为动态表单
            FormShowParameter showParameter = new FormShowParameter();

            //设置弹出页面的编码
            showParameter.setFormId("wmq_tanchaungceshi");

            //设置弹出页面标题
            showParameter.setCaption("弹窗测试1234");

            showParameter.setCustomParam("wmq_textfield","你好");


            //设置页面关闭回调方法
            //CloseCallBack参数：回调插件，回调标识
            //ShowParameter.setCloseCallBack(new CloseCallBack(this, "biaoshi"));

            //设置弹出页面打开方式，支持模态，新标签等
            showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);

            //弹出页面对象赋值给父页面
            this.getView().showForm(showParameter);
        }
    }
}

package kd.bos.XDdemo;

import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillOperationStatus;
import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.script.annotations.KSObject;

import java.util.Map;

@KSObject
public class OpenFormUtil {
    private OpenFormUtil() {
    }

    /**
     * 打开动态表单
     *
     * @param currView
     * @param pageKey
     * @param showType
     * @param paramMap
     * @param closeCallBack
     */
    public static void openDynamicPage(IFormView currView, String title, String pageKey, ShowType showType,
                                       Map<String, Object> paramMap, CloseCallBack closeCallBack) {
        FormShowParameter fsp = new FormShowParameter();
        fsp.setFormId(pageKey);
        if (!StringUtils.isBlank(title)) fsp.setCaption(title);
        if (paramMap != null)
            fsp.setCustomParams(paramMap);
        if (closeCallBack != null)
            fsp.setCloseCallBack(closeCallBack);
        fsp.getOpenStyle().setShowType(showType);
        currView.showForm(fsp);
    }

    /**
     * 打开基础资料页面
     *
     * @param currView
     * @param pageKey
     * @param billId
     * @param openStatus
     * @param showType
     * @param paramMap
     * @param closeCallBack
     */
    public static void openBasePage(IFormView currView, String pageKey, Object billId, BillOperationStatus openStatus,
                                    ShowType showType, Map<String, Object> paramMap, CloseCallBack closeCallBack) {
        BaseShowParameter bsp = new BaseShowParameter();
        bsp.setFormId(pageKey);
        bsp.setPkId(billId);
        bsp.setBillStatus(openStatus);
        bsp.getOpenStyle().setShowType(showType);
        if (paramMap != null)
            bsp.setCustomParams(paramMap);
        if (closeCallBack != null)
            bsp.setCloseCallBack(closeCallBack);
        currView.showForm(bsp);
    }

    /**
     * 打开单据页面
     *
     * @param currView
     * @param pageKey       实体编码
     * @param billId        主键
     * @param openStatus    打开状态
     * @param showType      显示类型
     * @param paramMap      参数
     * @param closeCallBack 关闭回调
     */
    public static void openBillPage(IFormView currView, String pageKey, Object billId, BillOperationStatus openStatus,
                                    ShowType showType, Map<String, Object> paramMap, CloseCallBack closeCallBack) {
        BillShowParameter bsp = new BillShowParameter();
        bsp.setFormId(pageKey);
        bsp.setPkId(billId);
        bsp.setBillStatus(openStatus);
        bsp.getOpenStyle().setShowType(showType);
        if (paramMap != null)
            bsp.setCustomParams(paramMap);
        if (closeCallBack != null)
            bsp.setCloseCallBack(closeCallBack);
        currView.showForm(bsp);
    }

    /**
     * 打开单据页面
     *
     * @param currView
     * @param pageKey       实体编码
     * @param billId        主键
     * @param openStatus    打开状态
     * @param showType      显示类型
     * @param targetKey     showType为InContainer时要填写
     * @param paramMap      参数
     * @param closeCallBack 关闭回调
     */
    public static void openBillPage(IFormView currView, String pageKey, Object billId, BillOperationStatus openStatus, ShowType showType, String targetKey, Map<String, Object> paramMap, CloseCallBack closeCallBack) {
        BillShowParameter bsp = new BillShowParameter();
        bsp.setFormId(pageKey);
        bsp.setPkId(billId);
        bsp.setBillStatus(openStatus);
        bsp.getOpenStyle().setShowType(showType);
        bsp.getOpenStyle().setTargetKey(targetKey);
        if (paramMap != null)
            bsp.setCustomParams(paramMap);
        if (closeCallBack != null)
            bsp.setCloseCallBack(closeCallBack);
        currView.showForm(bsp);
    }

    /**
     * 打开列表页面
     *
     * @param currView
     * @param pageKey
     * @param showType
     * @param paramMap
     * @param qFilter
     * @param closeCallBack
     */
    public static void openListPage(IFormView currView, String pageKey, ShowType showType, Map<String, Object> paramMap, QFilter qFilter, CloseCallBack closeCallBack) {
        ListShowParameter lsp = new ListShowParameter();
        lsp.setBillFormId(pageKey);
        lsp.getOpenStyle().setShowType(showType);
        lsp.getListFilterParameter().setFilter(qFilter);
        if (paramMap != null)
            lsp.setCustomParams(paramMap);
        if (closeCallBack != null)
            lsp.setCloseCallBack(closeCallBack);
        currView.showForm(lsp);
    }
}

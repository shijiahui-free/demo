package kd.bos.XDdemo;

import java.util.EventObject;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.config.GlobalConfigurationKey;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.control.AbstractGrid;
import kd.bos.form.control.Control;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.UploadEvent;
import kd.bos.form.control.events.UploadListener;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.servicehelper.ConfigServiceHelper;
import kd.bos.upload.UploadOption;

public class TicketBillPlugIn extends AbstractBillPlugIn implements BeforeF7SelectListener, UploadListener {
	// 頁面上导入按钮标识
	private final static String KEY_BUTTON_UPLOAD = "rt00_record_imag_upload";
	private final static String KEY_ENTRYENTITY = "rt00_xd_record_entity";
	private final static String KEY_PICTUREFIELD = "rt00_xd_img";

	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);
		Toolbar mbar = this.getView().getControl("rt00_advcontoolbarap");// 高级面板工具栏
		mbar.addItemClickListener(this);
		mbar.addUploadListener(this);
	}

	public void itemclick(EventObject evt) {
		super.click(evt);
		Control source = (Control) evt.getSource();

		if (StringUtils.equals(source.getKey(), KEY_BUTTON_UPLOAD)) {

			UploadOption option = new UploadOption();
			option.setTitle("上传图片"); // 上传文件界面标题
			option.setMultiple(true); // 是否允许上传多个文件
			option.setSuffix(".png,.jpg"); // 文件后缀，".png,.jpg"
			option.setLimitSize(20000L); // 单个文件大小，单位是字节byte

			this.getView().showUpload(option, KEY_BUTTON_UPLOAD);
		}

	}

	@Override
	public void upload(UploadEvent evt) {
		// do nothing
	}

	@Override
	public void afterUpload(UploadEvent evt) {

		// 从事件的传入参数中，读取已上传到文件服务器的文件地址(相对地址，未包括文件服务器站点地址)
		if (evt.getUrls()[0] != null) {
			this.loadImag((String) evt.getUrls()[0]);
		}
	}

	@Override
	public void remove(UploadEvent evt) {
		// do nothing
	}

	@Override
	public void afterRemove(UploadEvent evt) {
		// do nothing
	}

	/**
	 * 导入图片
	 * 
	 * @param fileUrl
	 */
	private void loadImag(String fileUrl) {
		// 获取文件服务器站点地址、端口号
		String ipconfig = ConfigServiceHelper.getGlobalConfiguration(GlobalConfigurationKey.FILE_SERVER_URL).toString();
		// 拼接 http地址
		String url = ipconfig + fileUrl;
		// 获取单据体焦点行
		AbstractGrid grid = this.getView().getControl(KEY_ENTRYENTITY);
		int row = grid.getEntryState().getFocusRow();
		if (row < 0) {
			this.getView().showMessage("请选择要上传图片的行！");
		} else {
			// 填写单据体图片字段值，要指定行号
			this.getModel().setValue(KEY_PICTUREFIELD, url, row);
		}
	}

	@Override
	public void beforeF7Select(BeforeF7SelectEvent arg0) {
		// TODO Auto-generated method stub

	}

}

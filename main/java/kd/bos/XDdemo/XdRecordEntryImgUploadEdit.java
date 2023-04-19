package kd.bos.XDdemo;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 
 * 消毒记录图片上传处理插件
 * 
 * 分录图片上传处理插件
 * 
 * @author 软通动力
 *
 */
public class XdRecordEntryImgUploadEdit extends AbstractBasePlugIn {

	public void propertyChanged(PropertyChangedArgs e) {
		if ("rt00_xd_attach".equals(e.getProperty().getName())) {
			ChangeData[] changeData = e.getChangeSet();
			int index = -1;
			if (changeData.length > 0) {
				index = changeData[0].getRowIndex();
			}

			DynamicObjectCollection dynCol = (DynamicObjectCollection) this.getModel().getValue("rt00_xd_attach",
					index);
			if (dynCol.size() > 0) {
				DynamicObject attObj = ((DynamicObject) dynCol.get(0)).getDynamicObject("fbasedataid");
				String path = attObj.getString("url");
				this.getModel().setValue("rt00_xd_img", path, index);
				SaveServiceHelper.save(new DynamicObject[] { this.getModel().getDataEntity() });
			}
		}

	}

}

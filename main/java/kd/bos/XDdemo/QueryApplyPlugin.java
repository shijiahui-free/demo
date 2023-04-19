package kd.bos.XDdemo;

import java.util.Map;

import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.entity.api.ApiResult;

public class QueryApplyPlugin  implements IBillWebApiPlugin{
	@Override
	 public ApiResult doCustomService(Map<String, Object> params) {
	 //实现业务逻辑。。。
	 ApiResult apiResult = new ApiResult();
	 apiResult.setSuccess(true);
	 apiResult.setErrorCode("success");
	 apiResult.setMessage("HelloWorld Success");
	 apiResult.setData(null);
	 return apiResult;
	 }
}

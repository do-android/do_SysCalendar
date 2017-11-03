package dotest.module.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import core.DoServiceContainer;
import doext.implement.do_SysCalendar_Model;
import dotest.module.frame.debug.DoService;

/**
 * webview组件测试样例
 */
public class WebViewSampleTestActivty extends DoTestActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initModuleModel() throws Exception {
		this.model = new do_SysCalendar_Model();
	}

	@Override
	protected void initUIView() throws Exception {
//		Do_WebView_View view = new Do_WebView_View(this);
//		((DoUIModule) this.model).setCurrentUIModuleView(view);
//		((DoUIModule) this.model).setCurrentPage(currentPage);
//		view.loadView((DoUIModule) this.model);
//		LinearLayout uiview = (LinearLayout) findViewById(R.id.uiview);
//		uiview.addView(view);
	}

	@Override
	public void doTestProperties(View view) {
		// DoService.setPropertyValue(this.model, "url",
		// "https://www.baidu.com");

		Map<String, String> _paras_loadString = new HashMap<String, String>();
		DoService.asyncMethod(this.model, "getAll", _paras_loadString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {// 回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}

	@Override
	protected void doTestSyncMethod() {
		Map<String, String> _paras_loadString = new HashMap<String, String>();
		_paras_loadString.put("title", "我是标题2");
		_paras_loadString.put("description", "www");

		Calendar calendar = Calendar.getInstance();
		String startString = calendar.getTime().getTime() + "";
		//_paras_loadString.put("startTime", "1503478800000");
		_paras_loadString.put("startTime", startString);
		calendar.add(Calendar.WEEK_OF_MONTH, 1);
		String endTime = calendar.getTime().getTime() + "";
		//_paras_loadString.put("endTime", "1504256400000");
		_paras_loadString.put("endTime", endTime);
		_paras_loadString.put("location", "神州数码");

//		_paras_loadString.put("reminderTime", "0");
		_paras_loadString.put("reminderRepeatMode", "day");
		_paras_loadString.put("reminderRepeatEndTime", "");

		DoService.asyncMethod(this.model, "add", _paras_loadString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {// 回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}

	@Override
	protected void doTestAsyncMethod() {

	}

	@Override
	protected void onEvent() {
		// 系统事件订阅
		DoService.subscribeEvent(this.model, "loaded", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("系统事件回调：name = loaded, data = " + _data);
				Toast.makeText(WebViewSampleTestActivty.this, "系统事件回调：loaded", Toast.LENGTH_LONG).show();
			}
		});
		// 自定义事件订阅
		DoService.subscribeEvent(this.model, "_messageName", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("自定义事件回调：name = _messageName, data = " + _data);
				Toast.makeText(WebViewSampleTestActivty.this, "自定义事件回调：_messageName", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void doTestFireEvent(View view) {
		Map<String, String> _paras_loadString = new HashMap<String, String>();
		_paras_loadString.put("id", myEdit.getText().toString());
		DoService.asyncMethod(this.model, "delete", _paras_loadString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {// 回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});

//		Map<String, String> _paras_loadString = new HashMap<String, String>();
//		_paras_loadString.put("id", myEdit.getText().toString());
//		_paras_loadString.put("title", "title1");
//		_paras_loadString.put("description", "description1");
//		DoService.asyncMethod(this.model, "update", _paras_loadString, new DoService.EventCallBack() {
//			@Override
//			public void eventCallBack(String _data) {// 回调函数
//				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
//			}
//		});
	}

}


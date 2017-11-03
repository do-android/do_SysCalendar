package doext.implement;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.define.do_SysCalendar_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_SysCalendar_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_SysCalendar_Model extends DoSingletonModule implements do_SysCalendar_IMethod {

	static Context mContext = null;
	final String calanderURL = "content://com.android.calendar/calendars";
	final static String calanderEventURL = "content://com.android.calendar/events";

	// https://github.com/MetaD/Evento/blob/master/src/com/summer/evento/CalendarsResolver.java
	public do_SysCalendar_Model() throws Exception {
		super();
		mContext = DoServiceContainer.getPageViewFactory().getAppContext();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 *
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		// ...do something
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 *
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.getUniqueKey());
		if ("getAll".equals(_methodName)) {
			this.getAll(_dictParas, _scriptEngine, _invokeResult, _callbackFuncName);
			return true;
		} else if ("add".equals(_methodName)) {
			this.add(_dictParas, _scriptEngine, _invokeResult, _callbackFuncName);
			return true;
		} else if ("update".equals(_methodName)) {
			this.update(_dictParas, _scriptEngine, _invokeResult, _callbackFuncName);
			return true;
		} else if ("delete".equals(_methodName)) {
			this.delete(_dictParas, _scriptEngine, _invokeResult, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 添加日程；
	 *
	 * @throws JSONException
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	// https://stackoverflow.com/questions/28871921/add-weekly-event-to-calendar
	@Override
	public void add(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws JSONException {
		String title = DoJsonHelper.getString(_dictParas, "title", "");
		String description = DoJsonHelper.getString(_dictParas, "description", "");
		String startTime = DoJsonHelper.getString(_dictParas, "startTime", "");
		String endTime = DoJsonHelper.getString(_dictParas, "endTime", "");
		String location = DoJsonHelper.getString(_dictParas, "location", "");
		String reminderTime = DoJsonHelper.getString(_dictParas, "reminderTime", "0");
		String reminderRepeatMode = DoJsonHelper.getString(_dictParas, "reminderRepeatMode", "");
		String reminderRepeatEndTime = DoJsonHelper.getString(_dictParas, "reminderRepeatEndTime", "");
		String returnId = "";

		try {
			if (TextUtils.isEmpty(title)) {
				_invokeResult.setResultText("请输入日程主题");
			} else if (TextUtils.isEmpty(startTime)) {
				_invokeResult.setResultText("请输入日程开始时间");
			} else if (TextUtils.isEmpty(endTime)) {
				_invokeResult.setResultText("请输入日程结束时间");
			} else if (TextUtils.isEmpty(description)) {
				_invokeResult.setResultText("请输入日程描述");
			} else if (Long.valueOf(startTime) > Long.valueOf(endTime)) {
				_invokeResult.setResultText("日程结束时间不能小于开始时间");
			} else {
				Cursor userCursor = mContext.getContentResolver().query(Uri.parse(calanderURL), null, null, null, null);
				if (userCursor.getCount() > 0) {
					userCursor.moveToFirst();
					String calId = userCursor.getString(userCursor.getColumnIndex("_id"));
					ContentValues event = new ContentValues();
					event.put("calendar_id", calId);
					event.put("title", title);
					event.put("description", description);
					event.put("dtstart", startTime);
					if (!TextUtils.isEmpty(location))
						event.put("eventLocation", location);
					event.put("hasAlarm", 1);
					// 设置时区
					event.put("eventTimezone", TimeZone.getDefault().getID().toString());

					// dtend和duration不能同时使用 否则无效
					if (TextUtils.isEmpty(reminderRepeatMode)) {
						event.put("dtend", endTime);
					} else {
						event.put("duration", getDuration(startTime, endTime));
						if ("day".equals(reminderRepeatMode)) {
							event.put("rrule", "FREQ=DAILY;UNTIL=" + getreminderRepeatEndTime(reminderRepeatEndTime));
						}
						if ("week".equals(reminderRepeatMode)) {
							event.put("rrule", "FREQ=WEEKLY;UNTIL=" + getreminderRepeatEndTime(reminderRepeatEndTime));
						}
						if ("month".equals(reminderRepeatMode)) {
							event.put("rrule", "FREQ=MONTHLY;UNTIL=" + getreminderRepeatEndTime(reminderRepeatEndTime));
						}
						if ("year".equals(reminderRepeatMode)) {
							event.put("rrule", "FREQ=YEARLY;UNTIL=" + getreminderRepeatEndTime(reminderRepeatEndTime));
						}
					}
					Uri newEvent = mContext.getContentResolver().insert(Uri.parse(calanderEventURL), event);
					long id = Long.parseLong(newEvent.getLastPathSegment());
					returnId = id + "";
					ContentValues values = new ContentValues();
					// 没有设置reminderTime 默认为0 准时提醒
					values.put("minutes", reminderTime);
					values.put("event_id", id);
					values.put("method", CalendarContract.Reminders.METHOD_ALERT);

					ContentResolver cr1 = mContext.getContentResolver(); // 为刚才新添加的event添加reminder
					cr1.insert(CalendarContract.Reminders.CONTENT_URI, values);

					_invokeResult.setResultText(returnId);
				} else {
					_invokeResult.setResultText("没有默认账户,请添加账户");
				}
			}
		} catch (Exception e) {
			_invokeResult.setResultText(e.getMessage());
			DoServiceContainer.getLogEngine().writeInfo("do_SysCalendar add", e.getMessage());
		} finally {
			_scriptEngine.callback(_callbackFuncName, _invokeResult);
		}

	}

	private String getDuration(String startTime, String endTime) {
		long duration = Long.valueOf(endTime) - Long.valueOf(startTime);
		String second = String.valueOf(duration / 1000);// 转化second
		String result = "P" + second + "S";
		return result;
	}

	private String getreminderRepeatEndTime(String reminderRepeatEndTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if (TextUtils.isEmpty(reminderRepeatEndTime)) {
			Calendar ca = Calendar.getInstance();// 得到一个Calendar的实例
			ca.setTime(new Date()); // 设置时间为当前时间
			ca.add(Calendar.YEAR, 10); // 年份减1
			reminderRepeatEndTime = String.valueOf(ca.getTime().getTime()); // 结果
		}
		java.util.Date date = new Date(Long.parseLong(reminderRepeatEndTime));
		return sdf.format(date);
	}

	/**
	 * 根据id删除对应的日程；
	 *
	 * @throws JSONException
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void delete(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws JSONException {
		String id = DoJsonHelper.getString(_dictParas, "id", "");
		if (!TextUtils.isEmpty(id)) {
			long nId = Long.parseLong(id);
			ContentResolver resolver = mContext.getContentResolver();
			int nums = resolver.delete(ContentUris.withAppendedId(Uri.parse(calanderEventURL), nId), null, null);
			if (nums > 0) {
				_invokeResult.setResultBoolean(true);
			} else {
				_invokeResult.setResultBoolean(false);
			}
		} else {
			DoServiceContainer.getLogEngine().writeInfo("do_SysCalendar delete", "id不能为空");
			_invokeResult.setResultBoolean(false);
		}
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}

	/**
	 * 根据所有日程信息；
	 *
	 * @throws JSONException
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */

	@Override
	public void getAll(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws JSONException {
		JSONArray _array = new JSONArray();
		try {
			String[] selectionArgs = new String[] { "local" };
			String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " <> ?))";
			Cursor eventCursor = mContext.getContentResolver().query(Uri.parse(calanderEventURL), null, selection, selectionArgs, null);
			if (eventCursor != null) {
				if (eventCursor.getCount() > 0) {
					while (eventCursor.moveToNext()) {
						JSONObject _jsonObject = new JSONObject();
						String id = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events._ID));
						String title = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.TITLE));
						String description = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
						String startTime = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.DTSTART));
						String endTime = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.DTEND));
						String location = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
						try {
							_jsonObject.put("id", id);
							_jsonObject.put("title", title != null ? title : "");
							_jsonObject.put("description", description != null ? description : "");
							_jsonObject.put("startTime", startTime != null ? startTime : "");
							_jsonObject.put("endTime", endTime != null ? endTime : "");
							_jsonObject.put("location", location != null ? location : "");
							_array.put(_jsonObject);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				DoServiceContainer.getLogEngine().writeError("do_SysCalendar getAll", new Exception("日历不可用"));
			}
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("do_SysCalendar getAll", e);
		}
		_invokeResult.setResultArray(_array);
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}

	/**
	 * 根据id修改对应的日程；
	 *
	 * @throws JSONException
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void update(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws JSONException {
		String id = DoJsonHelper.getString(_dictParas, "id", "");
		String title = DoJsonHelper.getString(_dictParas, "title", "");
		String description = DoJsonHelper.getString(_dictParas, "description", "");
		String startTime = DoJsonHelper.getString(_dictParas, "startTime", "");
		String endTime = DoJsonHelper.getString(_dictParas, "endTime", "");
		String location = DoJsonHelper.getString(_dictParas, "location", "");

		if (!TextUtils.isEmpty(id)) {
			long _updateId = Long.parseLong(id);
			ContentValues event = new ContentValues();
			if (!TextUtils.isEmpty(title))
				event.put("title", title);
			if (!TextUtils.isEmpty(description))
				event.put("description", description);
			if (!TextUtils.isEmpty(startTime))
				event.put("dtstart", startTime);
			if (!TextUtils.isEmpty(endTime))
				event.put("dtend", endTime);
			if (!TextUtils.isEmpty(location))
				event.put("eventLocation", location);

			Uri updateUri = ContentUris.withAppendedId(Uri.parse(calanderEventURL), _updateId);
			int rows = mContext.getContentResolver().update(updateUri, event, null, null);
			if (rows > 0)
				_invokeResult.setResultBoolean(true);
		} else {
			_invokeResult.setResultBoolean(false);
		}
		_scriptEngine.callback(_callbackFuncName, _invokeResult);
	}
}
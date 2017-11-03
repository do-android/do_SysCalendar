package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;

/**
 * 声明自定义扩展组件方法
 */
public interface do_SysCalendar_IMethod {
    void add(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws Exception;

    void delete(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws Exception;

    void getAll(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws Exception;

    void update(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult, String _callbackFuncName) throws Exception;
}
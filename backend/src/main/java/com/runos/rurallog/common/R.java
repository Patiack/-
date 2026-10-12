package com.runos.rurallog.common;

import java.util.HashMap;
import java.util.Map;

/** 统一响应包装: {code, msg, data} */
public class R {
    public int code;
    public String msg;
    public Object data;

    public static R ok(Object data) {
        R r = new R(); r.code = 0; r.msg = "ok"; r.data = data; return r;
    }

    public static R ok() { return ok(new HashMap<>()); }

    public static R fail(int code, String msg) {
        R r = new R(); r.code = code; r.msg = msg; r.data = null; return r;
    }

    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }
}

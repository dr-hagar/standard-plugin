package com.sbezboro.standardplugin.net;

import com.sbezboro.http.listeners.HttpRequestListener;
import com.sbezboro.standardplugin.StandardPlugin;

public class RegisterHttpRequest extends ApiHttpRequest {

	public RegisterHttpRequest(String uuid, String username, String email, HttpRequestListener listener) {
		super(StandardPlugin.getPlugin(), "register", HTTPMethod.POST, listener);

		addProperty("uuid", uuid);
		addProperty("username", username);
		addProperty("email", email);
	}
}

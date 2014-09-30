package io.torch.example.template;

import io.torch.controller.WebPage;
import io.torch.http.request.TorchHttpRequest;
import io.torch.http.response.TorchHttpResponse;
import io.torch.session.Session;

import java.util.HashMap;
import java.util.Map;

public class TemplateExample extends WebPage {

    @Override
    public void handle(TorchHttpRequest request, TorchHttpResponse response, Session session) {
		Map<String, Object> data = new HashMap<>();

		data.put("username", "Meow");
		data.put("extrainfo", "ME-YOW");

		response.templateData("user", data);
		response.template("example/template/example.tpl");
    }

}

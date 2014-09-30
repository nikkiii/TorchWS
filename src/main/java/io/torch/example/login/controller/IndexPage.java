package io.torch.example.login.controller;

import io.torch.controller.WebPage;
import io.torch.http.request.TorchHttpRequest;
import io.torch.http.response.TorchHttpResponse;
import io.torch.session.Session;
import io.torch.template.Templateable;

@Templateable(path = "example/login/index.tpl")
public class IndexPage extends WebPage {

    @Override
    public void handle(TorchHttpRequest request, TorchHttpResponse response, Session session) {
        response.setTemplateData("loggedIn", session.isSessionVariableSet("userid"));

        if (session.isSessionVariableSet("userid")) {
			response.setTemplateData("username", "admin");
        }
    }
}

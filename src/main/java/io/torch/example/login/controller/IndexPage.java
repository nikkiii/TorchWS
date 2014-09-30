package io.torch.example.login.controller;

import io.torch.controller.WebPage;
import io.torch.http.request.TorchHttpRequest;
import io.torch.http.response.TorchHttpResponse;
import io.torch.session.Session;

public class IndexPage extends WebPage {

    @Override
    public void handle(TorchHttpRequest request, TorchHttpResponse response, Session session) {
        response.templateData("loggedIn", session.isSessionVariableSet("userid"));

        if (session.isSessionVariableSet("userid")) {
			response.templateData("username", "admin");
        }

		response.template("example/login/index.tpl");
    }
}

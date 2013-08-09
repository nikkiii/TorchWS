package io.torch.example.login.controller;

import io.torch.example.login.data.IndexData;
import io.torch.handler.WebPage;
import io.torch.http.TorchHttpRequest;
import io.torch.http.TorchHttpResponse;
import io.torch.session.Session;
import io.torch.template.Templateable;

public class IndexPage extends WebPage implements Templateable {
    
    private IndexData indexData;

    @Override
    public void handle(TorchHttpRequest request, TorchHttpResponse response, Session session) {
        indexData = new IndexData(session.isSessionVariableSet("userid"));
        
        if(session.isSessionVariableSet("userid")) {
            indexData.setUsername("admin"); //query the real username from the db here
        }
    }

    @Override
    public String getTemplate() {
        return "example/login/index.tpl";
    }

    @Override
    public Object getTemplateRoot() {
        return indexData;
    }
}
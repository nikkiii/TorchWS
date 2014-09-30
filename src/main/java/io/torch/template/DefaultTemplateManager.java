package io.torch.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class DefaultTemplateManager implements TemplateManager {

    private final PebbleEngine engine = new PebbleEngine();

    public DefaultTemplateManager() {
		engine.getLoader().setPrefix("templates/");
    }

    @Override
    public boolean templateExists(String templateName) throws IOException, PebbleException {
        return engine.getTemplate(templateName) != null;
    }

    @Override
    public String processTemplate(String templateName, Map<String, Object> templateData) throws PebbleException, IOException {
        Writer templateText = new StringWriter();

		PebbleTemplate template = engine.getTemplate(templateName);

		template.evaluate(templateText, templateData);

        return templateText.toString();

    }
}

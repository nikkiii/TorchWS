package io.torch.template;

import com.mitchellbosecke.pebble.error.PebbleException;
import java.io.IOException;
import java.util.Map;

public interface TemplateManager {

    public boolean templateExists(String templateName) throws IOException, PebbleException;

    public String processTemplate(String templateName, Map<String, Object> templateData) throws PebbleException, IOException;
}

package wso2.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.log4j.Logger;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.task.Task;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.commons.lang.StringUtils;

public class SampleScheduledTask implements Task, ManagedLifecycle {

    private final Logger log = Logger.getLogger(SampleScheduledTask.class);

    private String param1 = "undefined";
    private String param2 = "undefined";

    private static final String SECURE_VAULT_REGEX = "(wso2:vault-lookup\\('(.*?)'\\))";
    private Pattern vaultLookupPattern = Pattern.compile(SECURE_VAULT_REGEX);
    private SynapseEnvironment synapseEnvironment;

    public void init(SynapseEnvironment se) {
        synapseEnvironment = se;
        MessageContext messageContext = synapseEnvironment.createMessageContext();
        // Checks whether the parameter values contains lookups.


        //If the lookup is defined as the parameter 01. Resolve and retrieve the actual password.
        if (param1.matches(SECURE_VAULT_REGEX)) {
            log.debug("param1 = " + param1);
            String resolvedValue = resolveSecureVaultExpressions(param1, messageContext);
            setParam1(resolvedValue);
            log.debug("resolved_param1 = " + param1);
        }
        if (param2.matches(SECURE_VAULT_REGEX)) {
            log.debug("param2 = " + param2);
            String resolvedValue = resolveSecureVaultExpressions(param2, messageContext);
            setParam2(resolvedValue);
            log.debug("resolved_param2 = " + param2);
        }

    }

    public void execute() {
        try {

            Files.write(Paths.get("/Users/shanaka//Desktop/INABOXGROUPDEV-36/text.log"),
                    ("param1: " + param1 + "," + " param2: " + param2 + "\n").getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    private String resolveSecureVaultExpressions(String parameter, MessageContext messageContext) {
        //Password can be null, it is optional
        if (parameter == null) {
            return null;
        }
        Matcher lookupMatcher = vaultLookupPattern.matcher(parameter);
        String resolvedValue = "";
        if (lookupMatcher.find()) {
            Value expression;
            String expressionStr = lookupMatcher.group(1);
            try {
                expression = new Value(new SynapseXPath(expressionStr));

            } catch (JaxenException e) {
                throw new SynapseException("Error while building the expression : " + expressionStr, e);
            }
            resolvedValue = expression.evaluateValue(messageContext);
            if (StringUtils.isEmpty(resolvedValue)) {
                log.warn("Found Empty value for expression : " + expression.getExpression());
                resolvedValue = "";
            }
        }
        return resolvedValue;
    }

    public void destroy() {

    }

}

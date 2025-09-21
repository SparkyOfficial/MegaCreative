package com.megacreative.coding.containers;

import com.megacreative.coding.values.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Action configuration
 */
public class ActionConfiguration {
    private final String action;
    private final List<ActionParameter> parameters;
    
    public ActionConfiguration(String action) {
        this.action = action;
        this.parameters = generateParameters(action);
    }
    
    private List<ActionParameter> generateParameters(String action) {
        List<ActionParameter> params = new ArrayList<>();
        
        switch (action) {
            case "sendMessage":
                params.add(new ActionParameter("message", ValueType.TEXT, "Message to send to player"));
                break;
            case "teleport":
                params.add(new ActionParameter("location", ValueType.LOCATION, "Destination location"));
                break;
            case "giveItem":
                params.add(new ActionParameter("item", ValueType.ITEM, "Item to give"));
                params.add(new ActionParameter("amount", ValueType.NUMBER, "Number of items"));
                break;
            case "playSound":
                params.add(new ActionParameter("sound", ValueType.SOUND, "Sound to play"));
                params.add(new ActionParameter("volume", ValueType.NUMBER, "Sound volume"));
                params.add(new ActionParameter("pitch", ValueType.NUMBER, "Sound pitch"));
                break;
            default:
                params.add(new ActionParameter("value", ValueType.ANY, "Action parameter"));
        }
        
        return params;
    }
    
    public String getAction() { return action; }
    public List<ActionParameter> getParameters() { return parameters; }
    public int getParameterCount() { return parameters.size(); }
}